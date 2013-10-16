package org.n52.sos.it;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.n52.util.CommonUtilities;

/**
 * 
 * @author Arne
 */
public class SosGetIT extends EsriTestBase {
	
    private static Logger LOGGER = Logger.getLogger(SosGetIT.class.getName());

	public static final String SOS_ENDPOINT = "http://localhost:6080/arcgis/rest/services/AirQualitySOS/MapServer/exts/52nArcGisSos/GetObservation";
	
	@Test
	public void testGetObservation () {
		
		HttpClient client = new DefaultHttpClient();
		
		try {
			String requestURL = CommonUtilities.concatRequestParameters(SOS_ENDPOINT, ITConstants.getInstance().getSosGetObservation1());
			LOGGER.info("request: " + requestURL);
			
			HttpGet request = new HttpGet(requestURL);
			HttpResponse response = client.execute(request);

			String result = CommonUtilities.readResource(response.getEntity().getContent());
			
	        LOGGER.info("result: " + result);
		} catch (Exception e) {
			fail();
		} finally {
			client.getConnectionManager().shutdown();
		}
	}
}
