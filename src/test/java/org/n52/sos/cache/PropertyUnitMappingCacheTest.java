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
package org.n52.sos.cache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.n52.sos.dataTypes.PropertyUnitMapping;
import org.n52.util.CommonUtilities;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CommonUtilities.class)
public class PropertyUnitMappingCacheTest {
	
	String line = "0=1=6;153=6;2=1;352=6;428=6;246=6;43=6;95=1;124=6;56=6;192=6;423=6";

	@Test
	public void testSerializationRoundtrip() throws IOException, CacheException {
		PowerMockito.mockStatic(CommonUtilities.class);
		File f = File.createTempFile("hassss", "da");
		f.mkdir();
        BDDMockito.given(CommonUtilities.resolveCacheBaseDir("test")).willReturn(f.getParentFile());
		
		PropertyUnitMappingCache pumc = PropertyUnitMappingCache.instance("test");
		
		Map<String, PropertyUnitMapping> result = pumc.deserializeEntityCollection(new ByteArrayInputStream(line.getBytes()));
		
		PropertyUnitMapping mapping = result.values().iterator().next();
		
		Assert.assertTrue("Unexpected mapping size", 12 == mapping.size());
		
		String roundtripped = "0=".concat(pumc.serializeEntity(mapping));

		Map<String, PropertyUnitMapping> resultRoundtripped = pumc.deserializeEntityCollection(new ByteArrayInputStream(roundtripped.getBytes()));
		
		PropertyUnitMapping mappingRoundtripped = resultRoundtripped.values().iterator().next();
		
		Assert.assertEquals(mapping, mappingRoundtripped);
	}
	
	
}
