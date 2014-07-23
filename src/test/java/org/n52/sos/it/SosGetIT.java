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
package org.n52.sos.it;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.junit.Test;
import org.n52.util.CommonUtilities;

/**
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class SosGetIT {

	private static Logger LOGGER = Logger.getLogger(SosGetIT.class.getName());

	@Test
	public void testGetCapabilities() {

		executeRequest(
				ITConstants.SOS_GETCAPABILITIES_ENDPOINT_AGS, 
				ITConstants.SOS_GETCAPABILITIES,
				new File("c:/temp/TestGetCapabilitiesResponse.xml"));
	}
	
	@Test
	public void testDescribeSensor() {

		executeRequest(
				ITConstants.SOS_DESCRIBESENSOR_ENDPOINT_AGS, 
				ITConstants.SOS_DESCRIBESENSOR_NETWORK,
				new File("c:/temp/TestDescribeSensorNetworkResponse.xml"));
		
		executeRequest(
				ITConstants.SOS_DESCRIBESENSOR_ENDPOINT_AGS, 
				ITConstants.SOS_DESCRIBESENSOR_SUBCOMPONENT,
				new File("c:/temp/TestDescribeSensorSubcomponentResponse.xml"));
	}
	
	@Test
	public void testGetObservationByID() {

		executeRequest(
				ITConstants.SOS_GETOBSERVATIONBYID_ENDPOINT_AGS, 
				ITConstants.SOS_GETOBSERVATIONBYID,
				new File("c:/temp/TestGetObservationByIdResponse.xml"));
	}
	
	@Test
	public void testGetFeatureOfInterest() {

		executeRequest(
				ITConstants.SOS_GETFOI_ENDPOINT_AGS, 
				ITConstants.SOS_GETFOI,
				new File("c:/temp/TestGetFeatureOfInterestResponse.xml"));
		
		executeRequest(
				ITConstants.SOS_GETFOI_ENDPOINT_AGS, 
				ITConstants.SOS_GETFOI_2,
				new File("c:/temp/TestGetFeatureOfInterest2Response.xml"));
	}
	
	@Test
	public void testGetObservation() {

		executeRequest(
				ITConstants.SOS_GETOBSERVATION_ENDPOINT_AGS, 
				ITConstants.SOS_GETOBSERVATION_MULTIPLE_PROPERTIES,
				new File("c:/temp/TestGetObservationMultipleResponse.xml"));
		
		executeRequest(
				ITConstants.SOS_GETOBSERVATION_ENDPOINT_AGS, 
				ITConstants.SOS_GETOBSERVATION,
				new File("c:/temp/TestGetObservation.xml"));
	}
	
	
	private void executeRequest(String requestUrlEndpoint, Map<String, String> keyValuePairs, File outputFile) {
		HttpClient client = new DefaultHttpClient();

		try {
			URL requestURL = new URL(concatRequestParameters(requestUrlEndpoint, keyValuePairs));
			LOGGER.info("request: " + requestURL);

			HttpGet request = new HttpGet(requestURL.toString());
			HttpResponse response = client.execute(request);

			String result = CommonUtilities.readResource(response.getEntity().getContent());

			CommonUtilities.saveFile(outputFile, result);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.severe(e.getLocalizedMessage());
			Assert.fail();
		} finally {
			client.getConnectionManager().shutdown();
		}
	}
	
	   /**
		 * Appends the keyValuePairs to the requestBase, according to the schema: requestBase?key1=value1&key2=value2&...
	 * @throws UnsupportedEncodingException 
		 */
		public static String concatRequestParameters(String requestBase, Map<String, String> keyValuePairs) throws UnsupportedEncodingException {

			StringBuffer resultingURL = new StringBuffer();
			resultingURL.append(requestBase);
			
			if (! requestBase.endsWith("?"))
				resultingURL.append("?");
			
			for (String key : keyValuePairs.keySet()) {
				resultingURL.append(key);
				resultingURL.append("=");
				resultingURL.append(URLEncoder.encode(keyValuePairs.get(key), "UTF-8"));
				resultingURL.append("&");
			}
			
			return resultingURL.toString();
		}
}
