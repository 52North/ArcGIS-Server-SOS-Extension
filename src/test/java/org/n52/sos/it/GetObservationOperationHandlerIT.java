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
package org.n52.sos.it;

import java.io.File;
import java.util.Map;

import org.junit.Test;
import org.n52.sos.handler.GetObservationOperationHandler;
import org.n52.util.CommonUtilities;

import com.esri.arcgis.server.json.JSONObject;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
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
        getObsOpHandler.initialize(ITConstants.SOS_GETOBSERVATION_ENDPOINT_AGS);
    }

    /**
     * Test method for {@link org.n52.sos.handler.GetObservationOperationHandler#invokeOGCOperation(com.esri.arcgis.server.json.JSONObject, java.lang.String[])}.
     */
    @Test
    public void testInvokeGetObservation()
    {
        this.executeOGCOperation(getObsOpHandler,
				ITConstants.SOS_GETOBSERVATION, new File(
						"c:/temp/getObservation.xml"));
    }

    /**
     * Test method for {@link org.n52.sos.handler.GetObservationOperationHandler#invokeOGCOperation(com.esri.arcgis.server.json.JSONObject, java.lang.String[])}.
     */
    @Test
    public void testInvokeGetObservation_MultipleProperties()
    {
        this.executeOGCOperation(getObsOpHandler,
				ITConstants.SOS_GETOBSERVATION_MULTIPLE_PROPERTIES, new File(
						"c:/temp/getObservation_MultipleProperties.xml"));
    }
    
//    /**
//     * Test method for {@link org.n52.sos.handler.GetObservationOperationHandler#invokeOGCOperation(com.esri.arcgis.server.json.JSONObject, java.lang.String[])}.
//     */
//    @Test
//    public void testInvokeGetObservation_GetLatest()
//    {
//        this.executeOGCOperation(getObsOpHandler,
//				ITConstants.SOS_GETOBSERVATION_LOCAL_GET_LATEST, new File(
//						"c:/temp/getObservation_GetLatest.xml"));
//    }

}
