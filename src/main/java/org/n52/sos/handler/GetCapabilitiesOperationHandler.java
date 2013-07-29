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

import java.util.Collection;

import org.n52.sos.OGCCapabilitiesEncoder;
import org.n52.sos.OGCOperationRequestHandler;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.dataTypes.ServiceDescription;
import org.n52.sos.db.AccessGDB;

import com.esri.arcgis.server.json.JSONObject;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class GetCapabilitiesOperationHandler extends OGCOperationRequestHandler {
	
	private static final String GET_CAPABILITIES_OPERATION_NAME = "GetCapabilities";

    public GetCapabilitiesOperationHandler() {
        super();
    }
    
    /**
     * 
     * @param inputObject
     * @return
     * @throws Exception
     */
    public byte[] invokeOGCOperation(AccessGDB geoDB, JSONObject inputObject, String[] responseProperties) throws Exception
    {
        super.invokeOGCOperation(geoDB, inputObject, responseProperties);
        
//        String[] acceptVersions = null;
//        if (inputObject.has("AcceptVersions")) {
//            acceptVersions = inputObject.getString("AcceptVersions").split(",");
//        }
        
        ServiceDescription serviceDesc = geoDB.getServiceDescription();
        Collection<ObservationOffering> obsOfferings = geoDB.getOfferingAccess().getNetworksAsObservationOfferings();
        
        String capabilitiesDocument = new OGCCapabilitiesEncoder().encodeCapabilities(serviceDesc, obsOfferings);
                
        // sending the Capabilities document:
        LOGGER.info("Returning capabilities document.");
        return capabilitiesDocument.getBytes("utf-8");
    }

	@Override
	protected String getOperationName() {
		return GET_CAPABILITIES_OPERATION_NAME;
	} 
    
}
