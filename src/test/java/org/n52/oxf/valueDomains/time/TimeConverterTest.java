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
package org.n52.oxf.valueDomains.time;

import java.util.Date;

import static org.hamcrest.CoreMatchers.*;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class TimeConverterTest {


	@Test
    public void testTimeZoneHandling() {
        
        String timeInstantLocal = "2011-10-16T01:00:00+02:00";
        String timeInstantUTC = "2011-10-15 23:00:00";
        
        Assert.assertEquals(timeInstantUTC, TimeConverter.convertLocalToUTC(timeInstantLocal));
        
        String timeZoneOffset = TimeConverter.getTimeZoneOffset(timeInstantLocal);
        Assert.assertEquals(timeInstantLocal, TimeConverter.convertUTCToLocal(timeInstantUTC, timeZoneOffset));
    }
	
	@Test
	public void testTimePositionCreation() {
		Date now = new Date(1374690253000L);
		
		ITimePosition result = TimeConverter.createTimePosition(now);
		
		Assert.assertThat(result.toString(), is(equalTo("24.7.2013 20:24:13.0+00:00")));
	}
}
