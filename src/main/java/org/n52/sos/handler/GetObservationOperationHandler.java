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
public class GetObservationOperationHandler extends OGCOperationRequestHandler {
	
	private static final String GET_OBSERVATION_OPERATION_NAME = "GetObservation";
    
    private static final String VERSION_KEY = "version";
	private static final String OFFERING_KEY = "offering";
	private static final String FOI_KEY = "featureOfInterest";
	private static final String OBSERVED_PROPERTY_KEY = "observedProperty";
	private static final String PROCEDURE_KEY = "procedure";
	private static final String SPATIAL_FILTER_KEY = "spatialFilter";
	private static final String TEMPORAL_FILTER_KEY = "temporalFilter";
	private static final String RESPONSE_FORMAT_KEY = "responseFormat";

	public GetObservationOperationHandler() {
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
        
        // check 'version' parameter:
        checkMandatoryParameter(inputObject, VERSION_KEY, VERSION);
        
        String[] offerings = null;
        if (inputObject.has(OFFERING_KEY)) {
            offerings = inputObject.getString(OFFERING_KEY).split(",");
        }
        String[] featuresOfInterest = null;
        if (inputObject.has(FOI_KEY)) {
            featuresOfInterest = inputObject.getString(FOI_KEY).split(",");
        }
        String[] observedProperties = null;
        if (inputObject.has(OBSERVED_PROPERTY_KEY)) {
            observedProperties = inputObject.getString(OBSERVED_PROPERTY_KEY).split(",");
        }
        String[] procedures = null;
        if (inputObject.has(PROCEDURE_KEY)) {
            procedures = inputObject.getString(PROCEDURE_KEY).split(",");
        }
        String spatialFilter = null;
        if (inputObject.has(SPATIAL_FILTER_KEY)) {
            String spatialFilterOGC = inputObject.getString(SPATIAL_FILTER_KEY);
            spatialFilter = convertSpatialFilterFromOGCtoESRI(spatialFilterOGC);
        }
        String temporalFilter = null;
        if (inputObject.has(TEMPORAL_FILTER_KEY)) {
            String temporalFilterOGC = inputObject.getString(TEMPORAL_FILTER_KEY);
            temporalFilter = convertTemporalFilterFromOGCtoESRI(temporalFilterOGC);
        }
        
        String responseFormat = null;
        if (inputObject.has(RESPONSE_FORMAT_KEY)) {
            responseFormat = inputObject.getString(RESPONSE_FORMAT_KEY);
        }
        
        String result;
           
        Map<String, MultiValueObservation> observationCollection = geoDB.getObservationAccess().getObservations(offerings, featuresOfInterest, observedProperties, procedures, spatialFilter, temporalFilter, null);
        
        if (responseFormat != null && responseFormat.equalsIgnoreCase(Constants.RESPONSE_FORMAT_RDF)) {
        	constructInvokedURL(offerings, featuresOfInterest, observedProperties, procedures, spatialFilter, temporalFilter, responseFormat);
            throw new UnsupportedOperationException("RDF not yet supported");
//            result = new RDFEncoder(sosUrlExtension).getObservationCollectionTriples(observationCollection, invokedURL);
        }
        else if (responseFormat != null && responseFormat.equalsIgnoreCase(Constants.RESPONSE_FORMAT_AQ)) {
            result = new AQDObservationEncoder().encodeObservations(observationCollection);
            result = new AQDObservationEncoder().wrapInEnvelope(result);
        }
        else if (responseFormat == null || responseFormat.equalsIgnoreCase(Constants.RESPONSE_FORMAT_OM)) {
            result = new OGCObservationSWECommonEncoder().encodeObservations(observationCollection);
            result = new OGCObservationSWECommonEncoder().wrapInEnvelope(result);
        }
        else {
            throw new IllegalArgumentException("Specified responseFormat '" + responseFormat + "' is unsupported. Please use either '"+Constants.RESPONSE_FORMAT_OM+"', '"+Constants.RESPONSE_FORMAT_AQ+"', or '"+Constants.RESPONSE_FORMAT_RDF+"'.");
        }
        
        return result.getBytes("utf-8");
    }

    private String constructInvokedURL(String[] offerings,
			String[] featuresOfInterest, String[] observedProperties,
			String[] procedures, String spatialFilter, String temporalFilter, String responseFormat) {
        StringBuilder invokedURL = new StringBuilder(this.sosUrlExtension);
        invokedURL.append("/GetObservation");
        invokedURL.append("?service=").append(SERVICE);
        invokedURL.append("&request=").append(getOperationName());
        
        invokedURL.append("&version=").append(VERSION);
        
        if (offerings != null) {
            invokedURL.append("&offering=").append(createCommaSeperatedList(offerings));
        }
        if (featuresOfInterest != null) {
            invokedURL.append("&featureOfInterest=").append(createCommaSeperatedList(featuresOfInterest));
        }
        if (observedProperties != null) {
            invokedURL.append("&observedProperty=").append(createCommaSeperatedList(observedProperties));
        }
        if (procedures != null) {
            invokedURL.append("&procedure=").append(createCommaSeperatedList(procedures));
        }
        if (spatialFilter != null) {
            invokedURL.append("&spatialFilter=").append(spatialFilter);
        }
        if (temporalFilter != null) {
            invokedURL.append("&temporalFilter=").append(temporalFilter);
        }
        
        if (responseFormat != null) {
            invokedURL.append("&responseFormat=").append(responseFormat);
        }
        
        return invokedURL.toString();
	}

	private String createCommaSeperatedList(String[] offerings) {
		StringBuilder sb = new StringBuilder(offerings[0]);
		
		if (offerings.length == 1) return sb.toString();
		
		for (int i = 1; i < offerings.length-1; i++) {
			sb.append(offerings[i]);
			sb.append(",");
		}
		
		sb.append(offerings[offerings.length-1]);
		
		return sb.toString();
	}

	public static void main(String[] args)
    {
        String[] offerings = new String[]{"o1", "o2"};
    
        String [] offeringsNew = new String[offerings.length + 1];
        System.arraycopy(offerings, 0, offeringsNew, 0, offerings.length);
        offeringsNew[offeringsNew.length - 1] = null;
        
        for (int offeringIndex = 0; offeringIndex < offeringsNew.length; offeringIndex++) {
            System.out.println(offeringsNew[offeringIndex]);
        }
    }

	@Override
	protected String getOperationName() {
		return GET_OBSERVATION_OPERATION_NAME;
	}
}