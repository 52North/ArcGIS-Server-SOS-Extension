package org.n52.sos.db;

import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;
import org.n52.sos.it.EsriTestBase;
import org.n52.sos.it.ITConstants;

public class AccessGdbForProceduresIT extends EsriTestBase {

    static Logger LOGGER = Logger.getLogger(AccessGdbForProceduresIT.class.getName());
    

    @Test
    public void testGetProcedureIdList()
    {
        try {
            List<String> procedureIDs = gdb.getProcedureAccess().getProcedureIdList();
            
            for (String procedureID : procedureIDs) {
                System.out.println(procedureID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetProcedures()
    {
        String[] procedureIdentifiers1 = { "GB_StationProcess_1" };
        String[] procedureIdentifiers2 = { "GB_StationProcess_2", "GB_StationProcess_3" };

        try {
            System.out.println(gdb.getProcedureAccess().getProcedures(procedureIdentifiers1).toArray()[0]);
            System.out.println(gdb.getProcedureAccess().getProcedures(procedureIdentifiers2).toArray()[0]);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetProceduresForNetwork()
    {
    	String networkID = ITConstants.NETWORK_ID;
    	
    	try {
            System.out.println(gdb.getProcedureAccess().getProceduresForNetwork(networkID).toArray()[0]);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
