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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimeConverter;
import org.n52.sos.SosSoe;
import org.n52.sos.dataTypes.ContactDescription;
import org.n52.sos.dataTypes.ServiceDescription;
import org.n52.sos.db.AccessGDB;
import org.n52.sos.db.AccessGdbForAnalysis;
import org.n52.sos.db.AccessGdbForFeatures;
import org.n52.sos.db.AccessGdbForObservations;
import org.n52.sos.db.AccessGdbForOfferings;
import org.n52.sos.db.AccessGdbForProcedures;
import org.n52.sos.db.InsertGdbForObservations;
import org.n52.util.logging.Logger;

import com.esri.arcgis.carto.IMapServer3;
import com.esri.arcgis.carto.IMapServerDataAccess;
import com.esri.arcgis.datasourcesGDB.SdeWorkspaceFactory;
import com.esri.arcgis.datasourcesGDB.SqlWorkspace;
import com.esri.arcgis.geodatabase.Feature;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IMemoryRelationshipClassFactory;
import com.esri.arcgis.geodatabase.IObjectClass;
import com.esri.arcgis.geodatabase.IObjectClassProxy;
import com.esri.arcgis.geodatabase.IRelQueryTableFactory;
import com.esri.arcgis.geodatabase.IRelationshipClass;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.ISpatialFilter;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.IWorkspaceFactory;
import com.esri.arcgis.geodatabase.MemoryRelationshipClassFactory;
import com.esri.arcgis.geodatabase.RelQueryTableFactory;
import com.esri.arcgis.geodatabase.SpatialFilter;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriFieldType;
import com.esri.arcgis.geodatabase.esriRelCardinality;
import com.esri.arcgis.geodatabase.esriSpatialRelEnum;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.json.JSONObject;
import com.esri.arcgis.system.ServerUtilities;

/**
 * This class is responsible for the database connection and access.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 * @author <a href="mailto:j.schulte@52north.org">Jan Schulte</a>
 * 
 */
public class AccessGDBImpl implements AccessGDB {

    static Logger LOGGER = Logger.getLogger(AccessGDBImpl.class.getName());

    private SosSoe sos;

    private Properties props;
    
    private int maxNumberOfResults;
    
    private ServiceDescription serviceDescription;
    
    private AccessGdbForObservations observationAccess;
    private AccessGdbForFeatures featureAccess;
    private AccessGdbForProcedures procedureAccess;
    private AccessGdbForOfferings offeringAccess;
    private AccessGdbForAnalysis analysisAccess;
    private InsertGdbForObservations observationInsert;

	private String databaseName;

	private WorkspaceWrapper workspaceWrapper;

    /**
     * Creates an AccessObservationGDB object and connects to the DB specified
     * in the arcGisSosLocal.properties file.
     * 
     * This constructor is for testing this class NOT for usage in the ArcGIS
     * Server.
     * 
     * @throws AutomationException
     * @throws IOException
     */
    public AccessGDBImpl() throws AutomationException, IOException {

        LOGGER.info("Creating AccessGDBImpl.");
        
        init("/arcGisSosLocal.properties", 0);
        
        // Load property for data access
        String dbPath = props.getProperty("database.path");
        LOGGER.info("Using database connection '" + dbPath + "'.");
        
        // Workspace creation
        IWorkspaceFactory factory = new SdeWorkspaceFactory();// FileGDBWorkspaceFactory();
        workspaceWrapper = new WorkspaceWrapper();
        workspaceWrapper.setWorkspace(new Workspace(factory.openFromFile(dbPath, 0)));
    }
    
    /**
     * Creates an AccessObservationGDB object and connects to the DB of the
     * ArcGIS MapServer handed over as a parameter.
     * 
     * @throws AutomationException
     * @throws IOException
     */
    public AccessGDBImpl(SosSoe sos) throws AutomationException, IOException {
        
        LOGGER.info("Creating AccessGDBImpl.");
        
        long start = System.currentTimeMillis();
        
        this.sos = sos;
        
        // Workspace creation
        IMapServer3 ms = (IMapServer3) sos.getMapServerDataAccess();
        String mapName = ms.getDefaultMapName();
        LOGGER.info("Using mapName: "+mapName);
        IMapServerDataAccess mapServerDataAccess = sos.getMapServerDataAccess();
        LOGGER.info("Using IMapServerDataAccess: "+mapServerDataAccess);
        Object dataSource= mapServerDataAccess.getDataSource(mapName, 0);
        LOGGER.info("Using dataSource: "+dataSource.getClass());
        FeatureClass fc = new FeatureClass(dataSource);
        resolveDatabaseName(fc);
        Workspace workspace = new Workspace(fc.getWorkspace());
        this.workspaceWrapper = new WorkspaceWrapper();
        
//        logConnectionProperties(fc.getWorkspace());
        
        if (fc.getWorkspace() instanceof SqlWorkspace) {
        	this.workspaceWrapper.setSqlWorkspace((SqlWorkspace) fc.getWorkspace());
        	this.workspaceWrapper.setWorkspace(workspace);
        }
        else {
//        	SqlWorkspaceFactory fac = new SqlWorkspaceFactory();
//        	SqlWorkspace sqlW = (SqlWorkspace) fac.open(fc.getWorkspace().getConnectionProperties(), fc.getWorkspace().getType());
//        	workspace = new Workspace(sqlW);
//        	this.workspaceWrapper.setSqlWorkspace(sqlW);
        	this.workspaceWrapper.setWorkspace(workspace);
        }
        
        LOGGER.info("workspace: "+this.workspaceWrapper.toString());

        init("/arcGisSos.properties", sos.getMaximumRecordCount());
        
        long delta = System.currentTimeMillis() - start;
        
        LOGGER.info("End of creating AccessGDBImpl. Created in " + delta/1000 + " seconds.");
    }
    
//    private void logConnectionProperties(IWorkspace workspace) throws IOException {
//    	Object[] target1 = new Object[1];
//    	Object[] target2 = new Object[1];
//    	workspace.getConnectionProperties().getAllProperties(target1, target2);
//
//    	int i = 0;
//    	for (Object object : target1) {
//    		LOGGER.info(i +"a="+createPresentation(object));
//    		i++;
//		}
//    	
//    	i = 0;
//    	for (Object object : target2) {
//    		LOGGER.info(i +"b="+createPresentation(object));
//    		i++;
//		}
//	}
//
//	private String createPresentation(Object object) {
//		StringBuilder sb = new StringBuilder();
//		if (object == null) {
//			return "n/a";
//		}
//		if (object instanceof CharSequence) {
//			return object.toString();
//		}
//		if (object instanceof Object[]) {
//			sb.append("[");
//			for (Object o : (Object[]) object) {
//				sb.append(o);
//				sb.append(", ");
//			}
//			sb.append("]");
//			return sb.toString();
//		}
//		return object.toString();
//	}

	private void resolveDatabaseName(FeatureClass fc) throws IOException {
    	String name = fc.getName();
    	int lastIndex = name.lastIndexOf(".");
    	this.databaseName = name.substring(0, lastIndex).trim();
    	LOGGER.info("databaseName = "+this.databaseName);
	}

	/**
     * initialization of local variables.
     * 
     * @param propsResourceName
     * @param maxRecords 
     * @throws IOException
     */
    public void init(String propsResourceName, int maxRecords) throws IOException {
        
        props = new Properties();
        
        // Load properties
        props.load(AccessGDBImpl.class.getResourceAsStream(propsResourceName));
        
        // init the table names for accessing the geodatabase:
        Table.initTableNames(props, this.databaseName);

        // init the field names:
        SubField.initSubfieldNames(props);

        // init maxNumberOfResults:
        if (maxRecords == 0) {
        	maxNumberOfResults = Integer.parseInt(props.getProperty("database.maxNumberOfResults"));
        } else {
        	maxNumberOfResults = maxRecords;
        }
        
        observationAccess = new AccessGdbForObservationsImpl(this);
        featureAccess = new AccessGdbForFeaturesImpl(this);
        procedureAccess = new AccessGdbForProceduresImpl(this);
        offeringAccess = new AccessGdbForOfferingsImpl(this);
        observationInsert = new InsertGdbForObservationsImpl(this);
    }
    
    @Override
    public String getDatabaseName() {
    	return this.databaseName;
    }

    /**
     * This method can be used to retrieve a {@link ServiceDescription} of the
     * SOS.
     * 
     * @throws IOException 
     */
    public ServiceDescription getServiceDescription() throws IOException
    {
        if (this.serviceDescription == null) {
            LOGGER.verbose("Creating new ServiceDescription.");
            
            String title = sos.getSosTitle();
            String description = sos.getSosDescription();
            String[] keywordArray = sos.getSosKeywords().split(",");
            String providerName = sos.getSosProviderName();
            String providerSite = sos.getSosProviderSite();        
            String individualName = sos.getSosContactPersonName();
            String positionName = sos.getSosContactPersonPosition();
            String phone = sos.getSosContactPersonPhone();
            String facsimile = sos.getSosContactPersonFax();
            String deliveryPoint = sos.getSosContactPersonAddress();
            String city = sos.getSosContactPersonCity();
            String administrativeArea = sos.getSosContactPersonAdminArea();
            String postalCode  = sos.getSosContactPersonPostalCode();
            String country = sos.getSosContactPersonCountry();
            String electronicMailAddress = sos.getSosContactPersonEmail();
         
            ContactDescription serviceContact = new ContactDescription(individualName, positionName, phone, facsimile, deliveryPoint, city, administrativeArea, postalCode, country, electronicMailAddress);
            
            // request procedure IDs
            List<String> procedureIdList = procedureAccess.getProcedureIdList();

            this.serviceDescription = new ServiceDescription(title, description, keywordArray, providerName, providerSite, new ContactDescription[]{serviceContact}, procedureIdList); 
        }
        
        return this.serviceDescription;
    }

    @Override
    public AccessGdbForProcedures getProcedureAccess() {
        return procedureAccess;
    }

    @Override
    public AccessGdbForFeatures getFeatureAccess() {
        return featureAccess;
    }

    @Override
    public AccessGdbForObservations getObservationAccess() {
    	return observationAccess;
    }
    
    @Override
    public AccessGdbForOfferings getOfferingAccess() {
        return offeringAccess;
    }
    
    @Override
    public AccessGdbForAnalysis getAnalysisAccess() {
        return analysisAccess;
    }
    
    @Override
    public InsertGdbForObservations getObservationInsert() {
        return observationInsert;
    }
    
    ///////////////////////////////
    // //////////////////////////// Helper Methods:
    ///////////////////////////////	

    public static ITimePosition createTimePosition(Object startValue)
    {
        ITimePosition timePosition = null;
        if (startValue != null && startValue instanceof Date) {
        	return TimeConverter.createTimePosition((Date) startValue);
        }
        return timePosition;
    }

    /**
     * This method creates a comma separated string of a list of string entries
     * 
     * @param list
     *            List of entries
     * @return a comma separated string
     */
    public static String createCommaSeparatedList(List<String> list)
    {
        StringBuilder sb = new StringBuilder();
        for (String entry : list) {
            sb.append(entry);
            sb.append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }
    
    public static String createCommaSeparatedList(String... list) {
    	return createCommaSeparatedList(Arrays.asList(list));
    }

    /**
     * This method creates an or-clause out of a given field and a list of
     * strings, where all entries of the list will be compared with the field.
     * 
     * @param field
     *            a database field
     * @param list
     *            a String array representing the list of values that are
     *            compared with the field.
     * @return the created or-clause
     */
    public static String createOrClause(String field,
            String[] list)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (String entry : list) {
            sb.append(field);
            sb.append(" = '");
            sb.append(entry);
            sb.append("'");
            sb.append(" OR ");
        }
        sb.delete(sb.length() - 4, sb.length());
        sb.append(")");
        return sb.toString();
    }

    /**
     * This method creates an or-clause out of a given field and a Collection of
     * Integers, where all Integer values will be compared with the field.
     * 
     * @param field
     *            a database field
     * @param list
     *            a Collection of Integers representing the list of values that
     *            are compared with the field.
     * @return the created or-clause
     */
    public static String createOrClause(String field,
            Collection<Integer> list)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Integer entry : list) {
            sb.append(field + " = '" + entry + "'");
            sb.append(" OR ");
        }
        sb.delete(sb.length() - 4, sb.length());
        sb.append(")");
        return sb.toString();
    }

    /**
     * 
     * @param spatialFilter
     * @return
     * @throws Exception
     */
    protected Collection<String> queryFeatureIDsForSpatialFilter(String spatialFilter) throws IOException
    {
        IGeometry geometry;
		try {
			geometry = ServerUtilities.getGeometryFromJSON(new JSONObject(spatialFilter));
		} catch (Exception e) {
			throw new IOException(e);
		}
        IFeatureClass features = workspaceWrapper.getWorkspace().openFeatureClass(Table.FEATUREOFINTEREST);
        ISpatialFilter spatialQuery = new SpatialFilter();
        spatialQuery.setGeometryByRef(geometry);
        spatialQuery.setGeometryField(features.getShapeFieldName());
        spatialQuery.setSpatialRel(esriSpatialRelEnum.esriSpatialRelIntersects);
        spatialQuery.setSubFields(SubField.FEATUREOFINTEREST_ID);
        IFeatureCursor featureCursor = features.search(spatialQuery, true);

        IFeature feature = featureCursor.nextFeature();
        List<String> featureList = new ArrayList<String>();
        while (feature != null) {
            featureList.add((String)feature.getValue(0));
            feature = featureCursor.nextFeature();
        }

        return featureList;
    }

    /**
     * Concatenates names of a database table and field with a '.'.
     */
    public static String concatTableAndField(String table,
            String field)
    {
        return String.format("%s.%s", table, field);
    }
    
    /**
     * @return table1.field1 = table2.field2
     */
    public static String join(String table1, String field1, String table2, String field2) {
    	return String.format("%s = %s", concatTableAndField(table1, field1), concatTableAndField(table2, field2));
    }
    
    public static String innerJoin(String table1, String field1, String table2, String field2) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("INNER JOIN ");
    	sb.append(table2);
    	sb.append(" ON ");
    	sb.append(join(table1, field1, table2, field2));
    	
    	return sb.toString();
    }

    /**
     * 
     * @param targetTable
     * @param joinTable
     * @param fromField
     * @param toField
     * @return
     * @throws Exception
     */
    public static ITable createRelQueryTable(ITable targetTable,
            ITable joinTable,
            String fromField,
            String toField) throws Exception
    {
        // Build a memory relationship class.
        IMemoryRelationshipClassFactory memRelClassFactory = new MemoryRelationshipClassFactory();
        // Create object class for the tables since table cannot be cast to
        // object class
        IObjectClass targetClass = new IObjectClassProxy(targetTable);
        IObjectClass joinClass = new IObjectClassProxy(joinTable);
        IRelationshipClass relationshipClass = memRelClassFactory.open("MemRelClass", targetClass, fromField, joinClass, toField, "forward", "backward", esriRelCardinality.esriRelCardinalityOneToOne);
        // Open the RelQueryTable as a feature class.
        IRelQueryTableFactory rqtFactory = new RelQueryTableFactory();
        ITable relQueryTable = (ITable) rqtFactory.open(relationshipClass, true, null, null, "", false, false);
        return relQueryTable;
    }

    /**
     * 
     * @param cursor
     * @throws AutomationException
     * @throws IOException
     */
    protected void printOut(ICursor cursor) throws AutomationException, IOException
    {
        // Get the number of fields in the feature class
        Fields fields = (Fields) cursor.getFields();
        int fieldCount = fields.getFieldCount();

        // Use the feature cursor to iterate over all elements in the feature
        // class,
        // printing the values of the fields. All field types are shown here.
        // Simple
        // values are printed as Strings. Complex elements are shown as the type
        // name.
        IRow feature = (IRow) cursor.nextRow();
        while (feature != null) {
            LOGGER.debug("");
            LOGGER.debug("#################################");

            for (int index = 0; index < fieldCount; index++) {
                String tmp = fields.getField(index).getName();
                int fieldType = feature.getFields().getField(index).getType();

                switch (fieldType) {
                case esriFieldType.esriFieldTypeDate:
                case esriFieldType.esriFieldTypeDouble:
                case esriFieldType.esriFieldTypeGlobalID:
                case esriFieldType.esriFieldTypeGUID:
                case esriFieldType.esriFieldTypeInteger:
                case esriFieldType.esriFieldTypeOID:
                case esriFieldType.esriFieldTypeSingle:
                case esriFieldType.esriFieldTypeSmallInteger:
                case esriFieldType.esriFieldTypeString:
                    tmp = tmp + " = " + feature.getValue(index);
                    break;

                case esriFieldType.esriFieldTypeBlob:
                    tmp = tmp + " = (blob)";
                    break;

                case esriFieldType.esriFieldTypeGeometry:
                    tmp = tmp + " = (geometry)";
                    break;

                case esriFieldType.esriFieldTypeRaster:
                    tmp = tmp + " = (raster)";
                    break;
                }
                LOGGER.debug(tmp);
            }
            feature = (IRow) cursor.nextRow();
        }
    }

    protected WorkspaceWrapper getWorkspace() {
    	return workspaceWrapper;
    }
    
    protected int getMaxNumberOfResults() {
    	return maxNumberOfResults;
    }

	
    /**
     * Support method.
     */
    public static String featureToString(Feature f) throws AutomationException, IOException
    {
        String featureAsString = "";
        IFields fields = f.getFields();
        for (int i = 0; i < fields.getFieldCount(); i++) {
            String fieldName = fields.getField(i).getName();
            String value = (String) f.getValue(i);
            featureAsString += "[" + fieldName + ":->" + value + "]";
        }
        return featureAsString;
    }

    @Override
	public boolean isResolveGeometriesFromStations() {
		return true;
	}
    
    
}
