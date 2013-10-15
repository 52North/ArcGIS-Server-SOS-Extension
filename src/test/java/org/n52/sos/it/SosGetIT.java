package org.n52.sos.it;

import java.util.HashMap;
import java.util.Map;
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
	
	public static Map<String, String> SOS_GETOBSERVATION_1 = new HashMap<String, String>();

	@Before
	public void setUp() {
		SOS_GETOBSERVATION_1.put("service", "SOS");
	    SOS_GETOBSERVATION_1.put("version", "2.0.0");
	    SOS_GETOBSERVATION_1.put("request", "GetObservation");
	    SOS_GETOBSERVATION_1.put("offering", "Network_GBXXXX");
	    SOS_GETOBSERVATION_1.put("procedure", "GB_StationProcess_7");
	    SOS_GETOBSERVATION_1.put("observedProperty", "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/7");
	    SOS_GETOBSERVATION_1.put("temporalFilter", "om:phenomenonTime,2013-04-01T01:00:00/2013-04-10T01:00:00");
	    SOS_GETOBSERVATION_1.put("aggregationType", "http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1d");
	    SOS_GETOBSERVATION_1.put("f", "xml");
	}
	
	@Test
	public void testGetObservation () {
		
		HttpClient client = new DefaultHttpClient();
		
		try {
			String requestURL = CommonUtilities.concatRequestParameters(SOS_ENDPOINT, SOS_GETOBSERVATION_1);
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
