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
/**
 * 
 */
package org.n52.sos.db.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Logger;

import javax.activation.UnsupportedDataTypeException;

import org.junit.Assert;
import org.junit.Test;
import org.n52.sos.db.impl.SubField;
import org.n52.sos.db.impl.Table;
import org.n52.sos.it.EsriTestBase;

import com.esri.arcgis.geodatabase.IDataset;
import com.esri.arcgis.geodatabase.IDatasetName;
import com.esri.arcgis.geodatabase.IEnumDataset;
import com.esri.arcgis.geodatabase.IEnumDatasetName;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.esriDatasetType;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 *
 */
public class DbBaseIT extends EsriTestBase {

    static Logger LOGGER = Logger.getLogger(DbBaseIT.class.getName());

    public static void main(String[] args) throws Exception {
    	DbBaseIT test = new DbBaseIT();
		test.setUp();
		test.testConnection();
	}
    
    /**
     * checks whether the connection to the DB can be established and prints out
     * the names of all dataset names contained in the DB.
     */
    @Test
    public void testConnection() {
        try {
            LOGGER.info("Workspace name: " + gdb.getWorkspace().getWorkspace().getName());
            
            IEnumDatasetName datasetNames = gdb.getWorkspace().getWorkspace().getDatasetNames(esriDatasetType.esriDTAny);
            
            LOGGER.info("Print dataset names:");
            IDatasetName dName = datasetNames.next();
            while(dName != null) {
                System.out.println(dName.getName());
                dName = datasetNames.next();
            }
        } catch (AutomationException e) {
            e.printStackTrace();
            fail();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * Checks whether all tables as defined in property file are present in DB.
     */
    @Test
    public void testIfAllTablesArePresent() {
        // check if all attribute fields are initialized:
        Field[] fields = Table.class.getFields();
        try {
            for (Field field : fields) {
                // let's get the fieldName and check (if not null) whether it's present in the DB:
                String fieldName = (String) field.get(null);
                if (fieldName == null) {
                    fail();
                } else {
                    LOGGER.info("Checking if table '" + field.getName() + "' with value '" + fieldName + "' is present in DB.");
                    Assert.assertTrue(checkPresenceOfTable(fieldName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * helper method that returns true if a tableName is used as a datasetName in the DB.
     */
    private boolean checkPresenceOfTable(String tableName) throws AutomationException, IOException {
        IEnumDatasetName datasetNames = gdb.getWorkspace().getWorkspace().getDatasetNames(esriDatasetType.esriDTAny);
        IDatasetName dName = datasetNames.next();
        while(dName != null) {
            String dNameString = dName.getName();
            if (dNameString.equalsIgnoreCase(tableName)) {
                return true;
            }
            else {
                dName = datasetNames.next();
            }
        }
        return false;
    }
    
    /**
     * Checks whether all subfields as defined in property file are present in
     * DB.
     * 
     * @deprecated since this method only checks whether a subfield is present
     *             in any table of the database and not a specific table. In
     *             conclusion, this results in false positives. A solution is
     *             not straightforward to implement since it would require a
     *             reflection of table-subfield relationships in the properties
     *             file.
     */
    @Test
    @Deprecated
    public void testIfAllSubfieldsArePresent() {
        // check if all attribute fields are initialized:
        Field[] fields = SubField.class.getFields();
        try {
            for (Field field : fields) {
                // let's get the fieldName and check (if not null) whether it's present in the DB:
                String fieldName = (String) field.get(null);
                if (fieldName == null) {
                    fail();
                } else {
                    LOGGER.info("Checking if subfield '" + field.getName() + "' with value '" + fieldName + "' is present in DB.");
                    Assert.assertTrue(checkPresenceOfSubfield(fieldName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * helper method that returns true if a subfieldName is used by a dataset in the DB.
     */
    private boolean checkPresenceOfSubfield(String subfieldName) throws AutomationException, IOException {
        IEnumDataset datasets = gdb.getWorkspace().getWorkspace().getDatasets(esriDatasetType.esriDTAny);
        IDataset dataset = datasets.next();
        
        while (dataset != null) {
            
            int typeID = dataset.getType();
            
            if (typeID == esriDatasetType.esriDTTable) {
                ITable table = gdb.getWorkspace().getWorkspace().openTable(dataset.getName());
                IFields fields = table.getFields();
                if (fields.findField(subfieldName) != -1) {
                    return true;
                } 
            }
            else if (typeID == esriDatasetType.esriDTFeatureClass) {
                IFeatureClass featureClass = gdb.getWorkspace().getWorkspace().openFeatureClass(dataset.getName());
                IFields fields = featureClass.getFields();
                if (fields.findField(subfieldName) != -1) {
                    return true;
                } 
            }
            else {
                throw new UnsupportedDataTypeException("Type not supported");
            }
            
            dataset = datasets.next();
        }
        return false;
    }
}

