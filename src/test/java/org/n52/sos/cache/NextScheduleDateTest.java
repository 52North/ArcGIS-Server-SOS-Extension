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

import java.io.File;
import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.n52.util.CommonUtilities;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CommonUtilities.class)
public class NextScheduleDateTest {
	
	@Before
	public void init() throws IOException {
		PowerMockito.mockStatic(CommonUtilities.class);
		File f = File.createTempFile("hassss", "da");
		f.mkdir();
        BDDMockito.given(CommonUtilities.resolveCacheBaseDir("test")).willReturn(f.getParentFile());
        
        AbstractCacheScheduler.Instance.init(null, false, new LocalTime("04:00:00"));
	}
	
	@After
	public void shutdown() {
		AbstractCacheScheduler.Instance.instance().shutdown();
	}

	@Test
	public void testNextScheduleDateResolving() throws IOException {
		AbstractCacheScheduler cs = AbstractCacheScheduler.Instance.instance();
		
		/*
		 * before target time. results in the same day
		 */
		DateTime referenceTime = new DateTime("2014-07-25T01:30:59.999+02:00");
		
		MutableDateTime result = cs.resolveNextScheduleDate(new LocalTime("04:00:00"), referenceTime);
		
		Assert.assertTrue(result.getHourOfDay() == 4);
		Assert.assertTrue(result.getMinuteOfHour() == 0);
		Assert.assertTrue(result.getDayOfMonth() == referenceTime.getDayOfMonth());
		Assert.assertTrue(result.isAfter(referenceTime));
		
		/*
		 * very close to target time. results in the same day
		 */
		referenceTime = new DateTime("2014-07-25T03:59:59.999+02:00");
		
		result = cs.resolveNextScheduleDate(new LocalTime("04:00:00"), referenceTime);
		
		Assert.assertTrue(result.getHourOfDay() == 4);
		Assert.assertTrue(result.getMinuteOfHour() == 0);
		Assert.assertTrue(result.getDayOfMonth() == referenceTime.getDayOfMonth());
		Assert.assertTrue(result.isAfter(referenceTime));
		
		/*
		 * safely after the schedule time of day. results in the next day
		 */
		referenceTime = new DateTime("2014-07-25T04:59:59.999+02:00");
		
		result = cs.resolveNextScheduleDate(new LocalTime("04:00:00"), referenceTime);
		
		Assert.assertTrue(result.getHourOfDay() == 4);
		Assert.assertTrue(result.getMinuteOfHour() == 0);
		Assert.assertTrue(result.getDayOfMonth() == referenceTime.getDayOfMonth()+1);
		Assert.assertTrue(result.isAfter(referenceTime));
		
		/*
		 * safely after the schedule time of day. results in the next day
		 * which is the first of a new month
		 */
		referenceTime = new DateTime("2014-07-31T04:59:59.999+02:00");
		
		result = cs.resolveNextScheduleDate(new LocalTime("04:00:00"), referenceTime);
		
		MutableDateTime mutableRef = referenceTime.toMutableDateTime();
		mutableRef.addDays(1);
		
		Assert.assertTrue(result.getHourOfDay() == 4);
		Assert.assertTrue(result.getMinuteOfHour() == 0);
		Assert.assertTrue(result.getDayOfMonth() == mutableRef.getDayOfMonth());
		Assert.assertTrue(result.isAfter(referenceTime));
		
		referenceTime = new DateTime("2014-07-31T04:00:00.000+02:00");
		
		/*
		 * EXACTLY the target schedule time. results in the next day
		 */
		result = cs.resolveNextScheduleDate(new LocalTime("04:00:00"), referenceTime);
		
		mutableRef = referenceTime.toMutableDateTime();
		mutableRef.addDays(1);
		
		Assert.assertTrue(result.getHourOfDay() == 4);
		Assert.assertTrue(result.getMinuteOfHour() == 0);
		Assert.assertTrue(result.getDayOfMonth() == mutableRef.getDayOfMonth());
		Assert.assertTrue(result.isAfter(referenceTime));
	}
	
}
