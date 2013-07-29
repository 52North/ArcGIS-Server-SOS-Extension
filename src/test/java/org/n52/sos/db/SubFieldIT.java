package org.n52.sos.db;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.n52.sos.db.AccessGDB;
import org.n52.sos.db.SubField;

public class SubFieldIT {

    private Properties props;

    @Before
    public void setUp() throws Exception
    {
        // Load properties for data access
        props = new Properties();
        props.load(AccessGDB.class.getResourceAsStream("/arcGisSos.properties"));
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
