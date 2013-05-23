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
 * @author <a href="mailto:j.schulte@52north.org">Jan Schulte</a>
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class SubField {

    public static String OBSERVATION_PK_OBSERVATION;
    public static String OBSERVATION_ID;
    public static String OBSERVATION_FK_FEATUREOFINTEREST;
    public static String OBSERVATION_FK_SAMPLINGPOINT;
    public static String OBSERVATION_FK_PROCEDURE;
    public static String OBSERVATION_FK_PROPERTY;
    public static String OBSERVATION_FK_UNIT;
    
    public static String VALUE_PK_VALUE;
    public static String VALUE_FK_OBSERVATION;
    public static String VALUE_DATETIME_BEGIN;
    public static String VALUE_DATETIME_END;
    public static String VALUE_VALUE_TEXT;
    public static String VALUE_VALUE_NUMERIC;
    public static String VALUE_FK_VALIDITY;
    public static String VALUE_FK_VERIFICATION;
    public static String VALUE_DATETIME_INSERTED;
    public static String VALUE_DATETIME_UPDATED;
    public static String VALUE_RESULTTIME;
    public static String VALUE_FK_AGGREGATIONTYPE;
    
    public static String PROPERTY_PK_PROPERTY;
    public static String PROPERTY_ID;
    public static String PROPERTY_LABEL;
    public static String PROPERTY_NOTATION;
    public static String PROPERTY_DEFINITION;
    public static String PROPERTY_RESOURCE;

    public static String PROCEDURE_ID;
    public static String PROCEDURE_PK_PROCEDURE;
    public static String PROCEDURE_RESOURCE;

    public static String FEATUREOFINTEREST_OBJECTID;
    public static String FEATUREOFINTEREST_SHAPE;
    public static String FEATUREOFINTEREST_PK_FEATUREOFINTEREST;
    public static String FEATUREOFINTEREST_ID;
    public static String FEATUREOFINTEREST_RESOURCE;
    public static String FEATUREOFINTEREST_INLETHEIGHT;
    public static String FEATUREOFINTEREST_BUILDINGDISTANCE;
    public static String FEATUREOFINTEREST_KERBDISTANCE;

    public static String SAMPLINGPOINT_OBJECTID;
    public static String SAMPLINGPOINT_SHAPE;
    public static String SAMPLINGPOINT_PK_SAMPLINGPOINT;
    public static String SAMPLINGPOINT_ID;
    public static String SAMPLINGPOINT_RESOURCE;
    public static String SAMPLINGPOINT_FK_STATION;

    public static String STATION_OBJECTID;
    public static String STATION_SHAPE;
    public static String STATION_PK_STATION;
    public static String STATION_ID;
    public static String STATION_RESOURCE;
    public static String STATION_FK_NETWORK_GID;
    public static String STATION_DATETIME_OPEN;
    public static String STATION_DATETIME_CLOSED;
    public static String STATION_OPERATIONAL;

    public static String NETWORK_PK_NETWOK;
    public static String NETWORK_ID;

    public static String UNIT_PK_UNIT;
    public static String UNIT_ID;
    public static String UNIT_LABEL;
    public static String UNIT_NOTATION;
    public static String UNIT_DEFINITION;
    public static String UNIT_RESOURCE;

    public static String AGGREGATIONTYPE_PK_AGGREGATIONTYPE;
    public static String AGGREGATIONTYPE_ID;
    public static String AGGREGATIONTYPE_NOTATION;
    public static String AGGREGATIONTYPE_DEFINITION;
    public static String AGGREGATIONTYPE_RESOURCE;

    public static String VALIDITY_PK_VALIDITY;
    public static String VALIDITY_ID;
    public static String VALIDITY_NOTATION;
    public static String VALIDITY_DEFINITION;
    public static String VALIDITY_RESOURCE;

    public static String VERIFICATION_PK_VERIFICATION;
    public static String VERIFICATION_ID;
    public static String VERIFICATION_NOTATION;
    public static String VERIFICATION_DEFINITION;
    public static String VERIFICATION_RESOURCE;


    /**
     * initializes the table subfields based on the Properties.
     */
    public static void initSubfieldNames(Properties props) {

    	OBSERVATION_PK_OBSERVATION = props.getProperty("database.table.OBSERVATION.PK_OBSERVATION");
        OBSERVATION_ID = props.getProperty("database.table.OBSERVATION.ID");
        OBSERVATION_FK_FEATUREOFINTEREST = props.getProperty("database.table.OBSERVATION.FK_FEATUREOFINTEREST");
        OBSERVATION_FK_SAMPLINGPOINT = props.getProperty("database.table.OBSERVATION.FK_SAMPLINGPOINT");
        OBSERVATION_FK_PROCEDURE = props.getProperty("database.table.OBSERVATION.FK_PROCEDURE");
        OBSERVATION_FK_PROPERTY = props.getProperty("database.table.OBSERVATION.FK_PROPERTY");
        OBSERVATION_FK_UNIT = props.getProperty("database.table.OBSERVATION.FK_UNIT");
    
        VALUE_PK_VALUE = props.getProperty("database.table.VALUE.PK_VALUE");
    	VALUE_FK_OBSERVATION = props.getProperty("database.table.VALUE.FK_OBSERVATION");
    	VALUE_DATETIME_BEGIN = props.getProperty("database.table.VALUE.DATETIME_BEGIN");
    	VALUE_DATETIME_END = props.getProperty("database.table.VALUE.DATETIME_END");
    	VALUE_VALUE_TEXT = props.getProperty("database.table.VALUE.VALUE_TEXT");
    	VALUE_VALUE_NUMERIC = props.getProperty("database.table.VALUE.VALUE_NUMERIC");
    	VALUE_FK_VALIDITY = props.getProperty("database.table.VALUE.FK_VALIDITY");
    	VALUE_FK_VERIFICATION = props.getProperty("database.table.VALUE.FK_VERIFICATION");
    	VALUE_DATETIME_INSERTED = props.getProperty("database.table.VALUE.DATETIME_INSERTED");
    	VALUE_DATETIME_UPDATED = props.getProperty("database.table.VALUE.DATETIME_UPDATED");
    	VALUE_RESULTTIME = props.getProperty("database.table.VALUE.RESULTTIME");
    	VALUE_FK_AGGREGATIONTYPE = props.getProperty("database.table.VALUE.FK_AGGREGATION_TYPE");
    	
        PROPERTY_PK_PROPERTY = props.getProperty("database.table.PROPERTY.PK_PROPERTY");
        PROPERTY_ID = props.getProperty("database.table.PROPERTY.ID");
        PROPERTY_LABEL = props.getProperty("database.table.PROPERTY.LABEL");
        PROPERTY_NOTATION = props.getProperty("database.table.PROPERTY.NOTATION");
        PROPERTY_DEFINITION = props.getProperty("database.table.PROPERTY.DEFINITION");
        PROPERTY_RESOURCE = props.getProperty("database.table.PROPERTY.RESOURCE");

        PROCEDURE_ID = props.getProperty("database.table.PROCEDURE.ID");
        PROCEDURE_PK_PROCEDURE = props.getProperty("database.table.PROCEDURE.PK_PROCEDURE");
        PROCEDURE_RESOURCE = props.getProperty("database.table.PROCEDURE.RESOURCE");

        FEATUREOFINTEREST_OBJECTID = props.getProperty("database.table.FEATUREOFINTEREST.OBJECTID");
        FEATUREOFINTEREST_SHAPE = props.getProperty("database.table.FEATUREOFINTEREST.SHAPE");
        FEATUREOFINTEREST_PK_FEATUREOFINTEREST = props.getProperty("database.table.FEATUREOFINTEREST.PK_FEATUREOFINTEREST");
        FEATUREOFINTEREST_ID = props.getProperty("database.table.FEATUREOFINTEREST.ID");
        FEATUREOFINTEREST_RESOURCE = props.getProperty("database.table.FEATUREOFINTEREST.RESOURCE");
        FEATUREOFINTEREST_INLETHEIGHT = props.getProperty("database.table.FEATUREOFINTEREST.INLETHEIGHT");
        FEATUREOFINTEREST_BUILDINGDISTANCE = props.getProperty("database.table.FEATUREOFINTEREST.BUILDINGDISTANCE");
        FEATUREOFINTEREST_KERBDISTANCE = props.getProperty("database.table.FEATUREOFINTEREST.KERBDISTANCE");

        SAMPLINGPOINT_OBJECTID = props.getProperty("database.table.SAMPLINGPOINT.OBJECTID");
        SAMPLINGPOINT_SHAPE = props.getProperty("database.table.SAMPLINGPOINT.SHAPE");
        SAMPLINGPOINT_PK_SAMPLINGPOINT = props.getProperty("database.table.SAMPLINGPOINT.PK_SAMPLINGPOINT");
        SAMPLINGPOINT_ID = props.getProperty("database.table.SAMPLINGPOINT.ID");
        SAMPLINGPOINT_RESOURCE = props.getProperty("database.table.SAMPLINGPOINT.RESOURCE");
        SAMPLINGPOINT_FK_STATION = props.getProperty("database.table.SAMPLINGPOINT.FK_STATION");

        STATION_OBJECTID = props.getProperty("database.table.STATION.OBJECTID");
        STATION_SHAPE = props.getProperty("database.table.STATION.SHAPE");
        STATION_PK_STATION = props.getProperty("database.table.STATION.PK_STATION");
        STATION_ID = props.getProperty("database.table.STATION.ID");
        STATION_RESOURCE = props.getProperty("database.table.STATION.RESOURCE");
        STATION_FK_NETWORK_GID = props.getProperty("database.table.STATION.FK_NETWORK_GID");
        STATION_DATETIME_OPEN = props.getProperty("database.table.STATION.DATETIME_OPEN");
        STATION_DATETIME_CLOSED = props.getProperty("database.table.STATION.DATETIME_CLOSED");
        STATION_OPERATIONAL = props.getProperty("database.table.STATION.OPERATIONAL");

        NETWORK_PK_NETWOK = props.getProperty("database.table.NETWORK.PK_NETWORK");
        NETWORK_ID = props.getProperty("database.table.NETWORK.ID");

        UNIT_PK_UNIT = props.getProperty("database.table.UNIT.PK_UNIT");
        UNIT_ID = props.getProperty("database.table.UNIT.ID");
        UNIT_LABEL = props.getProperty("database.table.UNIT.LABEL");
        UNIT_NOTATION = props.getProperty("database.table.UNIT.NOTATION");
        UNIT_DEFINITION = props.getProperty("database.table.UNIT.DEFINITION");
        UNIT_RESOURCE = props.getProperty("database.table.UNIT.RESOURCE");

        AGGREGATIONTYPE_PK_AGGREGATIONTYPE = props.getProperty("database.table.AGGREGATIONTYPE.PK_AGGREGATIONTYPE");
        AGGREGATIONTYPE_ID = props.getProperty("database.table.AGGREGATIONTYPE.ID");
        AGGREGATIONTYPE_NOTATION = props.getProperty("database.table.AGGREGATIONTYPE.NOTATION");
        AGGREGATIONTYPE_DEFINITION = props.getProperty("database.table.AGGREGATIONTYPE.DEFINITION");
        AGGREGATIONTYPE_RESOURCE = props.getProperty("database.table.AGGREGATIONTYPE.RESOURCE");

        VALIDITY_PK_VALIDITY = props.getProperty("database.table.VALIDITY.PK_VALIDITY");
        VALIDITY_ID = props.getProperty("database.table.VALIDITY.ID");
        VALIDITY_NOTATION = props.getProperty("database.table.VALIDITY.NOTATION");
        VALIDITY_DEFINITION = props.getProperty("database.table.VALIDITY.DEFINITION");
        VALIDITY_RESOURCE = props.getProperty("database.table.VALIDITY.RESOURCE");

        VERIFICATION_PK_VERIFICATION = props.getProperty("database.table.VERIFICATION.PK_VERIFICATION");
        VERIFICATION_ID = props.getProperty("database.table.VERIFICATION.ID");
        VERIFICATION_NOTATION = props.getProperty("database.table.VERIFICATION.NOTATION");
        VERIFICATION_DEFINITION = props.getProperty("database.table.VERIFICATION.DEFINITION");
        VERIFICATION_RESOURCE = props.getProperty("database.table.VERIFICATION.RESOURCE");
    }
}
