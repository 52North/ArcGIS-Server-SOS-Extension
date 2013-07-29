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

/**
 * @author Arne
 */
public class RDFEncoder {
    
	//TODO: RDFEncoder needs update to new AQ e-Reporting data model 
	
//    public static final String RDF_ROOT_START="<?xml version=\"1.0\"?>\n" +
//    		"<rdf:RDF \n" +
//    		"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" \n" +
//    		"xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" \n" +
//    		"xmlns:minioamld=\"http://52north.org/minionmld/\" \n" +
//    		"xmlns:dct=\"http://purl.org/dc/terms/\">\n";
//    public static final String RDF_ROOT_END="</rdf:RDF>";
//    
//    // RDF types:
//    private static final String OWL_LOCATION = "http://arnebroering.de/lod/miniOnM.owl"; 
//    private static final String RDF_FOI_TYPE = OWL_LOCATION + "#FeatureOfInterest";
//    private static final String RDF_OBS_TYPE = OWL_LOCATION + "#Observation";
//    private static final String RDF_OBSCOL_TYPE = OWL_LOCATION + "#ObservationCollection";
//    private static final String RDF_OBSPROP_TYPE = OWL_LOCATION + "#ObservedProperty";
//    private static final String RDF_RESULT_TYPE = OWL_LOCATION + "#Result";
//    private static final String RDF_SAMPLINGTIME_TYPE = OWL_LOCATION + "#SamplingTime";
//    private static final String RDF_SENSOR_TYPE = OWL_LOCATION + "#Sensor";
//    
//    // Base requests:
//    private String OBSERVATION_BY_ID_PREFIX;
//    private String SENSOR_OBSERVATIONS_PREFIX;
//    private String FEATURE_OBSERVATIONS_PREFIX;
//    private String RESULT_URL_PREFIX;
//    private String SAMPLINGTIMES_URL_PREFIX;
//
//    public RDFEncoder(String baseUrl) {
//        OBSERVATION_BY_ID_PREFIX    = baseUrl + "/GetObservationByID?service=SOS&version=2.0.0&request=GetObservationByID&responseFormat=" + GetObservationByIDOperationHandler.RESPONSE_FORMAT_RDF + "&observation=";
//        SENSOR_OBSERVATIONS_PREFIX  = baseUrl + "/observations/procedures/";
//        FEATURE_OBSERVATIONS_PREFIX = baseUrl + "/observations/featuresOfInterest/";
//        RESULT_URL_PREFIX           = baseUrl + "/results/";
//        SAMPLINGTIMES_URL_PREFIX    = baseUrl + "/samplingTimes/";
//    }
//    
//    /**
//     * Answer to a normal GetObservation URL. For example:
//     * http://myRESTfulSOS/observations
//     * /sensors/HR:0002A/samplingtimes/2008-01-01
//     * ,2008-12-31/observedproperties/concentration[NO2]
//     * 
//     * @param observationColl
//     * @param calledURL
//     * @return
//     */
//    public String getObservationCollectionTriples(GenericObservationCollection observationColl,
//            String calledURL)
//    {
//        String responseString = "";
//        responseString += RDF_ROOT_START;
//
//        String[] obsIds = new String[observationColl.size()];
//        for (int i = 0; i < observationColl.size(); i++) {
//            List<AbstractObservation> obsList = observationColl.getObservations();
//            AbstractObservation observation = obsList.get(i);
//            String obsID = observation.getIdentifier().getIdentifierValue();
//            obsIds[i] = OBSERVATION_BY_ID_PREFIX + obsID;
//        }
//
//        responseString += encodeObservationCollectionTriple(calledURL, observationColl.getGmlId(), obsIds);
//
//        responseString += RDF_ROOT_END;
//
//        return responseString;
//    }
//
//    /**
//     * Answer to a GetObservationById URL. For example:
//     * http://myRESTfulSOS/observations/ids/o_6543
//     * 
//     * @param observation
//     * @param calledURL
//     * @return
//     */
//    public String getObservationTriple(AbstractObservation observation,
//            String calledURL)
//    {
//        String observedPropertyURI = observation.getObservedProperty().toString();
//
//        String procedureURL = observation.getProcedure().toString();
//
//        IResult result = observation.getResult();
//        double resultValue = (Double) result.getValue();
//
//        String timeString = observation.getPhenomenonTime().toISO8601Format();
//
//        String obsId = observation.getIdentifier().getIdentifierValue();
//
//        String responseString = "";
//        responseString += RDF_ROOT_START;
//        responseString += encodeObservationTriple(
//                calledURL, 
//                obsId, 
//                observedPropertyURI, 
//                SAMPLINGTIMES_URL_PREFIX + timeString, 
//                procedureURL, 
//                RESULT_URL_PREFIX + resultValue);
//        responseString += RDF_ROOT_END;
//        return responseString;
//    }
//
//    /**
//     * Answer to a DescribeSensor URL. For example:
//     * http://myRESTfulSOS/sensors/HR:0002A.
//     * 
//     * @param calledURL
//     *            see above
//     * @param sensorShortID
//     *            for example: 'HR:0002A'
//     * @return
//     */
//    public String getSensorTriple(String calledURL,
//            String sensorShortID)
//    {
//        String responseString = "";
//        responseString += RDF_ROOT_START;
//        responseString += encodeSensorTriple(calledURL, calledURL, SENSOR_OBSERVATIONS_PREFIX + sensorShortID);
//        responseString += RDF_ROOT_END;
//        return responseString;
//    }
//
//    /**
//     * Answer to a GetFeatureOfInterest URL. For example:
//     * http://myRESTfulSOS/features/HR0002A.
//     * 
//     * @param calledURL
//     *            see above
//     * @param sensorShortID
//     *            for example: 'HR0002A'
//     * @return
//     */
//    public String getFeatureTriple(String calledURL,
//            String featureShortID)
//    {
//        String responseString = "";
//        responseString += RDF_ROOT_START;
//        responseString += encodeFoiTriple(calledURL, featureShortID, "UNKNOWN", FEATURE_OBSERVATIONS_PREFIX + featureShortID);
//        responseString += RDF_ROOT_END;
//        return responseString;
//    }
//
//    // ////////////////////////////////////////
//    // // HELPER METHODS:
//    // ////////////////////////////////////////
//
//    private String encodeFoiTriple(String foiUrl,
//            String foiLabel,
//            String obsPropUrl,
//            String relatedObsUrl)
//    {
//        return "<rdf:Description rdf:about=\"" + foiUrl + "\">\n"
//            + "<rdfs:label>" + foiLabel + "</rdfs:label>\n" 
//            + "<rdf:type rdf:resource=\"" + RDF_FOI_TYPE + "\"/>\n"
//            + "<minioamld:hasProperty rdf:resource=\"" + obsPropUrl + "\"/>\n" 
//            + "<minioamld:relatedObservations rdf:resource=\"" + relatedObsUrl + "\"/>\n"
//            + "</rdf:Description>\n";
//    }
//
//    private String encodeObservationTriple(String obsUrl,
//            String obsLabel,
//            String obsPropUrl,
//            String samplingTimeUrl,
//            String sensorUrl,
//            String resultUrl)
//    {
//        return "<rdf:Description rdf:about=\"" + obsUrl + "\">\n" 
//            + "<rdfs:label>" + obsLabel + "</rdfs:label>\n"
//            + "<rdf:type rdf:resource=\"" + RDF_OBS_TYPE + "\"/>\n"
//            + "<minioamld:aboutProperty rdf:resource=\"" + obsPropUrl + "\"/>\n"
//            + "<minioamld:samplingTime rdf:resource=\"" + samplingTimeUrl + "\"/>\n"
//            + "<minioamld:performedBy rdf:resource=\"" + sensorUrl + "\"/>\n"
//            + "<minioamld:hasResult rdf:resource=\"" + resultUrl + "\"/>\n"
//            + "</rdf:Description>\n";
//    }
//
//    private String encodeObservationCollectionTriple(String obsColUrl,
//            String obsColLabel,
//            String[] relatedObsURLs)
//    {
//        String result = "<rdf:Description rdf:about=\"" + obsColUrl + "\">\n" 
//                + "<rdfs:label>" + obsColLabel + "</rdfs:label>\n"
//                + "<rdf:type rdf:resource=\"" + RDF_OBSCOL_TYPE + "\"/>\n";
//        
//        for (int i = 0; i < relatedObsURLs.length; i++) {
//            result += "<minioamld:hasObservation rdf:resource=\"" + relatedObsURLs[i] + "\"/>\n";
//        }
//        result += "</rdf:Description>\n";
//        return result;
//    }
//
//    @SuppressWarnings("unused")
//    private String encodeObservedPropertyTriple(String obsPropUrl,
//            String obsPropLabel,
//            String foiUrl,
//            String relatedObsUrl)
//    {
//        return "<rdf:Description rdf:about=\"" + obsPropUrl + "\">" + "\n" + "<rdfs:label>" + obsPropLabel + "</rdfs:label>" + "\n" + "<rdf:type rdf:resource=\"" + RDF_OBSPROP_TYPE + "\"/>" + "\n"
//                + "<minioamld:isPropertyOf rdf:resource=\"" + foiUrl + "\"/>" + "\n" + "<minioamld:relatedObservations rdf:resource=\"" + relatedObsUrl + "\"/>" + "\n" + "</rdf:Description>" + "\n";
//    }
//
//    @SuppressWarnings("unused")
//    private String encodeResultTriple(String resultUrl,
//            String resultLabel,
//            String relatedObsUrl)
//    {
//        return "<rdf:Description rdf:about=\"" + resultUrl + "\">" + "\n" + "<rdfs:label>" + resultLabel + "</rdfs:label>" + "\n" + "<rdf:type rdf:resource=\"" + RDF_RESULT_TYPE + "\"/>" + "\n"
//                + "<minioamld:relatedObservations rdf:resource=\"" + relatedObsUrl + "\"/>" + "\n" + "</rdf:Description>" + "\n";
//    }
//
//    @SuppressWarnings("unused")
//    private String encodeSamplingTimeTriple(String stUrl,
//            String stLabel,
//            String relatedObsUrl)
//    {
//        return "<rdf:Description rdf:about=\"" + stUrl + "\">" + "\n" + "<rdfs:label>" + stLabel + "</rdfs:label>" + "\n" + "<rdf:type rdf:resource=\"" + RDF_SAMPLINGTIME_TYPE + "\"/>" + "\n"
//                + "<minioamld:relatedObservations rdf:resource=\"" + relatedObsUrl + "\"/>" + "\n" + "</rdf:Description>" + "\n";
//    }
//
//    private String encodeSensorTriple(String sensorUrl,
//            String sensorLabel,
//            String relatedObsUrl)
//    {
//        return "<rdf:Description rdf:about=\"" + sensorUrl + "\">" + "\n" + "<rdfs:label>" + sensorLabel + "</rdfs:label>" + "\n" + "<rdf:type rdf:resource=\"" + RDF_SENSOR_TYPE + "\"/>" + "\n"
//                + "<minioamld:relatedObservations rdf:resource=\"" + relatedObsUrl + "\"/>" + "\n" + "</rdf:Description>" + "\n";
//    }
}
