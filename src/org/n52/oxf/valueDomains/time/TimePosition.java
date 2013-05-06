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
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single timePosition. It is leaned on the WMS profile of ISO8601 spec. Any suggestions about
 * that profile are made in the decisions.html. <br>
 * Valid example time strings: <li>2005-11-01</li> <li>2005-11-01T12:30</li> <li>2005-11-01T12:30:20Z</li>
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class TimePosition implements ITimePosition {

    public static final String YEAR_PATTERN = "-?\\d+";
    public static final String MONTH_PATTERN = "0*\\d?\\d";
    public static final String DAY_PATTERN = "0*\\d?\\d";
    public static final String HOUR_PATTERN = "0*\\d?\\d";
    public static final String MINUTE_PATTERN = "0*\\d?\\d";
    public static final String SECOND_PATTERN = "0*\\d?\\d(.\\d)?\\d?\\d?";
    public static final String UTC_PATTERN = "\\d+(Z|[+-]\\d\\d([:]?(\\d\\d))?)";

    private long year;
    private int month = NOT_SET;
    private int day = NOT_SET;
    private int hour = NOT_SET;
    private int minute = NOT_SET;
    private float second = NOT_SET;
    private String timeZone = "Z";

    private boolean isDateComplete = false;
    private String timePos = "";
    private ITimeResolution timeRes;
    
    
    /**
     * constructs a timePosition out of a ISO 8601 String. The string has at least to indicate the year.
     * 
     * @param timePos
     * @throws IllegalArgumentException
     *         <ul>
     *         <li>if string ends with "T"</li>
     *         <li>if string is empty, <code>null</code> or -</li>
     *         <li>or does not match any additional pattern</li>
     *         </ul>
     */
    public TimePosition(String timePos) throws IllegalArgumentException {

        this.timePos = timePos;
        if (timePos == null) {
            throw new NullPointerException();
        }
        if (timePos.equals("")) {
            throw new IllegalArgumentException("empty String not allowed!");
        }
        if (timePos.endsWith("T")) {
            throw new IllegalArgumentException("timePos ends with T and does not contain any time information");
        }
        if (timePos.equalsIgnoreCase("now")) {
            // TODO set most recent available data
            throw new UnsupportedOperationException("for parameter 'now'");
        }

        String[] timePosArray = timePos.split("T");
        if (timePosArray.length <= 2) {
            initDate(timePosArray[0]);
            if (timePosArray.length == 2) {
                initTime(timePosArray[1]);
            }
        }
        else {
            throw new IllegalArgumentException("invalid timePosition!: " + timePos);
        }
    }

    private void initDate(String date) throws IllegalArgumentException {
        int negativeOffset = 0;
        if (date.equals("-")) {
            throw new IllegalArgumentException("date contains only \"-\"");
        }
        String[] dateArray = date.split("-");
        if ( ( !dateArray[0].equalsIgnoreCase("") && dateArray.length > 3) || dateArray[0].equalsIgnoreCase("")
                && dateArray.length > 4) {
            throw new IllegalArgumentException("date contains more than 3 parts");
        }
        if (date.startsWith("-")) {
            dateArray[1] = "-" + dateArray[1];
            negativeOffset = 1;
        }
        setYear(dateArray[0 + negativeOffset]);
        if (dateArray.length >= (2 + negativeOffset)) {
            setMonth(dateArray[1 + negativeOffset]);
        }
        if (dateArray.length == (3 + negativeOffset)) {
            setDay(dateArray[2 + negativeOffset]);
            isDateComplete = true;
        }
    }

    /**
     * Initializes the time (hours, minutes, seconds) after checking whether the date actually allows a time
     * meaning that a following time is only possible after a date that matches the scheme year-month-day.
     * Therefore initDate(String date) has to be called first otherwise this method will return an
     * IllegalArgumentException.
     */
    private void initTime(String time) throws IllegalArgumentException {
        if ( !isDateComplete) {
            throw new IllegalArgumentException("Date in: " + timePos
                    + " does not correspond to the year-month-day scheme" + "that is why a time is not allowed.");
        }
        String[] timeArray = time.split(":");
        Pattern utcPattern = Pattern.compile(UTC_PATTERN);
        Matcher utcMatcher = utcPattern.matcher(timeArray[timeArray.length - 1]);
        if ( !utcMatcher.matches()) {
            // throw new IllegalArgumentException("timeZone is not valid");
        }
        else {
            timeZone = utcMatcher.group(1);
        }
        if (timeArray.length > 4) {
            throw new IllegalArgumentException("time contains more than 3 parts");
        }
        String lastToken = timeArray[timeArray.length - 1];
        if (lastToken.endsWith("Z")) {
            timeArray[timeArray.length - 1] = lastToken.substring(0, lastToken.length() - 1);
        }
        if (timeArray.length >= 1) {
            setHour(timeArray[0]);
        }
        if (timeArray.length >= 2) {
            setMinute(timeArray[1]);
        }
        if (timeArray.length == 3 || timeArray.length == 4) {
            // changed to avoid parsing error with timeZone
            String seconds = timeArray[2];
            String[] secondAndTimeZonePlus = seconds.split("\\+");
            String[] secondAndTimeZoneMinus = seconds.split("\\-");
            if (secondAndTimeZonePlus.length == 1 && secondAndTimeZoneMinus.length == 1) {
                setSecond(seconds);
            }
            else {
                if (secondAndTimeZonePlus.length == 2) {
                    setSecond(secondAndTimeZonePlus[0]);
                    timeZone = "+" + secondAndTimeZonePlus[1];
                    
                    // set time zone minutes:
                    if(timeArray.length == 4) {
                        timeZone += ":" + timeArray[3];
                    }
                }
                else if (secondAndTimeZoneMinus.length == 2) {
                    setSecond(secondAndTimeZoneMinus[0]);
                    timeZone = "-" + secondAndTimeZoneMinus[1];
                    
                    // set time zone minutes:
                    if(timeArray.length == 4) {
                        timeZone += ":" + timeArray[3];
                    }
                }
            }

        }
    }

    private void setYear(String year) throws IllegalArgumentException {
        if (Pattern.matches(YEAR_PATTERN, year)) {
            this.setYear(Long.parseLong(year));
        }
        else {
            throw new IllegalArgumentException("year does not match pattern: applied Pattern: " + YEAR_PATTERN);
        }
    }

    private void setYear(long year) throws IllegalArgumentException {
        this.year = year;
    }

    private void setMonth(String month) throws IllegalArgumentException {
        if (Pattern.matches(MONTH_PATTERN, month)) {
            this.setMonth(Integer.parseInt(month));
        }
        else {
            throw new IllegalArgumentException("month does not match pattern: applied Pattern: " + MONTH_PATTERN);
        }
    }

    private void setMonth(int month) throws IllegalArgumentException {
        if (month < 13 && month > 0) {
            this.month = month;
        }
        else {
            throw new IllegalArgumentException("month is not an allowed value");
        }
    }

    private void setDay(String day) throws IllegalArgumentException {
        if (Pattern.matches(DAY_PATTERN, day)) {
            this.day = Integer.parseInt(day);
        }
        else {
            throw new IllegalArgumentException("day does not match pattern: applied Pattern: " + DAY_PATTERN);
        }
    }

    @SuppressWarnings("unused")
    private void setDay(int day) throws IllegalArgumentException {
        if (day < 32 && day > 0) {
            this.day = day;
        }
        else {
            throw new IllegalArgumentException("day is not an allowed value");
        }
    }

    private void setHour(String hour) throws IllegalArgumentException {
        if (Pattern.matches(HOUR_PATTERN, hour)) {
            this.setHour(Integer.parseInt(hour));
        }
        else {
            throw new IllegalArgumentException("hour does not match pattern: applied Pattern: " + HOUR_PATTERN);
        }

    }

    private void setHour(int hour) throws IllegalArgumentException {
        if (hour >= 0 && hour < 25) {
            this.hour = hour;
        }
        else {
            throw new IllegalArgumentException("hour is not an allowed value");
        }

    }

    private void setMinute(String minute) throws IllegalArgumentException {
        if (Pattern.matches(MINUTE_PATTERN, minute)) {
            this.setMinute(Integer.parseInt(minute));
        }
        else {
            throw new IllegalArgumentException("minute does not match pattern: applied Pattern: " + MINUTE_PATTERN);
        }

    }

    private void setMinute(int minute) throws IllegalArgumentException {
        if (minute >= 0 && minute < 60) {
            this.minute = minute;
        }
        else {
            throw new IllegalArgumentException("minute is not an allowed value");
        }

    }

    private void setSecond(String second) throws IllegalArgumentException {
        // TODO: hier kommt nen Hack (Arne):
        /*
         * { int plusIndex = second.indexOf("+"); if (plusIndex != -1) { second =
         * second.substring(0,plusIndex); } }
         */

        if (Pattern.matches(SECOND_PATTERN, second)) {
            this.setSecond(Float.parseFloat(second));
        }
        else {
            throw new IllegalArgumentException("second does not match pattern: applied Pattern: " + SECOND_PATTERN);
        }

    }

    private void setSecond(float second) throws IllegalArgumentException {
        if (second >= 0 && second < 60) {
            this.second = second;
        }
        else {
            throw new IllegalArgumentException("second is not an allowed value");
        }

    }

    public long getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public float getSecond() {
        return second;
    }

    /**
     * @return the timeZone
     */
    private String getTimeZone() {
        return timeZone;
    }

    public ITimeResolution getTimeResolution() {
        return timeRes;
    }

    public String toISO8601Format() {
        StringBuilder isoDate = new StringBuilder();
        isoDate.append(Long.toString(getYear()));
        if (this.getMonth() != NOT_SET) {
            isoDate.append("-");
            if (this.getMonth() < 10) {
                isoDate.append("0");
            }
            isoDate.append(this.getMonth());
        }
        else {
            return isoDate.toString();
        }
        if (this.getDay() != NOT_SET) {
            isoDate.append("-");
            if (this.getDay() < 10) {
                isoDate.append("0");
            }
            isoDate.append(this.getDay());
        }
        else {
            return isoDate.toString();
        }
        if (this.getHour() != NOT_SET) {
            isoDate.append("T");
            if (this.getHour() < 10) {
                isoDate.append("0");
            }
            isoDate.append(this.getHour());
        }
        else {
            return isoDate.toString();
        }
        if (this.getMinute() != NOT_SET) {
            isoDate.append(":");
            if (this.getMinute() < 10) {
                isoDate.append("0");
            }
            isoDate.append(this.getMinute());
        }
        else {
            isoDate.toString();
        }
        if (this.getSecond() != NOT_SET) {
            isoDate.append(":");
            if (this.getSecond() < 10) {
                isoDate.append("0");
            }
//            isoDate.append(new Double(this.getSecond()).intValue());
            String fullSec = new Double(this.getSecond()).toString();
            //3 nachkomma stellen erlauben
            int end = fullSec.indexOf(".")+4;
            if(end > fullSec.length()){
            	end = fullSec.length();
            }
            //Nachkommastellen entfernen falls nicht ben�tigt
            if(fullSec.endsWith(".0")){
            	// replaceAll with .0 leads to problems
            	// just build substring 
            	fullSec = fullSec.substring(0, fullSec.length()-2);
//            	fullSec = fullSec.replaceAll(".0", "");
            	end -=2;
            }
            isoDate.append(fullSec.substring(0,end));
        }
        else {
            return isoDate.toString();
        }

        if ( !this.timeZone.equals("Z")) {
            isoDate.append(timeZone);
        }

        // Should not be reached.
        return isoDate.toString();
    }

    /**
     * @return a String representation of this TimePosition object of the form: e.g.: 16.6.2006 14:53:12
     */
    @Override
    public String toString() {
        StringBuilder ordinaryDate = new StringBuilder();

        if (this.getDay() != NOT_SET) {
            ordinaryDate.append(this.getDay());
            ordinaryDate.append(".");
        }
        else {
            ordinaryDate.append("?");
        }
        if (this.getMonth() != NOT_SET) {
            ordinaryDate.append(this.getMonth());
            ordinaryDate.append(".");
        }
        else {
            ordinaryDate.append("?");
        }
        if (this.getYear() != NOT_SET) {
            ordinaryDate.append(this.getYear());
            ordinaryDate.append(" ");
        }
        else {
            ordinaryDate.append("?");
        }

        if (this.getHour() != NOT_SET) {
            ordinaryDate.append(this.getHour());
            ordinaryDate.append(":");
        }
        else {
            ordinaryDate.append("?");
        }
        if (this.getMinute() != NOT_SET) {
            ordinaryDate.append(this.getMinute());
            ordinaryDate.append(":");
        }
        else {
            ordinaryDate.append("?");
        }
        if (this.getSecond() != NOT_SET) {
            ordinaryDate.append(this.getSecond());
        }
        else {
            ordinaryDate.append("?");
        }

        if ( !this.getTimeZone().equals("Z")) {
            ordinaryDate.append(this.getTimeZone());
        }

        return ordinaryDate.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ITimePosition) {
            if (compareTo((ITimePosition) obj) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     * the specified object.
     * 
     * @param timePosP
     * @return -1 if this is before timePos; 1 if timePos is before this; 0 if this and timePos are equal.
     */
    public int compareTo(ITimePosition timePosP) {
        if (this.getYear() != timePosP.getYear()) {
            return (this.getYear() < timePosP.getYear()) ? -1 : 1;
        }
        if (this.getMonth() != timePosP.getMonth()) {
            return (this.getMonth() < timePosP.getMonth()) ? -1 : 1;
        }
        if (this.getDay() != timePosP.getDay()) {
            return (this.getDay() < timePosP.getDay()) ? -1 : 1;
        }
        if (this.getHour() != timePosP.getHour()) {
            return (this.getHour() < timePosP.getHour()) ? -1 : 1;
        }
        if (this.getMinute() != timePosP.getMinute()) {
            return (this.getMinute() < timePosP.getMinute()) ? -1 : 1;
        }
        if (this.getSecond() != timePosP.getSecond()) {
            return (this.getSecond() < timePosP.getSecond()) ? -1 : 1;
        }

        return 0;
    }

    /**
     * a negative integer, zero, or a positive integer as the first argument is less than, equal to, or
     * greater than the second.
     * 
     * @param o1
     * @param o2
     * @return
     */
    public int compare(ITimePosition o1, ITimePosition o2) {
        return o1.compareTo(o2);
    }

    /**
     * @return whether this TimePosition represents a time before the time represented by the specified
     *         TimePosition object. This method is equivalent to: compareTo(when) < 0
     */
    public boolean before(ITimePosition timePosP) {
        return compareTo(timePosP) < 0;
    }

    /**
     * @return whether this TimePosition represents a time after the time represented by the specified
     *         TimePosition object. This method is equivalent to: compareTo(when) > 0
     */
    public boolean after(ITimePosition timePosP) {
        return compareTo(timePosP) > 0;
    }

    public Calendar getCalendar() {
        return new GregorianCalendar((int) year, month - 1, day, hour, minute, (int) second);
    }

    public String getTimezone(){
    	return timeZone;
    }
    
    public void setTimezone(String timezone){
    	timeZone = timezone;
    }
    
    /**
     * testing
     * @param args
     */
    public static void main(String[] args)
    {
        String t1 = "2011-02-13T13:00:23+02:00";
        System.out.println(new TimePosition(t1).toISO8601Format());
    }
}