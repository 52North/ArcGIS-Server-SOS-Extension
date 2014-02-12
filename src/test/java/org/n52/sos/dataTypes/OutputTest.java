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
package org.n52.sos.dataTypes;

import static org.hamcrest.CoreMatchers.is;

import org.junit.Assert;
import org.junit.Test;

public class OutputTest {
	
	String aObservedProperty	= "Property_A";
	String aPropertyLabel 		= "PropertyLabel_A";
	String aUnit 				= "Unit_A";
	
	String bObservedProperty	= "Property_B";
	String bPropertyLabel 		= "PropertyLabel_B";
	String bUnit 				= "Unit_B";
	
	@Test
	public void testEqualsObject() {
		Output o1 = new Output(aObservedProperty, aPropertyLabel, aUnit);
		Output o2 = new Output(aObservedProperty, aPropertyLabel, aUnit);
		
		Assert.assertThat(o1.equals(o2), is(true));
	}
	
	@Test
	public void testNotEqualsObject() {
		Output o1 = new Output(aObservedProperty, aPropertyLabel, aUnit);
		Output o2 = new Output(bObservedProperty, aPropertyLabel, aUnit);
		Assert.assertThat(o1.equals(o2), is(false));
		
		Output o3 = new Output(aObservedProperty, bPropertyLabel, aUnit);
		Assert.assertThat(o1.equals(o3), is(false));
		
		Output o4 = new Output(aObservedProperty, aPropertyLabel, bUnit);
		Assert.assertThat(o1.equals(o4), is(false));
	}
	
	@Test
	public void testEqualsObject_BothObservedPropertiesNull() {
		Output o1 = new Output(null, aPropertyLabel, aUnit);
		Output o2 = new Output(null, aPropertyLabel, aUnit);
		
		Assert.assertThat(o1.equals(o2), is(true));
	}
	
	@Test
	public void testEqualsObject_OneObservedPropertyNull() {
		Output o1 = new Output(aObservedProperty, aPropertyLabel, aUnit);
		Output o2 = new Output(null, aPropertyLabel, aUnit);
		
		Assert.assertThat(o1.equals(o2), is(false));
	}
	
	@Test
	public void testEqualsObject_BothLabelsNull() {
		Output o1 = new Output(aObservedProperty, null, aUnit);
		Output o2 = new Output(aObservedProperty, null, aUnit);
		
		Assert.assertThat(o1.equals(o2), is(true));
	}
	
	@Test
	public void testEqualsObject_OneLabelNull() {
		Output o1 = new Output(aObservedProperty, aPropertyLabel, aUnit);
		Output o2 = new Output(aObservedProperty, null, aUnit);
		
		Assert.assertThat(o1.equals(o2), is(false));
	}
	
	@Test
	public void testEqualsObject_UnitNull() {
		Output o1 = new Output(aObservedProperty, aPropertyLabel, null);
		Output o2 = new Output(aObservedProperty, aPropertyLabel, null);
		
		Assert.assertThat(o1.equals(o2), is(true));
	}
	
	@Test
	public void testEqualsObject_OneUnitNull() {
		Output o1 = new Output(aObservedProperty, aPropertyLabel, aUnit);
		Output o2 = new Output(aObservedProperty, aPropertyLabel, null);
		
		Assert.assertThat(o1.equals(o2), is(false));
	}
}