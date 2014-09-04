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
package org.n52.sos.encoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.om.observation.MultiValueObservation;
import org.n52.om.result.MeasureResult;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.util.logging.Logger;

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

    private static String observationTemplate;
	private static String observationEnvelopeTemplate;
    
    
    public OGCObservationSWECommonEncoder() throws IOException {
		observationTemplate = readText(OGCObservationSWECommonEncoder.class.getResourceAsStream("template_om_observation_swe_common.xml"));
		observationEnvelopeTemplate = readText(OGCObservationSWECommonEncoder.class.getResourceAsStream("template_getobservation_response_OM.xml"));
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
        StringBuilder encodedObservations = new StringBuilder();

        List<ITimePosition> startTimes = new ArrayList<ITimePosition>();
        List<ITimePosition> endTimes = new ArrayList<ITimePosition>();
        
        Set<String> obsIdSet = idObsList.keySet();
        for (String obsId : obsIdSet) {

            MultiValueObservation multiValObs = idObsList.get(obsId);
        	
            StringBuilder observation = new StringBuilder();
            observation.append(getObservationTemplate());
            
            StringBuilder allValues = new StringBuilder();
            for (MeasureResult resultValue : multiValObs.getResult().getValue()) {
                allValues.append(encodeMeasureResult(resultValue));
            }
            
            replace(observation, OBSERVATION_ID, multiValObs.getIdentifier().getIdentifierValue());
            replace(observation, OBSERVATION_UNIT_ID, multiValObs.getUnit());
            replace(observation, OBSERVATION_UNIT_NOTATION, multiValObs.getUnitNotation());
            replace(observation, OBSERVATION_PHENTIME_START, multiValObs.getResult().getDateTimeBegin().toISO8601Format());
            replace(observation, OBSERVATION_PHENTIME_END, multiValObs.getResult().getDateTimeEnd().toISO8601Format());
            replace(observation, OBSERVATION_PROCEDURE, multiValObs.getProcedure());
            replace(observation, OBSERVATION_PROPERTY, multiValObs.getObservedProperty());
            replace(observation, OBSERVATION_FEATURE, multiValObs.getFeatureOfInterest());
            replace(observation, OBSERVATION_SAMPLING_POINT, multiValObs.getSamplingPoint());
            replace(observation, OBSERVATION_AGGREGATION_TYPE, multiValObs.getAggregationType());
            replace(observation, ELEMENT_COUNT, Integer.toString(multiValObs.getResult().getValue().size()));
            replace(observation, VALUES, allValues.toString());
            
            encodedObservations.append(observation);
            
            startTimes.add(multiValObs.getResult().getDateTimeBegin());
            endTimes.add(multiValObs.getResult().getDateTimeEnd());
        }
        return wrapInEnvelope(encodedObservations.toString(), startTimes, endTimes);
    }


    private String wrapInEnvelope(String result, List<ITimePosition> startTimes, List<ITimePosition> endTimes) throws IOException {
        String start, end;
    	if (startTimes != null && startTimes.size() > 0) {
    		if (startTimes.size() != 1) {
    			Collections.sort(startTimes);
    		}
    		start = startTimes.get(0).toISO8601Format();
    	} else start = "";
    	
    	if (endTimes != null && endTimes.size() > 0) {
    		if (endTimes.size() != 1) {
    			Collections.sort(endTimes);
    		}
    		end = endTimes.get(endTimes.size() - 1).toISO8601Format();
    	} else end = "";
    	
    	String responseTemplate = getObservationEnvelopeTemplate().replace(OBSERVATIONS, result).
    			replace(OBSERVATION_PHENTIME_START, start).replace(OBSERVATION_PHENTIME_END, end);

        return responseTemplate;
    }

    protected String getObservationTemplate() {
		return observationTemplate;
	}
    
	protected String getObservationEnvelopeTemplate() {
		return observationEnvelopeTemplate;
	}

    // /////////////////////////////////// helper methods:
    
    protected String encodeMeasureResult(MeasureResult resultValue)
    {
    	StringBuilder result = new StringBuilder();
    	result.append(resultValue.getDateTimeBegin().toISO8601Format());
    	result.append(",");
    	result.append(resultValue.getDateTimeEnd().toISO8601Format());
    	result.append(",");
    	result.append(resultValue.getVerification());
    	result.append(",");
    	result.append(resultValue.getValidity());
    	result.append(",");
    	result.append(resultValue.getAggregationNotation());
    	result.append(",");
    	result.append(resultValue.getValue());
    	result.append("@@");
    	
        return result.toString();
    }

}
