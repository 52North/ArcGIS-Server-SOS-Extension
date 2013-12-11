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
package org.n52.sos.db;

import java.util.Collection;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.n52.om.sampling.Feature;
import org.n52.sos.encoder.JSONEncoder;
import org.n52.sos.it.EsriTestBase;

public class AccessGdbForFeaturesIT extends EsriTestBase {

    static Logger LOGGER = Logger.getLogger(AccessGdbForFeaturesIT.class.getName());
    
    @Test
    public void testGetFeaturesOfInterest_withoutParameters()
    {
        try {
            Collection<Feature> featuresOfInterest = null;

            long millis;

            LOGGER.info("######################################");
            millis = System.currentTimeMillis();
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(null, null, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + featuresOfInterest.size());
            Assert.assertNotNull("Without Entries", featuresOfInterest);
            Assert.assertTrue(featuresOfInterest.size() > 0);

            System.out.println(JSONEncoder.encodeSamplingFeatures(featuresOfInterest).toString(2));

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void testGetFeaturesOfInterest_withObservedProperty()
    {
        try {
            Collection<Feature> featuresOfInterest = null;

            long millis;
            LOGGER.info("######################################");
            millis = System.currentTimeMillis();
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(null, new String[] { "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/1" }, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + featuresOfInterest.size());
            Assert.assertNotNull("Without Entries", featuresOfInterest);
            Assert.assertTrue(featuresOfInterest.size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void testGetFeaturesOfInterest_withProcedure()
    {
        try {
            Collection<Feature> featuresOfInterest = null;

            long millis;
            LOGGER.info("######################################");
            millis = System.currentTimeMillis();
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(null, null, new String[] { "GB_StationProcess_1" }, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + featuresOfInterest.size());
            Assert.assertNotNull("Without Entries", featuresOfInterest);
            Assert.assertTrue(featuresOfInterest.size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void testGetFeaturesOfInterest_withFoiID()
    {
        try {
            Collection<Feature> featuresOfInterest = null;

            long millis;
            LOGGER.info("######################################");
            millis = System.currentTimeMillis();
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(new String[] { "" }, null, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + featuresOfInterest.size());
            Assert.assertNotNull("Without Entries", featuresOfInterest);
            Assert.assertTrue(featuresOfInterest.size() == 1);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    //@Test
    public void testGetFeaturesOfInterest_withSpatialFilter()
    {
        try {
            Collection<Feature> featuresOfInterest = null;

            long millis;
            LOGGER.info("######################################");
            millis = System.currentTimeMillis();
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(null, null, null, "{\"xmin\":-180.0,\"ymin\":-90.0,\"xmax\":180.0,\"ymax\":90.0,\"spatialReference\":{\"wkid\":4326}}");
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + featuresOfInterest.size());
            Assert.assertNotNull("Without Entries", featuresOfInterest);
            Assert.assertTrue(featuresOfInterest.size() > 0);
            
            System.out.println(JSONEncoder.encodeSamplingFeatures(featuresOfInterest).toString(2));

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
}
