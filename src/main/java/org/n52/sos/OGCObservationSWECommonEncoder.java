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

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.n52.om.observation.MultiValueObservation;
import org.n52.om.result.MeasureResult;
import org.n52.util.ExceptionSupporter;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class OGCObservationSWECommonEncoder extends AbstractEncoder {

    protected static Logger LOGGER = Logger.getLogger(OGCObservationSWECommonEncoder.class.getName());
    
    /*
     * definition of anchor variables within template files:
     */
    protected static String OBSERVATIONS = "@observations@";
    protected static String OBSERVATION_ID = "@observation-id@";
    protected static String OBSERVATION_PHENTIME_START = "@observation-phentime-start@";
    protected static String OBSERVATION_PHENTIME_END = "@observation-phentime-end@";
    protected static String OBSERVATION_PROCEDURE = "@observation-procedure@";
    protected static String OBSERVATION_PROPERTY = "@observation-property@";
    protected static String OBSERVATION_FEATURE = "@observation-feature@";
    protected static String OBSERVATION_SAMPLING_POINT = "@observation-sampling-point@";
    protected static String OBSERVATION_UNIT_ID = "@observation-unit-id@";
    protected static String OBSERVATION_UNIT_NOTATION = "@observation-unit-notation@";
    protected static String OBSERVATION_AGGREGATION_TYPE = "@observation-aggregation-type@";
    protected static String ELEMENT_COUNT = "@element-count@";
    protected static String VALUES = "@values@";

    protected String observationTemplateFile;
    protected String observationEnvelopeTemplateFile;
    
    public OGCObservationSWECommonEncoder() {
        observationTemplateFile = "template_om_observation_swe_common.xml";
        observationEnvelopeTemplateFile = "template_getobservation_response_OM.xml";
    }
    
    /**
     * This operation encodes the observations contained in the
     * idObsList parameter in the compact SWE Common format.
     * 
     * @param observationCollection
     * @return
     * @throws IOException
     */
    public String encodeObservations(Map<String, MultiValueObservation> idObsList) throws IOException
    {
        String encodedObservations = "";

        String observationTemplate = null;
        try {
            // read template for SWE Common Encoding:
            observationTemplate = readText(OGCObservationSWECommonEncoder.class.getResourceAsStream(observationTemplateFile));
        } catch (Exception e) {
            LOGGER.severe("There was a problem while reading the template: \n" + e.getLocalizedMessage() + "\n" + ExceptionSupporter.createStringFromStackTrace(e));
            throw new IOException(e);
        }
        
        Set<String> obsIdSet = idObsList.keySet();
        for (String obsId : obsIdSet) {

            MultiValueObservation multiValObs = idObsList.get(obsId);
        	
            String observation = observationTemplate;
            
            StringBuilder allValues = new StringBuilder();
            for (MeasureResult resultValue : multiValObs.getResult().getValue()) {
                allValues.append(encodeMeasureResult(resultValue));
            }
            
            observation = observation.replace(OBSERVATION_ID, multiValObs.getIdentifier().getIdentifierValue());
            observation = observation.replace(OBSERVATION_UNIT_ID, multiValObs.getUnit());
            observation = observation.replace(OBSERVATION_UNIT_NOTATION, multiValObs.getUnitNotation());
            observation = observation.replace(OBSERVATION_PHENTIME_START, multiValObs.getResult().getDateTimeBegin().toISO8601Format());
            observation = observation.replace(OBSERVATION_PHENTIME_END, multiValObs.getResult().getDateTimeEnd().toISO8601Format());
            observation = observation.replace(OBSERVATION_PROCEDURE, multiValObs.getProcedure());
            observation = observation.replace(OBSERVATION_PROPERTY, multiValObs.getObservedProperty());
            observation = observation.replace(OBSERVATION_FEATURE, multiValObs.getFeatureOfInterest());
            observation = observation.replace(OBSERVATION_SAMPLING_POINT, multiValObs.getSamplingPoint());
            observation = observation.replace(OBSERVATION_AGGREGATION_TYPE, multiValObs.getAggregationType());
            observation = observation.replace(ELEMENT_COUNT, "" + multiValObs.getResult().getValue().size());
            observation = observation.replace(VALUES, allValues);
            
            encodedObservations += observation;
        }
        return encodedObservations;
    }

    public String wrapInEnvelope(String result) throws IOException
    {
        String responseTemplate = readText(OGCObservationSWECommonEncoder.class.getResourceAsStream(observationEnvelopeTemplateFile));

        responseTemplate = responseTemplate.replace(OBSERVATIONS, result);

        return responseTemplate;
    }

    // /////////////////////////////////// helper methods:
    
    protected String encodeMeasureResult(MeasureResult resultValue)
    {
        String valueRow = 
                resultValue.getDateTimeBegin().toISO8601Format() + "," +
                resultValue.getDateTimeEnd().toISO8601Format() + "," + 
                resultValue.getValidity() + "," +
                resultValue.getVerification() + "," + 
                resultValue.getValue() + "@@";
        return valueRow;
    }

}
