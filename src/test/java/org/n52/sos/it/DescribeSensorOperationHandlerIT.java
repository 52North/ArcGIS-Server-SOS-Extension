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

import java.io.File;
import java.util.Map;

import org.junit.Test;
import org.n52.sos.handler.DescribeSensorOperationHandler;
import org.n52.util.CommonUtilities;

import com.esri.arcgis.server.json.JSONObject;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class DescribeSensorOperationHandlerIT extends EsriTestBase {
    
    private DescribeSensorOperationHandler describeSensorOpHandler;
    
    /**
     * @throws java.lang.Exception
     */
    public void setUp() throws Exception
    {
        super.setUp();
        describeSensorOpHandler = new DescribeSensorOperationHandler();
        describeSensorOpHandler.initialize(ITConstants.SOS_DESCRIBESENSOR_ENDPOINT_LOCAL);
    }

    @Test
    public void testInvokeOGCOperation()
    {
        this.executeOGCOperation(describeSensorOpHandler,
				ITConstants.SOS_DESCRIBESENSOR_LOCAL, new File(
						"c:/temp/describeSensor.xml"));
    }

}
