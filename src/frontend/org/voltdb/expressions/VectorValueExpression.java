/* This file is part of VoltDB.
 * Copyright (C) 2008-2013 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.voltdb.expressions;

import org.json_voltpatches.JSONException;
import org.json_voltpatches.JSONObject;
import org.voltdb.catalog.Database;
import org.voltdb.types.ExpressionType;

/**
 * Represents a vector of expression trees.
 * Currently used for SQL IN lists (of values, not subqueries).
 */
public class VectorValueExpression extends AbstractExpression {

    public VectorValueExpression() {
        super(ExpressionType.VALUE_VECTOR);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof VectorValueExpression)) {
            return false;
        }

        VectorValueExpression other = (VectorValueExpression) obj;

        if (other.m_args.size() != m_args.size()) {
            return false;
        }

        for (int i = 0; i < m_args.size(); i++) {
            if (!other.m_args.get(i).equals(m_args.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void loadFromJSONObject(JSONObject obj, Database db) throws JSONException {
        // nothing used besides the m_args list in AbstractExpression
    }

    @Override
    public void finalizeValueTypes() {
        // just make sure the children have the
        finalizeChildValueTypes();
        // probably need to set them all to the same large type? ugh...
    }
}
