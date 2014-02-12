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
import org.n52.sos.handler.GetFeatureOfInterestOperationHandler;
import org.n52.util.CommonUtilities;

import com.esri.arcgis.server.json.JSONObject;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 *
 */
public class GetFeatureOfInterestOperationHandlerIT extends EsriTestBase {
    
    private GetFeatureOfInterestOperationHandler getFoiOpHandler;
    
    /**
     * @throws java.lang.Exception
     */
    public void setUp() throws Exception
    {
        super.setUp();
        getFoiOpHandler = new GetFeatureOfInterestOperationHandler();
        getFoiOpHandler.initialize(ITConstants.SOS_GETFOI_ENDPOINT_LOCAL);
    }

    /**
     * Test method for {@link org.n52.sos.handler.GetObservationOperationHandler#invokeOGCOperation(com.esri.arcgis.server.json.JSONObject, java.lang.String[])}.
     */
    @Test
    public void testInvokeOGCOperation()
    {
    	// test different parameterizations:
    	
        this.executeOGCOperation(getFoiOpHandler,
				ITConstants.SOS_GETFOI, new File(
						"c:/temp/getFeatureOfInterest.xml"));
        
        this.executeOGCOperation(getFoiOpHandler,
				ITConstants.SOS_GETFOI_2, new File(
						"c:/temp/getFeatureOfInterest_2.xml"));
    }
}
