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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a TimeResolution as specified in ISO8601:2004. Inputs are validated against this pattern:
 * "P(\\d+Y)?(\\d+M)?(\\d+D)?(T(\\d+H)?(\\d+M)?(\\d+([.]\\d+)?S)?)?" <br>
 * Valid example time strings: <li>P2Y</li> <li>P1Y1M3DT6H2M8.5S</li>
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class TimeResolution implements ITimeResolution {

    private long years = NOT_SET;
    private int months = NOT_SET;
    private int days = NOT_SET;
    private int hours = NOT_SET;
    private int minutes = NOT_SET;
    private float seconds = NOT_SET;
    public static final String RESOLUTION_PATTERN = "P(\\d+Y)?(\\d+M)?(\\d+D)?(T(\\d+H)?(\\d+M)?(\\d+([.]\\d+)?S)?)?";

    private String resString;

    /**
     * constructs a timeResolution. Validates against the pattern mentioned in the ISO8601:2004 spec (section
     * 4.4.4.2.1). This pattern is extended in order to support a number of days with more than 2 digits.
     * 
     * @param res
     */
    public TimeResolution(String res) {
        if (res == null) {
            throw new NullPointerException();
        }
        if (res.equals("P")) {
            throw new IllegalArgumentException("resolution has to be more specified. Not only P!");
        }
        if (res.endsWith("T")) {
            throw new IllegalArgumentException(res + " : ends with T. That indicates a following time"
                    + "which is missing. Add time or remove T!");
        }
        Pattern resPattern = Pattern.compile(RESOLUTION_PATTERN);
        Matcher matcher = resPattern.matcher(res);
        if ( !matcher.matches()) {
            throw new IllegalArgumentException("Resolution String does not match the pattern");
        }

        // indicates whether the group containing "T" has been passed.
        boolean tPassed = false;
        for (int i = 1; i <= matcher.groupCount(); i++) {
            String group = matcher.group(i);
            if (group != null && !group.startsWith("T")) {
                if (group.endsWith("Y")) {
                    years = Long.parseLong(group.substring(0, group.length() - 1));
                }
                else if (group.endsWith("M") && !tPassed) {
                    // MONTH FOUND
                    months = Integer.parseInt(group.substring(0, group.length() - 1));
                }
                else if (group.endsWith("D")) {
                    days = Integer.parseInt(group.substring(0, group.length() - 1));
                }
                else if (group.endsWith("H")) {
                    hours = Integer.parseInt(group.substring(0, group.length() - 1));
                }
                else if (group.endsWith("M") && tPassed) {
                    // MINUTE FOUND
                    minutes = Integer.parseInt(group.substring(0, group.length() - 1));
                }
                else if (group.endsWith("S")) {
                    seconds = Float.parseFloat(group.substring(0, group.length() - 1));
                }
            }
            else {
                tPassed = true;
            }
        }

        this.resString = res;
    }

    public long getYears() {
        return years;
    }

    public int getMonths() {
        return months;
    }

    public int getDays() {
        return days;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getHours() {
        return hours;
    }

    public float getSeconds() {
        return seconds;
    }

    public String toISO8601Format() {
        return resString;
    }

    @Override
    public String toString() {
        return toISO8601Format();
    }
}