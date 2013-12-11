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
