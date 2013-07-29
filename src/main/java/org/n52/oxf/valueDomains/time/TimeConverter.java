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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class TimeConverter {

    /**
     * Converts an input timeInstant in the ISO 8601 form of
     * 'yyyy-MM-ddTHH:mm:ss+HH:mm' to UTC standard time in the form of
     * 'yyyy-MM-dd HH:mm:ss'
     */
    public static String convertLocalToUTC(String timeInstant)
    {
        TimePosition timePos = (TimePosition) TimeFactory.createTime(timeInstant);

        String timeZoneOffset = getTimeZoneOffset(timeInstant);

        Calendar localTime = new GregorianCalendar(TimeZone.getTimeZone("GMT" + timeZoneOffset));
        localTime.set(Calendar.YEAR, (int) timePos.getYear());
        localTime.set(Calendar.MONTH, timePos.getMonth() - 1);
        localTime.set(Calendar.DAY_OF_MONTH, timePos.getDay());
        localTime.set(Calendar.HOUR_OF_DAY, timePos.getHour());
        localTime.set(Calendar.MINUTE, timePos.getMinute());
        localTime.set(Calendar.SECOND, (int) timePos.getSecond());

        Calendar utcTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        utcTime.setTimeInMillis(localTime.getTimeInMillis());

        int year = utcTime.get(Calendar.YEAR);
        int month = utcTime.get(Calendar.MONTH) + 1;
        int day = utcTime.get(Calendar.DAY_OF_MONTH);
        int hour = utcTime.get(Calendar.HOUR_OF_DAY);
        int minute = utcTime.get(Calendar.MINUTE);
        int second = utcTime.get(Calendar.SECOND);

        return toISO8601(false, year, month, day, hour, minute, second);
    }

    /**
     * Converts an input UTC timeInstant in the form of 'yyyy-MM-dd
     * HH:mm:ss' to local time indicated by an offset (e.g. '+02' or '-10') to
     * the ISO8601 form of "yyyy-MM-ddTHH:mm:ss+HH:mm'
     */
    public static String convertUTCToLocal(String timeInstantUTC,
            String timeZoneOffset)
    {

        // Parsing of UTC time:
        // 0123456789012345678
        // yyyy-MM-dd HH:mm:ss
        Calendar utcTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        utcTime.set(Calendar.YEAR, Integer.parseInt(timeInstantUTC.substring(0, 4)));
        utcTime.set(Calendar.MONTH, Integer.parseInt(timeInstantUTC.substring(5, 7)) - 1);
        utcTime.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeInstantUTC.substring(8, 10)));
        utcTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeInstantUTC.substring(11, 13)));
        utcTime.set(Calendar.MINUTE, Integer.parseInt(timeInstantUTC.substring(14, 16)));
        utcTime.set(Calendar.SECOND, Integer.parseInt(timeInstantUTC.substring(17, 19)));

        Calendar localTime = new GregorianCalendar(TimeZone.getTimeZone("GMT" + timeZoneOffset));
        localTime.setTimeInMillis(utcTime.getTimeInMillis());

        int year = localTime.get(Calendar.YEAR);
        int month = localTime.get(Calendar.MONTH) + 1;
        int day = localTime.get(Calendar.DAY_OF_MONTH);
        int hour = localTime.get(Calendar.HOUR_OF_DAY);
        int minute = localTime.get(Calendar.MINUTE);
        int second = localTime.get(Calendar.SECOND);

        return toISO8601(year, month, day, hour, minute, second, timeZoneOffset);
    }

    /**
     * @param iso8601TimeStamp
     *            an ISO 8601 conform time instnat following the format
     *            'yyyy-MM-ddTHH:mm:ss+HH:mm'.
     * @return the time zone offset as a String in format of e.g. '+02:00'
     */
    public static String getTimeZoneOffset(String iso8601TimeInstant)
    {
        TimePosition time = new TimePosition(iso8601TimeInstant);

        String timeZone = time.getTimezone();
        
        if (timeZone.equals("Z")) {
            return "+00:00";
        }
        return timeZone;
    }

    /**
     * Produces an ISO 8601 representation of the input date variables.
     * 
     * The output format is either: a) 'yyyy-MM-dd HH:mm:ss'
     * 
     * or with a 'T' separating date and time: b) 'yyyy-MM-ddTHH:mm:ss'
     * 
     * @param withTSeparator
     *            indicating whether the separating 'T' shall be present;
     *            otherwise a ' ' separates date and time.
     * @param year
     * @param month
     *            1 = January
     * @param day
     *            day in month; 1 = 1st day of month
     * @param hour
     *            24h mode; 23 = 11 pm
     * @param minute
     * @param second
     */
    public static String toISO8601(boolean withTSeparator,
            int year,
            int month,
            int day,
            int hour,
            int minute,
            int second)
    {

        String monthString = "" + month;
        if (month < 10) {
            monthString = "0" + monthString;
        }

        String dayString = "" + day;
        if (day < 10) {
            dayString = "0" + dayString;
        }

        String hourString = "" + hour;
        if (hour < 10) {
            hourString = "0" + hourString;
        }

        String minuteString = "" + minute;
        if (minute < 10) {
            minuteString = "0" + minuteString;
        }

        String secondString = "" + second;
        if (second < 10) {
            secondString = "0" + secondString;
        }

        if (withTSeparator) {
            return "" + year + "-" + monthString + "-" + dayString + "T" + hourString + ":" + minuteString + ":" + secondString;
        } else {
            return "" + year + "-" + monthString + "-" + dayString + " " + hourString + ":" + minuteString + ":" + secondString;
        }
    }

    /**
     * Produces an ISO 8601 conform representation of input date variables.
     * 
     * The output format is: 'yyyy-MM-ddTHH:mm:ss+HH:mm'
     * 
     * @param timeZoneOffset
     *            a string representing the time zone offset in hours and minutes, e.g., +02:00
     * 
     */
    public static String toISO8601(int year,
            int month,
            int day,
            int hour,
            int minute,
            int second,
            String timeZoneOffset)
    {
        return toISO8601(true, year, month, day, hour, minute, second) + timeZoneOffset;
    }

    public static void main(String[] args)
    {

        String localTime = "2011-10-16T01:00:00+01:00";
        System.out.println("localTime: " + localTime);
        System.out.println("converted to UTC: " + convertLocalToUTC(localTime) + "\n");

        String utcTime = "2009-12-31 20:00:00";
        System.out.println("UTC time: " + utcTime);
        String localOffset = "+05";
        System.out.println("converted to local time at '" + localOffset + "' is: " + convertUTCToLocal(utcTime, localOffset));
    }

	public static ITimePosition createTimePosition(Date startValue) {
		GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(startValue);
        // Problem: java.util.Date always sets the time zone to the
        // local time
        // zone, where the SOS is installed.
        // Hence, we have to make it UTC:
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String startTimeAsISO8601 = TimeConverter.toISO8601(year, month, day, hour, minute, second, "+00:00");
        return (ITimePosition) TimeFactory.createTime(startTimeAsISO8601);
	}
	
}
