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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.om.observation.AbstractObservation;
import org.n52.om.observation.MultiValueObservation;
import org.n52.om.result.MeasureResult;
import org.n52.om.result.MultiMeasureResult;

import com.esri.arcgis.server.json.JSONArray;
import com.esri.arcgis.server.json.JSONObject;

/**
 * This class provides methods for encoding {@link AbstractObservation}s in an ESRI-style
 * JSON format
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class JSONObservationEncoder {

    
    public static JSONObject encodeObservations(Map<String, MultiValueObservation> idObsList) throws Exception
    {
        JSONObject json = new JSONObject();
        
        JSONArray obsArray = new JSONArray();
        
        Set<String> obsIdSet = idObsList.keySet();
        for (String obsId : obsIdSet) {

            MultiValueObservation multiValObs = idObsList.get(obsId);
            
            obsArray.put(encodeObservation(multiValObs));
        }
        
        json.put("observations", obsArray);
        
        return json;
    }

    /**
     * creates a JSON representation for a {@link MultiValueObservation}.
     */
    public static JSONObject encodeObservation(MultiValueObservation obs) throws Exception
    {
        JSONObject json = new JSONObject();

        json.put("id", obs.getIdentifier().getIdentifierValue());

        json.put("type", obs.getName());

        // encode time
        json.put("dateTimeBegin", obs.getResult().getDateTimeBegin().toISO8601Format());
        json.put("dateTimeEnd", obs.getResult().getDateTimeEnd().toISO8601Format());
        json.put("resultTime", obs.getResultTime().toISO8601Format());

        // encode observed property
        json.put("observedProperty", obs.getObservedProperty());

        // encode observed property
        json.put("unit", obs.getUnit());
        
        // encode procedure
        json.put("procedure", obs.getProcedure());

        // encode foi
        json.put("featureOfInterest", obs.getFeatureOfInterest());

        // encode result
        json.put("result", encodeResult(obs.getResult()));

        return json;
    }

    private static JSONArray encodeResult(MultiMeasureResult result)
    {
        JSONArray jsonArray = new JSONArray();
        
        List<MeasureResult> resultValues = result.getValue();
        
        for (MeasureResult measureResult : resultValues) {
            JSONObject valueObject = new JSONObject();
            valueObject.put("StartTime", measureResult.getDateTimeBegin());
            valueObject.put("EndTime", measureResult.getDateTimeEnd());
            valueObject.put("Verification", measureResult.getVerification());
            valueObject.put("Validity", measureResult.getValidity());
            valueObject.put("Value", measureResult.getValue());
            jsonArray.put(valueObject);
        }

        return jsonArray;
    }

}
