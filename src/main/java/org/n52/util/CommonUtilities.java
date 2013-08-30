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

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;


/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class CommonUtilities {
    
    public static final String NEW_LINE_CHAR = System.getProperty("line.separator");
	private static final String TAB_CHAR = "\t";


	/**
	 * produces a Strin[] out of a String Collection. 
	 */
    public static String[] toArray(Collection<String> stringCollection) {
        String[] sArray = new String[stringCollection.size()];
        int i=0;
        for (Iterator<String> iterator = stringCollection.iterator(); iterator.hasNext();) {
            sArray[i] = (String) iterator.next();
            i++;
        }
        return sArray;
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
    
    public static String readResource(InputStream res) {
		Scanner sc = new Scanner(res);
		StringBuilder sb = new StringBuilder();
		while (sc.hasNext()) {
			sb.append(sc.nextLine());
			sb.append(NEW_LINE_CHAR);
		}
		sc.close();
		return sb.toString();
	}

	public static String convertExceptionToString(Exception e) {
		StringBuilder sb = new StringBuilder(e.getMessage());
		sb.append(":");
		sb.append(NEW_LINE_CHAR);

		int count = 0;
		for (StackTraceElement ste : e.getStackTrace()) {
			sb.append(TAB_CHAR);
			sb.append(ste.toString());
			if (++count < e.getStackTrace().length) 
				sb.append(NEW_LINE_CHAR);
		}
		
		return sb.toString();
	}
    
}