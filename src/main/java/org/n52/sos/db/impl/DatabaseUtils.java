/**
 * Copyright (C) 2012 52°North Initiative for Geospatial Open Source Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.sos.db.impl;

import java.io.IOException;

import org.n52.ows.ResponseExceedsSizeLimitException;
import org.n52.util.logging.Logger;

import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.interop.AutomationException;

public class DatabaseUtils {

	private static final Logger LOGGER = Logger.getLogger(DatabaseUtils.class
			.getName());

	public static void assertMaximumRecordCount(String tables,
			String whereClause, AccessGDBImpl geoDB)
			throws ResponseExceedsSizeLimitException {
		try {
			ICursor countCursor = evaluateQuery(tables, whereClause,
					"count(*)", geoDB);
			IRow row;
			if ((row = countCursor.nextRow()) != null) {
				Object value = row.getValue(0);
				if (value != null && value instanceof Integer) {
					if ((int) value > geoDB.getMaxNumberOfResults()) {
						throw new ResponseExceedsSizeLimitException(
								geoDB.getMaxNumberOfResults());
					}
				}
			}
		} catch (AutomationException e) {
			LOGGER.warn(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	public static ICursor evaluateQuery(String tables, String whereClause,
			String subFields, AccessGDBImpl geoDB) throws IOException,
			AutomationException {
		IQueryDef queryDef = geoDB.getWorkspace().createQueryDef();

		queryDef.setSubFields(subFields);
		LOGGER.debug("SELECT " + queryDef.getSubFields());

		queryDef.setTables(tables);
		LOGGER.debug("FROM " + queryDef.getTables());

		queryDef.setWhereClause(whereClause);
		LOGGER.debug("WHERE " + queryDef.getWhereClause());

		// evaluate the database query
		ICursor cursor = queryDef.evaluate();
		return cursor;
	}

}
