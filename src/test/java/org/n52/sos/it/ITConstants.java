/**
 * 
 */
package org.n52.sos.it;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines constants for integration testing of the SOS SOE.
 * 
 * @author Arne
 */
public class ITConstants {

	private static ITConstants testConstants;
	
	//
	// constants:
	//
	private static Map<String, String> SOS_GETOBSERVATION_1 = new HashMap<String, String>();
	private static String SOS_URL_1 = "http://localhost:6080/arcgis/rest/services/ObservationDB/MapServer/exts/SOSExtension";
	
	private ITConstants () {
		
		// init GetObservation Query Group 1:
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
	
	public static ITConstants getInstance() {
		if (testConstants == null) {
			return new ITConstants();
		}
		else {
			return testConstants;
		}
	}
	
	public Map<String, String> getSosGetObservation1 () {
		return SOS_GETOBSERVATION_1;
	}
	
	public String getSosUrl1() {
		return SOS_URL_1;
	}
}
