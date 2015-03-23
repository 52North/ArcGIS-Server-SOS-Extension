/**
 * Copyright (C) 2012 52°North Initiative for Geospatial Open Source Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.sos.db.impl;

import java.util.Properties;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 * @author <a href="mailto:j.schulte@52north.org">Jan Schulte</a>
 */
public class Table {

    private static final CharSequence DATABASE_PLACEHOLDER = "@@database_name@@";
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
     * @param databaseName 
     */
    public static void initTableNames(Properties props, String databaseName)
    {
        OBSERVATION = props.getProperty("database.table.OBSERVATION").replace(DATABASE_PLACEHOLDER, databaseName);
        VALUE = props.getProperty("database.table.VALUE").replace(DATABASE_PLACEHOLDER, databaseName);
        PROPERTY = props.getProperty("database.table.PROPERTY").replace(DATABASE_PLACEHOLDER, databaseName);
        PROCEDURE = props.getProperty("database.table.PROCEDURE").replace(DATABASE_PLACEHOLDER, databaseName);
        FEATUREOFINTEREST = props.getProperty("database.table.FEATUREOFINTEREST").replace(DATABASE_PLACEHOLDER, databaseName);
        SAMPLINGPOINT = props.getProperty("database.table.SAMPLINGPOINT").replace(DATABASE_PLACEHOLDER, databaseName);
        STATION = props.getProperty("database.table.STATION").replace(DATABASE_PLACEHOLDER, databaseName);
        NETWORK = props.getProperty("database.table.NETWORK").replace(DATABASE_PLACEHOLDER, databaseName);
        UNIT = props.getProperty("database.table.UNIT").replace(DATABASE_PLACEHOLDER, databaseName);
        AGGREGATIONTYPE = props.getProperty("database.table.AGGREGATIONTYPE").replace(DATABASE_PLACEHOLDER, databaseName);
        VALIDITY = props.getProperty("database.table.VALIDITY").replace(DATABASE_PLACEHOLDER, databaseName);
        VERIFICATION = props.getProperty("database.table.VERIFICATION").replace(DATABASE_PLACEHOLDER, databaseName);
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
