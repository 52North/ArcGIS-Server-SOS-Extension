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

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations; 
import org.n52.sos.db.impl.AccessGDBImpl;
import org.n52.sos.handler.GetCapabilitiesOperationHandler;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 *
 */
public class GetCapabilitiesOperationHandlerIT extends EsriTestBase {
    
    @Mock
    private GetCapabilitiesOperationHandler getCapabilitiesOpHandler;
    
    private AccessGDBImpl accessGDB;
    
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        MockitoAnnotations.initMocks(this);
        
        accessGDB.getServiceDescription();
        
        getCapabilitiesOpHandler.initialize(ITConstants.SOS_GETCAPABILITIES_ENDPOINT_LOCAL);
        
    }

    @Test
    public void testInvokeOGCOperation()
    {
        this.executeOGCOperation(getCapabilitiesOpHandler,
				ITConstants.SOS_GETCAPABILITIES_LOCAL, new File(
						"c:/temp/getCapabilities.xml"));
    }

}
