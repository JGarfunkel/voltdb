//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.10.17 at 06:12:41 PM EDT 
//


package org.voltdb.compiler.deploymentfile;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deploymentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deploymentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="cluster" type="{}clusterType"/>
 *         &lt;element name="paths" type="{}pathsType" minOccurs="0"/>
 *         &lt;element name="partition-detection" type="{}partitionDetectionType" minOccurs="0"/>
 *         &lt;element name="admin-mode" type="{}adminModeType" minOccurs="0"/>
 *         &lt;element name="heartbeat" type="{}heartbeatType" minOccurs="0"/>
 *         &lt;element name="httpd" type="{}httpdType" minOccurs="0"/>
 *         &lt;element name="snapshot" type="{}snapshotType" minOccurs="0"/>
 *         &lt;element name="export" type="{}exportType" minOccurs="0"/>
 *         &lt;element name="users" type="{}usersType" minOccurs="0"/>
 *         &lt;element name="commandlog" type="{}commandLogType" minOccurs="0"/>
 *         &lt;element name="systemsettings" type="{}systemSettingsType" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deploymentType", propOrder = {

})
public class DeploymentType {

    @XmlElement(required = true)
    protected ClusterType cluster;
    protected PathsType paths;
    @XmlElement(name = "partition-detection")
    protected PartitionDetectionType partitionDetection;
    @XmlElement(name = "admin-mode")
    protected AdminModeType adminMode;
    protected HeartbeatType heartbeat;
    protected HttpdType httpd;
    protected SnapshotType snapshot;
    protected ExportType export;
    protected UsersType users;
    protected CommandLogType commandlog;
    protected SystemSettingsType systemsettings;

    /**
     * Gets the value of the cluster property.
     * 
     * @return
     *     possible object is
     *     {@link ClusterType }
     *     
     */
    public ClusterType getCluster() {
        return cluster;
    }

    /**
     * Sets the value of the cluster property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClusterType }
     *     
     */
    public void setCluster(ClusterType value) {
        this.cluster = value;
    }

    /**
     * Gets the value of the paths property.
     * 
     * @return
     *     possible object is
     *     {@link PathsType }
     *     
     */
    public PathsType getPaths() {
        return paths;
    }

    /**
     * Sets the value of the paths property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathsType }
     *     
     */
    public void setPaths(PathsType value) {
        this.paths = value;
    }

    /**
     * Gets the value of the partitionDetection property.
     * 
     * @return
     *     possible object is
     *     {@link PartitionDetectionType }
     *     
     */
    public PartitionDetectionType getPartitionDetection() {
        return partitionDetection;
    }

    /**
     * Sets the value of the partitionDetection property.
     * 
     * @param value
     *     allowed object is
     *     {@link PartitionDetectionType }
     *     
     */
    public void setPartitionDetection(PartitionDetectionType value) {
        this.partitionDetection = value;
    }

    /**
     * Gets the value of the adminMode property.
     * 
     * @return
     *     possible object is
     *     {@link AdminModeType }
     *     
     */
    public AdminModeType getAdminMode() {
        return adminMode;
    }

    /**
     * Sets the value of the adminMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdminModeType }
     *     
     */
    public void setAdminMode(AdminModeType value) {
        this.adminMode = value;
    }

    /**
     * Gets the value of the heartbeat property.
     * 
     * @return
     *     possible object is
     *     {@link HeartbeatType }
     *     
     */
    public HeartbeatType getHeartbeat() {
        return heartbeat;
    }

    /**
     * Sets the value of the heartbeat property.
     * 
     * @param value
     *     allowed object is
     *     {@link HeartbeatType }
     *     
     */
    public void setHeartbeat(HeartbeatType value) {
        this.heartbeat = value;
    }

    /**
     * Gets the value of the httpd property.
     * 
     * @return
     *     possible object is
     *     {@link HttpdType }
     *     
     */
    public HttpdType getHttpd() {
        return httpd;
    }

    /**
     * Sets the value of the httpd property.
     * 
     * @param value
     *     allowed object is
     *     {@link HttpdType }
     *     
     */
    public void setHttpd(HttpdType value) {
        this.httpd = value;
    }

    /**
     * Gets the value of the snapshot property.
     * 
     * @return
     *     possible object is
     *     {@link SnapshotType }
     *     
     */
    public SnapshotType getSnapshot() {
        return snapshot;
    }

    /**
     * Sets the value of the snapshot property.
     * 
     * @param value
     *     allowed object is
     *     {@link SnapshotType }
     *     
     */
    public void setSnapshot(SnapshotType value) {
        this.snapshot = value;
    }

    /**
     * Gets the value of the export property.
     * 
     * @return
     *     possible object is
     *     {@link ExportType }
     *     
     */
    public ExportType getExport() {
        return export;
    }

    /**
     * Sets the value of the export property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExportType }
     *     
     */
    public void setExport(ExportType value) {
        this.export = value;
    }

    /**
     * Gets the value of the users property.
     * 
     * @return
     *     possible object is
     *     {@link UsersType }
     *     
     */
    public UsersType getUsers() {
        return users;
    }

    /**
     * Sets the value of the users property.
     * 
     * @param value
     *     allowed object is
     *     {@link UsersType }
     *     
     */
    public void setUsers(UsersType value) {
        this.users = value;
    }

    /**
     * Gets the value of the commandlog property.
     * 
     * @return
     *     possible object is
     *     {@link CommandLogType }
     *     
     */
    public CommandLogType getCommandlog() {
        return commandlog;
    }

    /**
     * Sets the value of the commandlog property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandLogType }
     *     
     */
    public void setCommandlog(CommandLogType value) {
        this.commandlog = value;
    }

    /**
     * Gets the value of the systemsettings property.
     * 
     * @return
     *     possible object is
     *     {@link SystemSettingsType }
     *     
     */
    public SystemSettingsType getSystemsettings() {
        return systemsettings;
    }

    /**
     * Sets the value of the systemsettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link SystemSettingsType }
     *     
     */
    public void setSystemsettings(SystemSettingsType value) {
        this.systemsettings = value;
    }

}
