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
package org.n52.sos;

import java.util.Map;

import org.n52.om.observation.MultiValueObservation;
import org.n52.sos.db.AccessGDB;

import com.esri.arcgis.server.json.JSONObject;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class GetObservationOperationHandler extends OGCOperationRequestHandler {
    
    public GetObservationOperationHandler(String urlSosExtension) {
        super(urlSosExtension);
        OPERATION_NAME = "GetObservation";
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
        String invokedURL = this.sosUrlExtension + "/GetObservation";
        invokedURL += "?service=" + SERVICE;
        invokedURL += "&request=" + OPERATION_NAME;
        
        // check 'version' parameter:
        checkMandatoryParameter(inputObject, "version", VERSION);
        invokedURL += "&version=" + VERSION;
        
        String[] offerings = null;
        if (inputObject.has("offering")) {
            offerings = inputObject.getString("offering").split(",");
            
            invokedURL += "&offering=" + inputObject.getString("offering");
        }
        String[] featuresOfInterest = null;
        if (inputObject.has("featureOfInterest")) {
            featuresOfInterest = inputObject.getString("featureOfInterest").split(",");
            
            invokedURL += "&featureOfInterest=" + inputObject.getString("featureOfInterest");
        }
        String[] observedProperties = null;
        if (inputObject.has("observedProperty")) {
            observedProperties = inputObject.getString("observedProperty").split(",");
            
            invokedURL += "&observedProperty=" + inputObject.getString("observedProperty");
        }
        String[] procedures = null;
        if (inputObject.has("procedure")) {
            procedures = inputObject.getString("procedure").split(",");
            
            invokedURL += "&procedure=" + inputObject.getString("procedure");
        }
        String spatialFilter = null;
        if (inputObject.has("spatialFilter")) {
            String spatialFilterOGC = inputObject.getString("spatialFilter");
            spatialFilter = convertSpatialFilterFromOGCtoESRI(spatialFilterOGC);
            
            invokedURL += "&spatialFilter=" + inputObject.getString("spatialFilter");
        }
        String temporalFilter = null;
        if (inputObject.has("temporalFilter")) {
            String temporalFilterOGC = inputObject.getString("temporalFilter");
            temporalFilter = convertTemporalFilterFromOGCtoESRI(temporalFilterOGC);
            
            invokedURL += "&temporalFilter=" + inputObject.getString("temporalFilter");
        }
        
        String responseFormat = null;
        if (inputObject.has("responseFormat")) {
            responseFormat = inputObject.getString("responseFormat");
            
            invokedURL += "&responseFormat=" + inputObject.getString("responseFormat");
        }
        
        String result = "";
           
        Map<String, MultiValueObservation> observationCollection = geoDB.getObservationAccess().getObservations(offerings, featuresOfInterest, observedProperties, procedures, spatialFilter, temporalFilter, null);
        
        if (responseFormat != null && responseFormat.equalsIgnoreCase(Constants.RESPONSE_FORMAT_RDF)) {
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
}