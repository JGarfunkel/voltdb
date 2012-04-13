/* This file is part of VoltDB.
 * Copyright (C) 2012 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package org.voltdb.jdbc;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Future;

import org.voltdb.client.Client;
import org.voltdb.client.ClientConfig;
import org.voltdb.client.ClientFactory;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.NoConnectionsException;
import org.voltdb.client.ProcCallException;
import org.voltdb.client.ProcedureCallback;

/**
 * Provides a high-level wrapper around the core {@link Client} class to provide performance
 * tracking, connection pooling and Future-based asynchronous execution support. ClientConnections
 * should be obtained through the {@link JDBC4ClientConnectionPool} get methods and cannot be
 * instantiated directly.
 *
 * Extending ClientStatusListenerExt allows us to detect dropped connections, etc..
 *
 * @author Seb Coursol (copied and renamed from exampleutils)
 * @since 2.0
 */
public class JDBC4ClientConnection implements Closeable {
    private final JDBC4PerfCounterMap statistics;
    private final ArrayList<String> servers;
    private final int port;
    private final Client client;

    /**
     * The base hash/key for this connection, that uniquely identifies its parameters, as defined by
     * the pool.
     */
    protected final String keyBase;

    /**
     * The actual hash/key for this connection, that uniquely identifies this specific native
     * {@link Client} wrapper.
     */
    protected final String key;

    /**
     * The number of active users on the connection. Used and managed by the pool to determine when
     * a specific {@link Client} wrapper has reached capacity (and a new one should be created).
     */
    protected short users;

    /**
     * The default asynchronous operation timeout for Future-based executions (while the operation
     * may so time out on the client side, note that, technically, once submitted to the database
     * cluster, the call cannot be cancelled!).
     */
    protected long defaultAsyncTimeout = 60000;

    /**
     * Creates a new native client wrapper from the given parameters (internal use only).
     *
     * @param clientConnectionKeyBase
     *            the base hash/key for this connection, as defined by the pool.
     * @param clientConnectionKey
     *            the actual hash/key for this connection, as defined by the pool (may contain a
     *            trailing index when the pool decides a new client needs to be created based on the
     *            number of clients).
     * @param servers
     *            the list of VoltDB servers to connect to.
     * @param port
     *            the VoltDB native protocol port to connect to (usually 21212).
     * @param user
     *            the user name to use when connecting to the server(s).
     * @param password
     *            the password to use when connecting to the server(s).
     * @param isHeavyWeight
     *            the flag indicating callback processes on this connection will be heavy (long
     *            running callbacks). By default the connection only allocates one background
     *            processing thread to process callbacks. If those callbacks run for a long time,
     *            the network stack can get clogged with pending responses that have yet to be
     *            processed, at which point the server will disconnect the application, thinking it
     *            died and is not reading responses as fast as it is pushing requests. When the flag
     *            is set to 'true', an additional 2 processing thread will deal with processing
     *            callbacks, thus mitigating the issue.
     * @param maxOutstandingTxns
     *            the number of transactions the client application may push against a specific
     *            connection before getting blocked on back-pressure. By default the connection
     *            allows 3,000 open transactions before preventing the client from posting more
     *            work, thus preventing server fire-hosing. In some cases however, with very fast,
     *            small transactions, this limit can be raised.
     * @throws IOException
     * @throws UnknownHostException
     */
    protected JDBC4ClientConnection(String clientConnectionKeyBase, String clientConnectionKey,
            String[] servers, int port, String user, String password, boolean isHeavyWeight,
            int maxOutstandingTxns) throws UnknownHostException, IOException {
        // Save the list of trimmed non-empty server names.
        this.servers = new ArrayList<String>(servers.length);
        for (String server : servers) {
            String server2 = server.trim();
            if (!server2.isEmpty()) {
                this.servers.add(server2);
            }
        }
        if (this.servers.isEmpty()) {
            throw new UnknownHostException("JDBC4ClientConnection: no servers provided");
        }

        this.port = port;
        this.keyBase = clientConnectionKeyBase;
        this.key = clientConnectionKey;
        this.statistics = JDBC4ClientConnectionPool.getStatistics(clientConnectionKeyBase);

        // Create configuration
        final ClientConfig config = new ClientConfig(user, password);
        config.setHeavyweight(isHeavyWeight);
        if (maxOutstandingTxns > 0)
            config.setMaxOutstandingTxns(maxOutstandingTxns);

        // Create client and connect.
        this.client = ClientFactory.createClient(config);
        this.users = 0;
        for (String server : this.servers) {
            this.client.createConnection(server, this.port);
        }
    }

    /**
     * Used by the pool to indicate a new thread/user is using a specific connection, helping the
     * pool determine when new connections need to be created.
     *
     * @return the reference to this connection to be returned to the calling user.
     */
    protected JDBC4ClientConnection use() {
        this.users++;
        return this;
    }

    /**
     * Used by the pool to indicate a thread/user has stopped using the connection (and optionally
     * close the underlying client if there are no more users against it).
     */
    protected void dispose() {
        this.users--;
        if (this.users == 0) {
            try {
                this.client.close();
            } catch (Exception x) {
                // ignore
            }
        }
    }

    /**
     * Closes the connection, releasing it to the pool so another thread/client may pick it up. This
     * method must be closed by a user when the connection is no longer needed to avoid pool
     * pressure and leaks where the pool would keep creating new connections all the time,
     * wrongfully believing all existing connections to be actively used.
     */
    @Override
    public void close() {
        JDBC4ClientConnectionPool.dispose(this);
    }

    /**
     * Executes a procedure synchronously and returns the result to the caller. The method
     * internally tracks execution performance.
     *
     * @param procedure
     *            the name of the procedure to call.
     * @param parameters
     *            the list of parameters to pass to the procedure.
     * @return the response sent back by the VoltDB cluster for the procedure execution.
     * @throws IOException
     * @throws NoConnectionsException
     * @throws ProcCallException
     */
    public ClientResponse execute(String procedure, Object... parameters)
            throws NoConnectionsException, IOException, ProcCallException {
        long start = System.currentTimeMillis();
        try {
            // If connections are lost try reconnecting.
            ClientResponse response = this.client.callProcedure(procedure, parameters);
            this.statistics.update(procedure, response);
            return response;
        } catch (ProcCallException pce) {
            this.statistics.update(procedure, System.currentTimeMillis() - start, false);
            throw pce;
        }
    }

    /**
     * Internal asynchronous callback used to track the execution performance of asynchronous calls.
     */
    private static class TrackingCallback implements ProcedureCallback {
        private final JDBC4ClientConnection Owner;
        private final String Procedure;
        private final ProcedureCallback UserCallback;

        /**
         * Creates a new callback.
         *
         * @param owner
         *            the connection to which the request was sent (and that will be receiving the
         *            response).
         * @param procedure
         *            the procedure being executed and for which we're awaiting a response.
         * @param userCallback
         *            the user-specified callback that will be called once we have tracked
         *            statistics, making this internal callback transparent to the calling
         *            application.
         */
        public TrackingCallback(JDBC4ClientConnection owner, String procedure,
                ProcedureCallback userCallback) {
            this.Owner = owner;
            this.Procedure = procedure;
            this.UserCallback = userCallback;
        }

        /**
         * Processes the server response, tracking performance statistics internally, then calling
         * the user-specified callback (if any).
         */
        @Override
        public void clientCallback(ClientResponse response) throws Exception {

            this.Owner.getStatistics().update(this.Procedure, response);
            if (this.UserCallback != null)
                this.UserCallback.clientCallback(response);
        }
    }

    /**
     * Executes a procedure asynchronously, then calls the provided user callback with the server
     * response upon completion.
     *
     * @param callback
     *            the user-specified callback to call with the server response upon execution
     *            completion.
     * @param procedure
     *            the name of the procedure to call.
     * @param parameters
     *            the list of parameters to pass to the procedure.
     * @return the result of the submission false if the client connection was terminated and unable
     *         to post the request to the server, true otherwise.
     */
    public boolean executeAsync(ProcedureCallback callback, String procedure, Object... parameters)
            throws NoConnectionsException, IOException {
        return this.client.callProcedure(new TrackingCallback(this, procedure, callback),
                procedure, parameters);
    }

    /**
     * Executes a procedure asynchronously, returning a Future that can be used by the caller to
     * wait upon completion before processing the server response.
     *
     * @param procedure
     *            the name of the procedure to call.
     * @param parameters
     *            the list of parameters to pass to the procedure.
     * @return the Future created to wrap around the asynchronous process.
     */
    public Future<ClientResponse> executeAsync(String procedure, Object... parameters)
            throws NoConnectionsException, IOException {
        final JDBC4ExecutionFuture future = new JDBC4ExecutionFuture(this.defaultAsyncTimeout);
        this.client.callProcedure(new TrackingCallback(this, procedure, new ProcedureCallback() {
            @SuppressWarnings("unused")
            final JDBC4ExecutionFuture result;
            {
                this.result = future;
            }

            @Override
            public void clientCallback(ClientResponse response) throws Exception {
                future.set(response);
            }
        }), procedure, parameters);
        return future;
    }

    /**
     * Gets the global performance statistics for this connection (and all connections with the same
     * parameters).
     *
     * @return the counter map aggregated across all the connections in the pool with the same
     *         parameters as this connection.
     */
    public JDBC4PerfCounterMap getStatistics() {
        return JDBC4ClientConnectionPool.getStatistics(this);
    }

    /**
     * Gets the performance statistics for a specific procedure on this connection (and all
     * connections with the same parameters).
     *
     * @param procedure
     *            the name of the procedure for which to retrieve the statistics.
     * @return the counter aggregated across all the connections in the pool with the same
     *         parameters as this connection.
     */
    public JDBC4PerfCounter getStatistics(String procedure) {
        return JDBC4ClientConnectionPool.getStatistics(this).get(procedure);
    }

    /**
     * Gets the aggregated performance statistics for a list of procedures on this connection (and
     * all connections with the same parameters).
     *
     * @param procedures
     *            the list of procedures for which to retrieve the statistics.
     * @return the counter aggregated across all the connections in the pool with the same
     *         parameters as this connection, and across all procedures.
     */
    public JDBC4PerfCounter getStatistics(String... procedures) {
        JDBC4PerfCounterMap map = JDBC4ClientConnectionPool.getStatistics(this);
        JDBC4PerfCounter result = new JDBC4PerfCounter(false);
        for (String procedure : procedures)
            result.merge(map.get(procedure));
        return result;
    }

    /**
     * Save statistics to a CSV file.
     *
     * @param file
     *            File path
     * @throws IOException
     */
    public void saveStatistics(String file) throws IOException {
        if (file != null && !file.trim().isEmpty()) {
            FileWriter fw = new FileWriter(file);
            fw.write(getStatistics().toRawString(','));
            fw.flush();
            fw.close();
        }
    }

    /**
     * Block the current thread until all queued stored procedure invocations have received
     * responses or there are no more connections to the cluster
     *
     * @throws NoConnectionsException
     * @throws InterruptedException
     * @see Client#drain()
     */
    public void drain() throws NoConnectionsException, InterruptedException {
        this.client.drain();
    }

    /**
     * Blocks the current thread until there is no more backpressure or there are no more
     * connections to the database
     *
     * @throws InterruptedException
     */
    public void backpressureBarrier() throws InterruptedException {
        this.client.backpressureBarrier();
    }

    /**
     * Synchronously invokes UpdateApplicationCatalog procedure. Blocks until a result is available.
     * A {@link ProcCallException} is thrown if the response is anything other then success.
     *
     * @param catalogPath
     *            Path to the catalog jar file.
     * @param deploymentPath
     *            Path to the deployment file
     * @return array of VoltTable results
     * @throws IOException
     *             If the files cannot be serialized
     * @throws NoConnectionException
     * @throws ProcCallException
     */
    public ClientResponse updateApplicationCatalog(File catalogPath, File deploymentPath)
            throws IOException, NoConnectionsException, ProcCallException {
        return this.client.updateApplicationCatalog(catalogPath, deploymentPath);
    }
}