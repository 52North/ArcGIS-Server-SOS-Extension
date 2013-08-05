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
package org.n52.sos;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.n52.gml.Identifier;
import org.n52.om.observation.MultiValueObservation;
import org.n52.om.result.MeasureResult;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimeConverter;
import org.n52.sos.db.AccessGDB;
import org.n52.sos.db.AccessGdbForObservations;
import org.n52.sos.handler.GetObservationOperationHandler;

import com.esri.arcgis.server.json.JSONObject;

public class GetObservationOperationHandlerTest {

	private static final Date DATE_NOW = new Date();

	private static final int TIMES_COUNT = 10;

	@Mock
	private AccessGdbForObservations observationDB;
	@Mock
	private AccessGdbForObservations observationDBMultiObservations;
	@Mock
	private AccessGDB geoDB;
	@Mock
	private AccessGDB geoDBMultiObservations;
	

	private String codeSpace = "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_StationProcess.xml#";
	private String idValue = "GB_StationProcess_1";
	private String procedure = codeSpace + idValue;
	private String observedProperty = "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/8";
	private String foi = "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_Sample.xml#GB_SamplingFeature_850";
	private String samplingFeature = "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_SamplingPoint.xml#GB_SamplingPoint_64";
	private String unit = "http://dd.eionet.europa.eu/vocabulary/aq/observationunit/mg.m-3";
	private String unitCode = "mg.m-3";
	private String unitLabel = "ozone-or-what";
	private String aggregationType = "test";

	private List<Date> times;

	
	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

		initTimeValue();
		
		initDefaultDB();
		
		initMultiObservationDB();
	}

	private void initTimeValue() {
		this.times = new ArrayList<Date>();
		
		for (int i = 0; i < TIMES_COUNT; i++) {
			Calendar date = new GregorianCalendar();
			date.setTime(DATE_NOW);
			date.add(Calendar.YEAR, -i);
			this.times.add(date.getTime());
		}
	}

	private void initMultiObservationDB() throws Exception {
		Map<String, MultiValueObservation> staticMap = createStaticMap(TIMES_COUNT);

		Mockito.when(observationDBMultiObservations.getObservations(null, null, null, new String[] {"GB_StationProcess_1"}, null, null, null))
				.thenReturn(staticMap);
		
		Mockito.when(geoDBMultiObservations.getObservationAccess()).thenReturn(observationDBMultiObservations);		
	}

	private void initDefaultDB() throws Exception {
		Map<String, MultiValueObservation> staticMap = createStaticMap(1);

		Mockito.when(observationDB.getObservations(null, null, null, new String[] {"GB_StationProcess_1"}, null, null, null))
				.thenReturn(staticMap);
		
		Mockito.when(geoDB.getObservationAccess()).thenReturn(observationDB);		
	}

	private Map<String, MultiValueObservation> createStaticMap(int i)
			throws URISyntaxException {
		Map<String, MultiValueObservation> result = new HashMap<String, MultiValueObservation>();

		for (int j = 0; j < i; j++) {
			ITimePosition time = TimeConverter.createTimePosition(times.get(j));
			MultiValueObservation mvo = new MultiValueObservation(
					new Identifier(
							new URI(
									codeSpace),
							idValue),
					procedure,
					observedProperty,
					foi,
					samplingFeature,
					unit,
					unitCode,
					unitLabel,
					aggregationType,
					time);
			
			mvo.getResult().addResultValue(new MeasureResult(time, time, "1", "3", "summer", 40.0));
			
			result.put("GB_Observation_"+j, mvo);			
		}

		return result;
	}
	
	@Test
	public void testHandler() throws Exception {
		GetObservationOperationHandler handler = new GetObservationOperationHandler();
		Assert.assertThat(handler.canHandle("GetObservation"), is(true));
		
		JSONObject input = new JSONObject("{\"version\":\"2.0.0\",\"request\":\"GetObservation\",\"service\":\"SOS\",\"procedure\":\"GB_StationProcess_1\",\"f\":\"pjson\"}");
		
		byte[] response = handler.invokeOGCOperation(geoDB, input, new String[] {""});
		
		Assert.assertThat(response, is(notNullValue()));
		
		assertAllAttributesContained(new String(response));
	}
	
	@Test
	public void shouldReturnSortedPhenomenonTime() throws Exception {
		GetObservationOperationHandler handler = new GetObservationOperationHandler();
		Assert.assertThat(handler.canHandle("GetObservation"), is(true));
		
		JSONObject input = new JSONObject("{\"version\":\"2.0.0\",\"request\":\"GetObservation\",\"service\":\"SOS\",\"procedure\":\"GB_StationProcess_1\",\"responseFormat\":\"http://aqd.ec.europa.eu/aqd/0.3.7c\",\"f\":\"pjson\"}");
		
		byte[] response = handler.invokeOGCOperation(geoDBMultiObservations, input, new String[] {""});
		
		Assert.assertThat(response, is(notNullValue()));
		
		assertCorrectReportingPeriod(new String(response));
	}

	private void assertCorrectReportingPeriod(String string) {
		String startString = "<gml:beginPosition>"
				+TimeConverter.createTimePosition(this.times.get(TIMES_COUNT-1)).toISO8601Format()
				+"</gml:beginPosition>";
		String endString = "<gml:endPosition>"
				+TimeConverter.createTimePosition(this.times.get(0)).toISO8601Format()
				+"</gml:endPosition>";
		int startPos = string.indexOf(startString);
		int endPos = string.indexOf(endString);
		
		/*
		 * very dirty test but avoids real XML handling
		 * check for nearness of the two strings, this
		 * can be understood as being in the same gml:TimePeriod
		 */
		Assert.assertThat(startPos, is(not(-1)));
		Assert.assertThat(endPos, is(not(-1)));
		Assert.assertTrue(Math.abs(startPos-endPos) < startString.length()+25);
	}

	private void assertAllAttributesContained(String response) {
		Assert.assertThat(response, containsString(aggregationType));
		Assert.assertThat(response, containsString(codeSpace));
		Assert.assertThat(response, containsString(foi));
		Assert.assertThat(response, containsString(idValue));
		Assert.assertThat(response, containsString(procedure));
		Assert.assertThat(response, containsString(samplingFeature));
		Assert.assertThat(response, containsString(unit));
		Assert.assertThat(response, containsString(unitCode));
		Assert.assertThat(response, containsString(unitLabel));
	}
}
