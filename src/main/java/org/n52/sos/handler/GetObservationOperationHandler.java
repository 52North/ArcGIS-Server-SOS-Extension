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
import java.util.Arrays;
import java.util.List;
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
public class GetObservationOperationHandler extends OGCOperationRequestHandler {
	
	private static final String GET_OBSERVATION_OPERATION_NAME = "GetObservation";
	
    public static final String OM_PHENOMENON_TIME_LATEST = "om:phenomenonTime,latest";
	public static final String OM_PHENOMENON_TIME_FIRST = "om:phenomenonTime,first";
    
    private static final String VERSION_KEY = "version";
	private static final String OFFERING_KEY = "offering";
	private static final String FOI_KEY = "featureOfInterest";
	private static final String OBSERVED_PROPERTY_KEY = "observedProperty";
	private static final String PROCEDURE_KEY = "procedure";
	private static final String SPATIAL_FILTER_KEY = "spatialFilter";
	private static final String TEMPORAL_FILTER_KEY = "temporalFilter";
	private static final String RESPONSE_FORMAT_KEY = "responseFormat";
	private static final String AGGREGATION_TYPE = "aggregationType";
	
    private static List<String> supportedValueReferences = Arrays.asList(new String[] {
        	"om:phenomenonTime"	
        });

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
            String[] responseProperties) throws ExceptionReport
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
            spatialFilter = convertSpatialFilterFromOGCtoArcGisREST(spatialFilterOGC);
        }
        
        String temporalFilter = createTemporalFilter(inputObject);
        
        String responseFormat = null;
        if (inputObject.has(RESPONSE_FORMAT_KEY)) {
            responseFormat = inputObject.getString(RESPONSE_FORMAT_KEY);
        }
        
        String[] aggregationTypes = null;
        if (inputObject.has(AGGREGATION_TYPE)) {
        	aggregationTypes = inputObject.getString(AGGREGATION_TYPE).split(",");
        }
        
        String result;
           
        Map<String, MultiValueObservation> observationCollection;
		try {
			observationCollection = geoDB.getObservationAccess().getObservations(offerings, featuresOfInterest, observedProperties, procedures, spatialFilter, temporalFilter, aggregationTypes, null);
        
	        if (responseFormat != null && responseFormat.equalsIgnoreCase(Constants.RESPONSE_FORMAT_RDF)) {
	//        	constructInvokedURL(offerings, featuresOfInterest, observedProperties, procedures, spatialFilter, temporalFilter, responseFormat);
	            throw new UnsupportedOperationException("RDF not yet supported");
	//            result = new RDFEncoder(sosUrlExtension).getObservationCollectionTriples(observationCollection, invokedURL);
	        }
	        else if (responseFormat != null && responseFormat.equalsIgnoreCase(Constants.RESPONSE_FORMAT_AQ)) {
	            result = new AQDObservationEncoder().encodeObservations(observationCollection);
	        }
	        else if (responseFormat == null || responseFormat.equalsIgnoreCase(Constants.RESPONSE_FORMAT_OM)) {
	            result = new OGCObservationSWECommonEncoder().encodeObservations(observationCollection);
	        }
	        else {
	            throw new InvalidParameterValueException("Specified responseFormat '" + responseFormat + "' is unsupported. Please use either '"+Constants.RESPONSE_FORMAT_OM+"', '"+Constants.RESPONSE_FORMAT_AQ+"', or '"+Constants.RESPONSE_FORMAT_RDF+"'.");
	        }
	        
	        return result.getBytes("utf-8");
		} catch (IOException e) {
			throw new NoApplicableCodeException(e);
		}
    }


	private String createTemporalFilter(JSONObject inputObject)
			throws InvalidParameterValueException {
		String temporalFilter = null;
        if (inputObject.has(TEMPORAL_FILTER_KEY)) {
            String temporalFilterOGC = inputObject.getString(TEMPORAL_FILTER_KEY);
            
            if (temporalFilterOGC.equals(OM_PHENOMENON_TIME_FIRST) ||
            		temporalFilterOGC.equals(OM_PHENOMENON_TIME_LATEST)) {
            	return temporalFilterOGC;
            }
            
        	String[] params = temporalFilterOGC.split(",");
        	if (params.length != 2) {
        		throw new InvalidParameterValueException("The temporalFilter must consist of two comma separated values: valueReference,iso8601Time   OR   valueReference,<latest or first>");
        	}
        	
        	/*
        	 * TODO: once we decide to support other valueReferences we have to
        	 * come up with a better mechanism (see also AccessGdbForObservationsImpl.createTemporalClauseSDE(String))
        	 */
        	if (!supportedValueReferences.contains(params[0].trim())) {
        		throw new InvalidParameterValueException("The value reference "+params[0].trim()+" is currently not supported for temporalFilter");
        	}
            
            temporalFilter = convertTemporalFilterFromOGCtoArcGisREST(params[1].trim());
        }
		return temporalFilter;
	}

    protected String constructInvokedURL(String[] offerings,
			String[] featuresOfInterest, String[] observedProperties,
			String[] procedures, String spatialFilter, String temporalFilter, String responseFormat) {
        StringBuilder invokedURL = new StringBuilder();
        invokedURL.append(this.sosUrlExtension);
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
		StringBuilder sb = new StringBuilder();
		sb.append(offerings[0]);
		
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

	@Override
	public int getExecutionPriority() {
		return 0;
	}
}