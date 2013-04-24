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

package org.n52.sos.db;

import java.util.Properties;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 * @author <a href="mailto:j.schulte@52north.org">Jan Schulte</a>
 */
public class Table {

    public static String OBSERVATION;
    public static String VALUE;
    public static String PROPERTY;
    public static String PROCEDURE;
    public static String FEATUREOFINTEREST;
    public static String SAMPLINGPOINT;
    public static String STATION;
    public static String NETWORK;
    public static String UNIT;
    public static String AGGREGATIONTYPE;
    public static String VALIDITY;
    public static String VERIFICATION;

    /**
     * initializes the table names from Properties.
     */
    public static void initTableNames(Properties props)
    {
        OBSERVATION = props.getProperty("database.table.OBSERVATION");
        VALUE = props.getProperty("database.table.VALUE");
        PROPERTY = props.getProperty("database.table.PROPERTY");
        PROCEDURE = props.getProperty("database.table.PROCEDURE");
        FEATUREOFINTEREST = props.getProperty("database.table.FEATUREOFINTEREST");
        SAMPLINGPOINT = props.getProperty("database.table.SAMPLINGPOINT");
        STATION = props.getProperty("database.table.STATION");
        NETWORK = props.getProperty("database.table.NETWORK");
        UNIT = props.getProperty("database.table.UNIT");
        AGGREGATIONTYPE = props.getProperty("database.table.AGGREGATIONTYPE");
        VALIDITY = props.getProperty("database.table.VALIDITY");
        VERIFICATION = props.getProperty("database.table.VERIFICATION");
    }
    
    public static boolean hasTableName(String name) {
        if (OBSERVATION.equalsIgnoreCase(name)) {
            return true;
        }
        else if (VALUE.equalsIgnoreCase(name)) {
            return true;
        }
        else if (PROPERTY.equalsIgnoreCase(name)) {
            return true;
        }
        else if (PROCEDURE.equalsIgnoreCase(name)) {
            return true;
        }
        else if (FEATUREOFINTEREST.equalsIgnoreCase(name)) {
            return true;
        }
        else if (SAMPLINGPOINT.equalsIgnoreCase(name)) {
            return true;
        }
        else if (STATION.equalsIgnoreCase(name)) {
            return true;
        }
        else if (NETWORK.equalsIgnoreCase(name)) {
            return true;
        }
        else if (UNIT.equalsIgnoreCase(name)) {
            return true;
        }
        else if (AGGREGATIONTYPE.equalsIgnoreCase(name)) {
            return true;
        }
        else if (VALIDITY.equalsIgnoreCase(name)) {
            return true;
        }
        else if (VERIFICATION.equalsIgnoreCase(name)) {
            return true;
        }
        else {
            return false;
        }
    }
}
