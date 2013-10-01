/**
 * 
 */
package org.n52.sos;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.n52.om.sampling.Feature;
import org.n52.sos.db.AccessGDB;
import org.n52.sos.db.AccessGdbForFeatures;
import org.n52.sos.handler.GetFeatureOfInterestOperationHandler;

import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.server.json.JSONObject;

/**
 * @author Arne
 *
 */
public class GetFeatureOfInterestOperationHandlerTest {

	@Mock
	private AccessGdbForFeatures featuresDB;

	@Mock
	private AccessGDB geoDB;
	
	private String uri = "http://myfeature";
	private String gmlId = "gmlID";
	private int localId = 123;
	private String name = "my feature";
	private String description = "great feature";
	private String sampledFeatureURI = "http://mySampledFeature";
	private IGeometry shape = null;

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		initDefaultDB();
	}
	
	private void initDefaultDB() throws Exception {
		Collection<Feature> staticFeatureCollection = createFeatureCollection(1);

		Mockito.when(featuresDB.getFeaturesOfInterest(null, null, null, null)).thenReturn(staticFeatureCollection);
		
		Mockito.when(geoDB.getFeatureAccess()).thenReturn(featuresDB);		
	}
	
	private Collection<Feature> createFeatureCollection(int i) throws IllegalArgumentException, URISyntaxException {
		Collection<Feature> result = new HashSet<Feature>();

		for (int j = 0; j < i; j++) {
			Feature feature = new Feature(new URI(uri), gmlId, localId, name, description, sampledFeatureURI, shape);
			
			result.add(feature);
		}

		return result;
	}

	/**
	 * Test method for {@link org.n52.sos.handler.GetFeatureOfInterestOperationHandler#invokeOGCOperation(org.n52.sos.db.AccessGDB, com.esri.arcgis.server.json.JSONObject, java.lang.String[])}.
	 * @throws Exception 
	 */
	@Test
	public void testInvokeOGCOperation() throws Exception {
		
		GetFeatureOfInterestOperationHandler handler = new GetFeatureOfInterestOperationHandler();
		
		Assert.assertThat(handler.canHandle("GetFeatureOfInterest"), is(true));

		JSONObject input = new JSONObject("{\"version\":\"2.0.0\",\"request\":\"GetFeatureOfInterest\",\"service\":\"SOS\",\"f\":\"pjson\"}");
		
		byte[] response = handler.invokeOGCOperation(geoDB, input, new String[] {""});
		
		Assert.assertThat(response, is(notNullValue()));
		
		assertAllAttributesContained(new String(response));
	}
	
	private void assertAllAttributesContained(String response) {
		//Assert.assertThat(response, containsString(uri));
		Assert.assertThat(response, containsString(gmlId));
		//Assert.assertThat(response, containsString(""+localId));
		//Assert.assertThat(response, containsString(name));
		//Assert.assertThat(response, containsString(description));
		Assert.assertThat(response, containsString(sampledFeatureURI));
		//Assert.assertThat(response, containsString(shape));
	}

}
