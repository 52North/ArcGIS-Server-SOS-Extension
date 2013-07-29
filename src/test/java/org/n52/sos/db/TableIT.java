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

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.n52.sos.db.AccessGDB;
import org.n52.sos.db.Table;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class TableIT {

    private Properties props;

    @Before
    public void setUp() throws Exception
    {
        // Load properties for data access
        props = new Properties();
        props.load(AccessGDB.class.getResourceAsStream("/arcGisSos.properties"));
    }

    @Test
    public void testInitTableNames()
    {
        Table.initTableNames(props);

        if (Table.OBSERVATION == null) {
            fail();
        }
        if (Table.VALUE == null) {
            fail();
        }
        if (Table.PROPERTY == null) {
            fail();
        }
        if (Table.PROCEDURE == null) {
            fail();
        }
        if (Table.FEATUREOFINTEREST == null) {
            fail();
        }
        if (Table.SAMPLINGPOINT == null) {
            fail();
        }
        if (Table.STATION == null) {
            fail();
        }
        if (Table.NETWORK == null) {
            fail();
        }
        if (Table.UNIT == null) {
            fail();
        }
        if (Table.AGGREGATIONTYPE == null) {
            fail();
        }
        if (Table.VALIDITY == null) {
            fail();
        }
        if (Table.VERIFICATION == null) {
            fail();
        }

    }

}
