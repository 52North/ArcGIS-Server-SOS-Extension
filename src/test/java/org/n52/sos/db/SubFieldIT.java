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
package org.n52.sos.db;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.n52.sos.db.impl.SubField;

public class SubFieldIT {

    private Properties props;

    @Before
    public void setUp() throws Exception
    {
        // Load properties for data access
        props = new Properties();
        props.load(getClass().getResourceAsStream("/arcGisSos.properties"));
    }

    /**
     * checks whether all subfields are initialized by reading out the properties.
     */
    @Test
    public void testInitSubfieldNames()
    {
        SubField.initSubfieldNames(props);

        // check if all attribute fields are initialized:
        Field[] fields = SubField.class.getFields();
        try {
            for (Field field : fields) {
                System.out.println(field.getName());

                // let's get the value and check whether it's null:
                String value = (String) field.get(null);
                if (value == null) {
                    fail();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public static void main(String[] args) throws Exception
    {
        SubFieldIT st = new SubFieldIT();
        st.setUp();
        st.testInitSubfieldNames();
    }
}
