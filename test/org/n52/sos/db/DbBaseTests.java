/**
 * 
 */
package org.n52.sos.db;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Logger;

import org.junit.Test;
import org.n52.sos.EsriBaseTest;

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
 * @author Arne
 *
 */
public class DbBaseTests extends EsriBaseTest {

    static Logger LOGGER = Logger.getLogger(DbBaseTests.class.getName());

    /**
     * checks whether the connection to the DB can be established and prints out
     * the names of all dataset names contained in the DB.
     */
    @Test
    public void testConnection() {
        try {
            LOGGER.info("Workspace name: " + gdb.getWorkspace().getName());
            
            IEnumDatasetName datasetNames = gdb.getWorkspace().getDatasetNames(esriDatasetType.esriDTAny);
            
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
                    assertTrue(checkPresenceOfTable(fieldName));
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
        IEnumDatasetName datasetNames = gdb.getWorkspace().getDatasetNames(esriDatasetType.esriDTAny);
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
     * Checks whether all subfields as defined in property file are present in DB.
     */
    @Test
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
                    assertTrue(checkPresenceOfSubfield(fieldName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * helper method that returns true if a subfieldName is used as a datasetName in the DB.
     */
    private boolean checkPresenceOfSubfield(String subfieldName) throws AutomationException, IOException {
        IEnumDataset datasets = gdb.getWorkspace().getDatasets(esriDatasetType.esriDTAny);
        IDataset dataset = datasets.next();
        
        while(dataset != null) {
            int typeID = dataset.getType();
            if (typeID == esriDatasetType.esriDTTable) {
                ITable table = gdb.getWorkspace().openTable(dataset.getName());
                IFields fields = table.getFields();
                if (fields.findField(subfieldName) != -1) {
                    return true;
                }
            }
            else if (typeID == esriDatasetType.esriDTFeatureClass) {
                IFeatureClass featureClass = gdb.getWorkspace().openFeatureClass(dataset.getName());
                IFields fields = featureClass.getFields();
                if (fields.findField(subfieldName) != -1) {
                    return true;
                }
            }
            else {
                throw new RuntimeException("Type not supported");
            }
            dataset = datasets.next();
        }
        return false;
    }
}

