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
package org.n52.sos.handler;

import java.io.IOException;
import java.util.Map;

import org.n52.om.observation.MultiValueObservation;
import org.n52.ows.ExceptionReport;
import org.n52.ows.InvalidParameterValueException;
import org.n52.ows.NoApplicableCodeException;
import org.n52.sos.Constants;
import org.n52.sos.db.AccessGDB;
import org.n52.sos.encoder.AQDObservationEncoder;
import org.n52.sos.encoder.OGCObservationSWECommonEncoder;

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
            String[] responseProperties) throws ExceptionReport
    {
        super.invokeOGCOperation(geoDB, inputObject, responseProperties);
        
        // keep track of the invokedURL (for possible RDF response):
//        String invokedURL = sosUrlExtension + "/GetObservation";
//        invokedURL += "?service=" + SERVICE;
//        invokedURL += "&request=" + getOperationName();
        
        // check 'version' parameter:
        checkMandatoryParameter(inputObject, "version", VERSION);
//        invokedURL += "&version=" + VERSION;
        
        String[] observationIDs = null;
        if (inputObject.has("observation")) {
            observationIDs = inputObject.getString("observation").split(",");
            
//            invokedURL += "&observation=" + inputObject.getString("observation");
        }
        
        String responseFormat = null;
        if (inputObject.has("responseFormat")) {
            responseFormat = inputObject.getString("responseFormat");
            
//            invokedURL += "&responseFormat=" + inputObject.getString("responseFormat");
        }
        
        String result = "";
           
        Map<String, MultiValueObservation> idObsMap;
		try {
			idObsMap = geoDB.getObservationAccess().getObservations(observationIDs);
        
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
	        }
	        else if (responseFormat == null || responseFormat.equalsIgnoreCase(Constants.RESPONSE_FORMAT_OM)) {
	            result = new OGCObservationSWECommonEncoder().encodeObservations(idObsMap);
	        }
	        else {
	            throw new InvalidParameterValueException("Specified responseFormat '" + responseFormat + "' is unsupported. Please use either '"+Constants.RESPONSE_FORMAT_OM+"', '"+Constants.RESPONSE_FORMAT_AQ+"', or '"+Constants.RESPONSE_FORMAT_RDF+"'.");
	        }
	        
	        return result.getBytes("utf-8");
		} catch (IOException e) {
			throw new NoApplicableCodeException(e);
		}
    }

	@Override
	protected String getOperationName() {
		return GET_OBSERVATION_BY_ID_OPERATION_NAME;
	}

	@Override
	public int getExecutionPriority() {
		return 2;
	}

}