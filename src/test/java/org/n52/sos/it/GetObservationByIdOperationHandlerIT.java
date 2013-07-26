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

import org.junit.Test;
import org.n52.sos.handler.GetObservationByIDOperationHandler;

import com.esri.arcgis.server.json.JSONObject;

/**
 * @author Arne
 *
 */
public class GetObservationByIdOperationHandlerIT extends EsriTestBase {
    
    private GetObservationByIDOperationHandler getObsByIDOpHandler;
    
    /**
     * @throws java.lang.Exception
     */
    public void setUp() throws Exception
    {
        super.setUp();
        getObsByIDOpHandler = new GetObservationByIDOperationHandler();
        getObsByIDOpHandler.setSosUrlExtension("http://localhost:6080/arcgis/rest/services/ObservationDB/MapServer/exts/SOSExtension");
    }

    @Test
    public void testInvokeOGCOperation()
    {
        JSONObject inputObject = new JSONObject();
        String[] responseProperties = new String[0];
        
        inputObject = inputObject.put("version", "2.0.0");
        inputObject = inputObject.put("service", "SOS");
        inputObject = inputObject.put("request", "GetObservationByID");
        inputObject = inputObject.put("observation", "1");
        
        try {
            String result = new String(getObsByIDOpHandler.invokeOGCOperation(gdb, inputObject, responseProperties));
            
            System.out.println(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testInvokeOGCOperation_RDF()
    {
        JSONObject inputObject = new JSONObject();
        String[] responseProperties = new String[0];
        
        inputObject = inputObject.put("version", "2.0.0");
        inputObject = inputObject.put("service", "SOS");
        inputObject = inputObject.put("request", "GetObservationByID");
        inputObject = inputObject.put("observation", "1");
        inputObject = inputObject.put("responseFormat", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
        
        try {
            String result = new String(getObsByIDOpHandler.invokeOGCOperation(gdb, inputObject, responseProperties));
            
            System.out.println(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
