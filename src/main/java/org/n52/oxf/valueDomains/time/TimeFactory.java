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
package org.n52.oxf.valueDomains.time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Creates an appropriate ITime object, (--> TimePeriod or TimePosition)
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class TimeFactory {
    
    public static ITime createTime(Date timePos)
    {
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        
        String convertedTime = iso8601Format.format(timePos);
        return createTime(convertedTime);
    }
    
    /**
     * 
     * @param timeString
     * @return an appropriate ITime object depending on the timeString that may be "now" for the most recent
     *         available data, a TimePosition (e.g. "2005-08-04") or "min/max(/res)" to create a TimePeriod.
     * 
     * @throws IllegalArgumentException
     *         if timeString is not in correct format.
     */
    public static ITime createTime(String timeString) throws IllegalArgumentException {
        if (timeString.equals("now")) {
            TimePosition now = new TimePosition(timeString);
            return now;
        }
        if (timeString.contains("/")) {
            // TIME=min/max/res
            if (timeString.split("/").length == 3) {
                TimePeriod period = new TimePeriod(timeString);
                return period;
            }
            else if (timeString.split("/").length == 2) {
                TimePeriod period = new TimePeriod(timeString.split("/")[0], timeString.split("/")[1]);
                return period;
            }
            else {
                throw new IllegalArgumentException("Time parameter is not in correct format");
            }
        }
        // TIME=timePos
        return new TimePosition(timeString);
    }
}