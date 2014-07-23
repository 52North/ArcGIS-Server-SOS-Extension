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

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.n52.util.CommonUtilities;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 *
 */
public class SosSoapIT {

    public static void main(String[] args) throws IOException {
        
        String serverName = "localhost";
        String serviceName = "ObservationDB";
        String soapExt = "SOSExtension";

        String url = "http://" + serverName + ":6080/arcgis/services/" + serviceName + "/MapServer/" + soapExt;
        
        String query = CommonUtilities.readResource(SosSoapIT.class.getResourceAsStream("soapTest.xml")); 
        
        String result = CommonUtilities.readResource(sendPostMessage(url, query));
        
        System.out.println("result: " + result);
    }
	/**
     * sends a POST-request using org.apache.commons.httpclient.HttpClient.
     * 
     * @param serviceURL
     * @param request
     * @return
     */
    public static InputStream sendPostMessage(String serviceURL, String request) throws IOException {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost  post = new HttpPost (serviceURL);

        post.setEntity(new StringEntity(request, "text/xml", "UTF-8"));

        HttpResponse response = httpClient.execute(post);

        return response.getEntity().getContent();
    }
}
