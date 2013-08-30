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
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(null, new String[] { "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/7" }, null, null);
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
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(new String[] { "GB_SamplingFeature_4" }, null, null, null);
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
