package org.n52.sos;

import static org.junit.Assert.fail;

import org.junit.Test;

public class SOSExtTest {

    @Test
    public void testGetSchema() {
        try {
            System.out.println(SosSoe.createSchema());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
