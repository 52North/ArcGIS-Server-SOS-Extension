package org.n52.sos;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.n52.om.sampling.Feature;
import org.n52.sos.encoder.OGCFeatureEncoder;
import org.n52.sos.it.EsriTestBase;
import org.n52.util.CommonUtilities;

public class OGCFeatureEncoderIT extends EsriTestBase {

	@Test
	public void testEncodeFeatures() {
		
		try {
            Collection<Feature> featuresOfInterest = null;
            
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(null, null, null, null);
            
            Assert.assertNotNull("Without Entries", featuresOfInterest);
            Assert.assertTrue(featuresOfInterest.size() > 0);

            System.out.println(new OGCFeatureEncoder().encodeFeatures(featuresOfInterest));
            
            CommonUtilities.saveFile(new File("c:/temp/encodedFeatures.xml"), new OGCFeatureEncoder().encodeFeatures(featuresOfInterest));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
	}

}
