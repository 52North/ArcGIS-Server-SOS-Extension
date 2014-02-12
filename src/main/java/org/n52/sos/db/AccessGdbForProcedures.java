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
package org.n52.sos.db;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.n52.sos.dataTypes.Procedure;

import com.esri.arcgis.interop.AutomationException;

public interface AccessGdbForProcedures {

	Collection<Procedure> getProceduresWithIdAndResource(String[] procedures) throws AutomationException, IOException;
	
	Collection<Procedure> getProceduresComplete(String[] procedures) throws AutomationException, IOException;

	List<String> getProcedureIdList() throws AutomationException, IOException;

	Collection<Procedure> getProceduresForNetwork(String networkID) throws IOException;

	boolean isNetwork(String procedure) throws AutomationException, IOException;

	boolean isProcedure(String procedure) throws AutomationException, IOException;
}
