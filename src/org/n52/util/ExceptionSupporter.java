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

/**
 * @author Arne
 *
 */
public class ExceptionSupporter {

    /**
     * puts stack trace elements in String[] and returns it.
     */
    public static String[] createStringArrayFromStackTrace(StackTraceElement[] sTElements) {
        String[] errorDetails = new String[sTElements.length];
        for (int i = 0; i < sTElements.length; i++) {
            errorDetails[i] = sTElements[i].toString();
        }
        return errorDetails;
    }
    
    /**
     * puts stack trace elements of specified {@link Throwable} e in String and returns it.
     */
    public static String createStringFromStackTrace(Throwable e) {
        
        String errorDetails = "";
        
        StackTraceElement[] sTElements = e.getStackTrace();
        for (int i = 0; i < sTElements.length; i++) {
            errorDetails += sTElements[i].toString() + "\n";
        }
        
        Throwable cause = e.getCause();
        if (cause != null) {
            errorDetails += "------------ caused by:";
            errorDetails += createStringFromStackTrace(cause);
        }
        
        return errorDetails;
    }
}
