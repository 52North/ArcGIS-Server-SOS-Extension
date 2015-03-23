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

import com.esri.arcgis.datasourcesGDB.SqlWorkspace;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.interop.AutomationException;

public class DatabaseUtils {

	private static final Logger LOGGER = Logger.getLogger(DatabaseUtils.class
			.getName());

	public static int assertMaximumRecordCount(String tables,
			String whereClause, AccessGDBImpl geoDB)
			throws ResponseExceedsSizeLimitException {
		int value = resolveRecordCount(tables, whereClause, geoDB);
		
		if (value > geoDB.getMaxNumberOfResults()) {
			throw new ResponseExceedsSizeLimitException(
					geoDB.getMaxNumberOfResults(), value);
		}
		
		return value;
	}
	
	public static int resolveRecordCount(String tables,
			String whereClause, AccessGDBImpl gdb) {
		try {
			ICursor countCursor = evaluateQuery(tables, whereClause,
					"count(*)", gdb);
			IRow row;
			if ((row = countCursor.nextRow()) != null) {
				Object value = row.getValue(0);
				if (value != null && value instanceof Integer) {
					return (int) value;
				}
			}
		} catch (AutomationException e) {
			LOGGER.warn(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.warn(e.getMessage(), e);
		}
		
		return 0;
	}

	public static synchronized ICursor evaluateQuery(String tables, String whereClause,
			String subFields, AccessGDBImpl gdb, boolean logAtInfoLevel) throws IOException {
		return evaluateQuery(tables, whereClause, subFields, gdb.getWorkspace(), logAtInfoLevel);
	}
	
	public static synchronized ICursor evaluateQuery(String tables, String whereClause,
			String subFields, AccessGDBImpl gdb) throws IOException {
		return evaluateQuery(tables, whereClause, subFields, gdb.getWorkspace(), false);
	}

	public static synchronized ICursor evaluateQuery(String tables, String whereClause,
			String subFields, WorkspaceWrapper workspace) throws IOException {
		return evaluateQuery(tables, whereClause, subFields, workspace, false);
	}
	
	public static synchronized ICursor evaluateQuery(String tables, String whereClause,
			String subFields, WorkspaceWrapper workspace, boolean logAtInfoLevel) throws IOException {
		
		if (workspace.usesSqlWorkspace()) {
			return evaluateSqlWorkspaceQuery(tables, whereClause, subFields,
					workspace.getSqlWorkspace(),
					logAtInfoLevel);
		}
		
		IQueryDef queryDef;
		queryDef = workspace.getWorkspace().createQueryDef();

		queryDef.setSubFields(subFields);
		if (logAtInfoLevel) {
			LOGGER.info("SELECT " + queryDef.getSubFields());
		}
		else {
			LOGGER.debug("SELECT " + queryDef.getSubFields());
		}

		queryDef.setTables(tables);
		if (logAtInfoLevel) {
			LOGGER.info("FROM " + queryDef.getTables());
		}
		else {
			LOGGER.debug("FROM " + queryDef.getTables());
		}

		if (whereClause != null && !whereClause.isEmpty()) {
			queryDef.setWhereClause(whereClause);
			if (logAtInfoLevel) {
				LOGGER.info("WHERE " + queryDef.getWhereClause());
			}
			else {
				LOGGER.debug("WHERE " + queryDef.getWhereClause());			
			}
		}
		
		// evaluate the database query
		ICursor cursor = queryDef.evaluate();
		return cursor;
	}

	private static ICursor evaluateSqlWorkspaceQuery(String tables,
			String whereClause, String subFields, SqlWorkspace workspace,
			boolean logAtInfoLevel) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(subFields);
		
		sb.append(" FROM ");
		sb.append(tables);
		
		if (whereClause != null && !whereClause.trim().isEmpty()) {
			sb.append(" WHERE ");
			sb.append(whereClause);	
		}
		
		if (logAtInfoLevel) {
			LOGGER.info(sb.toString());
		}
		else {
			LOGGER.debug(sb.toString());
		}
		
		return workspace.openQueryCursor(sb.toString());
	}

}
