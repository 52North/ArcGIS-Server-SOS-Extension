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

import com.esri.arcgis.datasourcesGDB.SqlWorkspace;
import com.esri.arcgis.geodatabase.Workspace;

public class WorkspaceWrapper {

	private SqlWorkspace sqlWorkspace;
	private Workspace workspace;

	public void setSqlWorkspace(SqlWorkspace workspace) {
		this.sqlWorkspace = workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
	
	public boolean usesSqlWorkspace() {
		return this.sqlWorkspace != null;
	}

	public SqlWorkspace getSqlWorkspace() {
		return sqlWorkspace;
	}

	public Workspace getWorkspace() {
		return workspace;
	}
	
	

}
