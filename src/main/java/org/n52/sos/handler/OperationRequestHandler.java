/*
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 * 
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.sos.handler;

import org.n52.sos.db.AccessGDB;

import com.esri.arcgis.server.json.JSONObject;

/**
 * Interface for all operation handlers.
 * 
 * @author matthes rieke
 *
 */
public interface OperationRequestHandler {

	/**
	 * An implementation shall return true if it
	 * supports this operation.
	 * 
	 * @param operationName the operation by name
	 * @return true if the handler supports the given operation by name
	 */
	public boolean canHandle(String operationName);

	/**
	 * A service shall execute its operation and return
	 * the response (encoded as defined within the request) as
	 * a byte array. 
	 * 
	 * @param geoDB the access to the underlying database
	 * @param inputObject the inputObject defining the request
	 * @param responseProperties responseProperties which the ArcGIS server requires
	 * to provide correct HTTP headers
	 * @return the response (encoded as defined within the request)
	 * @throws Exception if something bad happens
	 */
	public byte[] invokeOGCOperation(AccessGDB geoDB, JSONObject inputObject,
			String[] responseProperties) throws Exception;

	/**
	 * set the public url for the extension
	 * @param urlSosExtension the public URL
	 */
	void setSosUrlExtension(String urlSosExtension);

}
