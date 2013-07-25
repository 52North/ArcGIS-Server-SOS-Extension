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

import java.io.IOException;
import java.util.logging.Logger;

import org.junit.Assert;
import org.n52.sos.db.AccessGDB;

import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.esriLicenseProductCode;
import com.esri.arcgis.system.esriLicenseStatus;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class EsriTestBase {

    static Logger LOGGER = Logger.getLogger(EsriTestBase.class.getName());
    
    protected AoInitialize aoInit;
    
    protected AccessGDB gdb;
    
    /**
     * @throws java.lang.Exception
     */
    protected void setUp() throws Exception
    {
        // Initialize engine console application
        EngineInitializer.initializeEngine();

        // Initialize ArcGIS license
        aoInit = new AoInitialize();
        initializeArcGISLicenses();
        
        gdb = new AccessGDB();
    }
    
    protected void tearDown() throws Exception
    {
        // Ensure any ESRI libraries are unloaded in the correct order
        aoInit.shutdown();
    }
    
    /**
     * Checks to see if an ArcGIS Engine Runtime license or an ArcView License
     * is available. If so, then the appropriate ArcGIS License is initialized.
     * 
     * @throws IOException
     * @throws AutomationException
     */
    private void initializeArcGISLicenses() throws AutomationException, IOException
    {
        LOGGER.info("Initializing ArcGIS licenses...");

        if (aoInit.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeEngine) == esriLicenseStatus.esriLicenseAvailable){
            aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeEngine);
            LOGGER.info("Using ArcEngine license.");
        }
        else if (aoInit.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeArcServer) == esriLicenseStatus.esriLicenseAvailable){
            aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeArcServer);
            LOGGER.info("Using ArcServer license.");
        }
        else if (aoInit.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeAdvanced) == esriLicenseStatus.esriLicenseAvailable){
            aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeAdvanced);
            LOGGER.info("Using Advanced license.");
        }
        else if (aoInit.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeStandard) == esriLicenseStatus.esriLicenseAvailable){
            aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeStandard);
            LOGGER.info("Using Standard license.");
        }
        else if (aoInit.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeBasic) == esriLicenseStatus.esriLicenseAvailable){
            aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeBasic);
            LOGGER.info("Using Basic license.");
        }
        else {
            // System.exit(-1);
            throw new IOException("Could not initialize a license. Exiting application.");
        }
    }
    
    protected void fail() {
    	Assert.fail();
	}
}
