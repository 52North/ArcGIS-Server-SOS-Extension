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

import java.util.Map;

import org.n52.om.observation.MultiValueObservation;
import org.n52.sos.AQDObservationEncoder;
import org.n52.sos.Constants;
import org.n52.sos.OGCObservationSWECommonEncoder;
import org.n52.sos.OGCOperationRequestHandler;
import org.n52.sos.db.AccessGDB;

import com.esri.arcgis.server.json.JSONObject;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class GetObservationByIDOperationHandler extends OGCOperationRequestHandler {
	
	private static final String GET_OBSERVATION_BY_ID_OPERATION_NAME = "GetObservationByID";
    
    public GetObservationByIDOperationHandler() {
        super();
    }

    /**
     * 
     * @param inputObject
     * @return
     * @throws Exception
     */
    public byte[] invokeOGCOperation(AccessGDB geoDB, JSONObject inputObject,
            String[] responseProperties) throws Exception
    {
        super.invokeOGCOperation(geoDB, inputObject, responseProperties);
        
        // keep track of the invokedURL (for possible RDF response):
        String invokedURL = sosUrlExtension + "/GetObservation";
        invokedURL += "?service=" + SERVICE;
        invokedURL += "&request=" + getOperationName();
        
        // check 'version' parameter:
        checkMandatoryParameter(inputObject, "version", VERSION);
        invokedURL += "&version=" + VERSION;
        
        String[] observationIDs = null;
        if (inputObject.has("observation")) {
            observationIDs = inputObject.getString("observation").split(",");
            
            invokedURL += "&observation=" + inputObject.getString("observation");
        }
        
        String responseFormat = null;
        if (inputObject.has("responseFormat")) {
            responseFormat = inputObject.getString("responseFormat");
            
            invokedURL += "&responseFormat=" + inputObject.getString("responseFormat");
        }
        
        String result = "";
           
        Map<String, MultiValueObservation> idObsMap = geoDB.getObservationAccess().getObservations(observationIDs);
        
        if (responseFormat != null && responseFormat.equalsIgnoreCase(Constants.RESPONSE_FORMAT_RDF)) {
            throw new UnsupportedOperationException("RDF not yet supported");
//            if (idObsMap.size() == 1) {
//                
//                idObsMap.
//                
//                AbstractObservation observation = observationCollection.getObservations().get(0);
//                result = new RDFEncoder(sosUrlExtension).getObservationTriple(observation, invokedURL);
//            }
//            else {
//                result = new RDFEncoder(sosUrlExtension).getObservationCollectionTriples(observationCollection, invokedURL);
//            }
        }
        else if (responseFormat != null && responseFormat.equalsIgnoreCase(Constants.RESPONSE_FORMAT_AQ)) {
            result = new AQDObservationEncoder().encodeObservations(idObsMap);
            result = new AQDObservationEncoder().wrapInEnvelope(result);
        }
        else if (responseFormat == null || responseFormat.equalsIgnoreCase(Constants.RESPONSE_FORMAT_OM)) {
            result = new OGCObservationSWECommonEncoder().encodeObservations(idObsMap);
            result = new OGCObservationSWECommonEncoder().wrapInEnvelope(result);
        }
        else {
            throw new IllegalArgumentException("Specified responseFormat '" + responseFormat + "' is unsupported. Please use either '"+Constants.RESPONSE_FORMAT_OM+"', '"+Constants.RESPONSE_FORMAT_AQ+"', or '"+Constants.RESPONSE_FORMAT_RDF+"'.");
        }
        
        return result.getBytes("utf-8");
    }

	@Override
	protected String getOperationName() {
		return GET_OBSERVATION_BY_ID_OPERATION_NAME;
	}

}