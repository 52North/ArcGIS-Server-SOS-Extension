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

import java.util.Calendar;
import java.util.Comparator;

/**
 * Specifies a position of time by defining year, month, day, hour, minute and second.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public interface ITimePosition extends ITime, Comparable<ITimePosition>, Comparator<ITimePosition> {

    long getYear();

    int getMonth();

    int getDay();

    int getHour();

    int getMinute();

    float getSecond();
    
    String getTimezone();

    boolean before(ITimePosition timePos);

    boolean after(ITimePosition timePos);
    
    /**
     * @return this ITimePosition as a {@link java.util.Calendar} object.
     */
    Calendar getCalendar();
}