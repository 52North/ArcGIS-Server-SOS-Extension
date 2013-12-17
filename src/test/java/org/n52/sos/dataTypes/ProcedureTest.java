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
import org.n52.sos.dataTypes.Procedure;

public class ProcedureTest {

	@Test
	public void testEqualsObject() {
		String aID       = "Procedure_A";
		String aResource = "Procedure_A_Resource";
		
		Procedure p1 = new Procedure(aID, aResource);
		Procedure p2 = new Procedure(aID, aResource);
		
		Assert.assertThat(p1.equals(p2), is(true));
	}

}
