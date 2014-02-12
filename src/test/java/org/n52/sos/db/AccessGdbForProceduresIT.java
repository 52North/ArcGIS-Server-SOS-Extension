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

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;
import org.n52.sos.dataTypes.Procedure;
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
    public void testGetProceduresWithIdAndResource()
    {
        String[] procedureIdentifiers1 = { "GB_StationProcess_1" };
        String[] procedureIdentifiers2 = { "GB_StationProcess_2", "GB_StationProcess_3" };

        try {
        	System.out.println(gdb.getProcedureAccess().getProceduresWithIdAndResource(procedureIdentifiers2).toArray()[0]);
        	
        	System.out.println(gdb.getProcedureAccess().getProceduresWithIdAndResource(procedureIdentifiers1).toArray()[0]);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void testGetProceduresComplete()
    {
        String[] procedureIdentifiers1 = { ITConstants.PROCEDURE_RESOURCE };
     
        try {
        	System.out.println(gdb.getProcedureAccess().getProceduresComplete(procedureIdentifiers1).toArray()[0]);
            
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
            Collection<Procedure> procedures = gdb.getProcedureAccess().getProceduresForNetwork(networkID);
            
            int counter = 1;
            for (Procedure procedure : procedures) {
				System.out.println(counter++ + ".: " + procedure);
			}
            
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
