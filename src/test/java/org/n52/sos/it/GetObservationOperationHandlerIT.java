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
package org.n52.sos.it;

import java.util.Map;

import org.junit.Test;
import org.n52.sos.handler.GetObservationOperationHandler;

import com.esri.arcgis.server.json.JSONObject;

/**
 * @author Arne
 *
 */
public class GetObservationOperationHandlerIT extends EsriTestBase {
    
    private GetObservationOperationHandler getObsOpHandler;
    
    /**
     * @throws java.lang.Exception
     */
    public void setUp() throws Exception
    {
        super.setUp();
        getObsOpHandler = new GetObservationOperationHandler();
        getObsOpHandler.initialize(ITConstants.getInstance().getSosUrl1());
    }

    /**
     * Test method for {@link org.n52.sos.handler.GetObservationOperationHandler#invokeOGCOperation(com.esri.arcgis.server.json.JSONObject, java.lang.String[])}.
     */
    @Test
    public void testInvokeOGCOperation()
    {
        JSONObject inputObject = new JSONObject();
        String[] responseProperties = new String[1];
        
        // init the JSONObject to simulate a GetObservation request:
        Map<String, String> kvp = ITConstants.getInstance().getSosGetObservation1();
        
        inputObject = inputObject.put("version", kvp.get("version"));
        inputObject = inputObject.put("service", kvp.get("service"));
        inputObject = inputObject.put("request", kvp.get("request"));
        inputObject = inputObject.put("observedProperty", kvp.get("observedProperty"));
        inputObject = inputObject.put("featureOfInterest", "GB_SamplingFeature_849");
        inputObject = inputObject.put("temporalFilter", "om:phenomenonTime,2013-04-15T00:00:00/2013-04-20T00:00:00");
        inputObject = inputObject.put("aggregationType", "http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1d");
      //inputObject = inputObject.put("responseFormat", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
        
        try {
            String result = new String(getObsOpHandler.invokeOGCOperation(gdb, inputObject, responseProperties));
            
            System.out.println(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }


}
