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

import org.n52.om.observation.MultiValueObservation;
import org.n52.om.result.MeasureResult;
import org.n52.util.CommonUtilities;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AQDObservationEncoder extends AbstractEncoder {

    /*
     * definition of anchor variables within template files:
     */
    private static String OBSERVATIONS = "@observations@";

    private static String OBSERVATION_ID = "@observation-id@";

    private static String OBSERVATION_PHENTIME_START = "@observation-phentime-start@";

    private static String OBSERVATION_PHENTIME_END = "@observation-phentime-end@";

    private static String OBSERVATION_PROCEDURE = "@observation-procedure@";

    private static String OBSERVATION_PROPERTY = "@observation-property@";

    private static String OBSERVATION_FEATURE = "@observation-feature@";

    private static String OBSERVATION_SAMPLING_POINT = "@observation-sampling-point@";
    
    private static String OBSERVATION_UCUM = "@observation-ucum@";

    private static String ELEMENT_COUNT = "@element-count@";

    private static String VALUES = "@values@";

    /**
     * This operation encodes the observations contained in the
     * idObsList parameter in the compact SWE Common format.
     * 
     * @param observationCollection
     * @return
     * @throws IOException
     */
    public static String encodeObservations(Map<String, MultiValueObservation> idObsList) throws IOException
    {
        String encodedObservations = "";

        // read template for SWE Common Encoding:
        String observationTemplate = readText(AQDObservationEncoder.class.getResourceAsStream("template_aqd_observation.xml"));
        
        Set<String> obsIdSet = idObsList.keySet();
        for (String obsId : obsIdSet) {

            MultiValueObservation multiValObs = idObsList.get(obsId);
        	
            String observation = observationTemplate;
            
            StringBuffer allValues = new StringBuffer();
            for (MeasureResult resultValue : multiValObs.getResult().getValue()) {
                allValues.append(encodeMeasureResult(resultValue));
            }
            
            observation = observation.replace(OBSERVATION_ID, multiValObs.getIdentifier().getIdentifierValue());
            observation = observation.replace(OBSERVATION_UCUM, multiValObs.getUnit());
            observation = observation.replace(OBSERVATION_PHENTIME_START, multiValObs.getResult().getDateTimeBegin().toISO8601Format());
            observation = observation.replace(OBSERVATION_PHENTIME_END, multiValObs.getResult().getDateTimeEnd().toISO8601Format());
            observation = observation.replace(OBSERVATION_PROCEDURE, multiValObs.getProcedure());
            observation = observation.replace(OBSERVATION_PROPERTY, multiValObs.getObservedProperty());
            observation = observation.replace(OBSERVATION_FEATURE, multiValObs.getFeatureOfInterest());
            observation = observation.replace(OBSERVATION_SAMPLING_POINT, multiValObs.getSamplingPoint());
            observation = observation.replace(ELEMENT_COUNT, "" + multiValObs.getResult().getValue().size());
            observation = observation.replace(VALUES, allValues);
            
            encodedObservations += observation;
        }
        return encodedObservations;
    }

    public static String wrapInEnvelope(String result) throws IOException
    {
        String responseTemplate = readText(AQDObservationEncoder.class.getResourceAsStream("template_getobservation_response_AQD.xml"));

        responseTemplate = responseTemplate.replace(OBSERVATIONS, result);

        return responseTemplate;
    }

    // /////////////////////////////////// helper methods:
    
    private static String encodeMeasureResult(MeasureResult resultValue)
    {
        String valueRow = resultValue.getDateTimeEnd().toISO8601Format() + "," + 
                resultValue.getValidity() + "," +
                resultValue.getVerification() + "," + 
                resultValue.getValue() + "@@";
        return valueRow;
    }

}
