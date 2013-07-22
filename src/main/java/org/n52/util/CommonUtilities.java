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
package org.n52.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class CommonUtilities {
    
    static Logger LOGGER = Logger.getLogger(CommonUtilities.class.getName());
    
    /**
     * sends a POST-request using org.apache.commons.httpclient.HttpClient.
     * 
     * @param serviceURL
     * @param request
     * @return
     */
    public static InputStream sendPostMessage(String serviceURL, String request) throws IOException {

        InputStream is = null;

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(serviceURL);

        method.setRequestEntity(new StringRequestEntity(request, "text/xml", "UTF-8"));
      
        HostConfiguration hostConfig = getHostConfiguration(new URL(serviceURL));
        httpClient.setHostConfiguration(hostConfig);
        httpClient.executeMethod(method);

        LOGGER.info("POST-request sent to: " + method.getURI());
        LOGGER.info("Sent request was: " + request);
        
        is = method.getResponseBodyAsStream();

        return is;
    }
    
    protected static HostConfiguration getHostConfiguration(URL serviceURL) {
        HostConfiguration hostConfig = new HostConfiguration();
        
        // apply proxy settings:
        String host = System.getProperty("http.proxyHost");
        String port = System.getProperty("http.proxyPort");
        String nonProxyHosts = System.getProperty("http.nonProxyHosts");
        
        // check if service url is among the non-proxy-hosts:
        boolean serviceIsNonProxyHost = false;
        if (nonProxyHosts != null && nonProxyHosts.length() > 0)
        {   
            String[] nonProxyHostsArray = nonProxyHosts.split("\\|");
            String serviceHost = serviceURL.getHost();
            
            for (String nonProxyHost : nonProxyHostsArray) {
                if ( nonProxyHost.equals(serviceHost)) {
                    serviceIsNonProxyHost = true;
                    break;
                }
            }
        }
        // set proxy:
        if ( serviceIsNonProxyHost == false
          && host != null && host.length() > 0
          && port != null && port.length() > 0)
        {
            int portNumber = Integer.parseInt(port);
            hostConfig.setProxy(host, portNumber);
            LOGGER.info("Using proxy: " + host + " on port: " + portNumber);
        }
        
        return hostConfig;
    }
    
    /**
     * produces a single String representation of a stringArray.
     */
    public static String arrayToString(String[] stringArray) {
        StringBuilder stringRep = new StringBuilder();
        stringRep.append("[");
        for (int i = 0; i < stringArray.length; i++) {
            stringRep.append(stringArray[i]);
            
            if (i < stringArray.length) {
                stringRep.append(",");
            }
        }
        stringRep.append("]");
        return stringRep.toString();
    }
}