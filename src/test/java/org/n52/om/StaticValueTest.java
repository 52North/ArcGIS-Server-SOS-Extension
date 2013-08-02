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
package org.n52.om;

import static org.hamcrest.CoreMatchers.*;
import org.junit.Assert;
import org.junit.Test;
import org.n52.gml.Identifier;
import org.n52.om.observation.MultiValueObservation;

public class StaticValueTest {
	
	@Test
	public void testValueMatchingReplacement() {
		MultiValueObservation ao = new MultiValueObservation(new Identifier(null, "id"),
				"proc", "obs", "foi", "sf",
				"unit", "not", "The daily average or daily mean is the average of all va", null);
		
		Assert.assertThat(ao.getAggregationType(), is("Fixed measurement"));
	}

}
