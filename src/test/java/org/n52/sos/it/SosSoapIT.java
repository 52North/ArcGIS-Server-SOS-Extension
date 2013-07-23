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
package org.n52.sos.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.n52.util.CommonUtilities;

/**
 * @author Arne
 *
 */
public class SosSoapIT {

    public static void main(String[] args) throws IOException {
        
        String serverName = "localhost";
        String serviceName = "ObservationDB";
        String soapExt = "SOSExtension";

        String url = "http://" + serverName + ":6080/arcgis/services/" + serviceName + "/MapServer/" + soapExt;
        
        String query = readText(SosSoapIT.class.getResourceAsStream("soapTest.xml")); 
        
        String result = readText(CommonUtilities.sendPostMessage(url, query));
        
        System.out.println("result: " + result);
    }
    
    private static String readText(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        StringBuffer sb = new StringBuffer();
        for (int i=0; (line = br.readLine()) != null; i++) {
            
            // if not first line --> append "\n"
            if (i > 0) {
                sb.append("\n");
            }
            
            sb.append(line);
        }
        br.close();

        return sb.toString();
    }
}
