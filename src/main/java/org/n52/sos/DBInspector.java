/*
 * Copyright (C) 2011
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 * 
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 * 
 * This program is free software; you can redistribute and/or modify it under 
 * the terms of the GNU General Public License version 2 as published by the 
 * Free Software Foundation.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 * 
 * Author: Arne Broering
 */

package org.n52.sos;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.n52.sos.db.AccessGdbForAnalysis;
import org.n52.util.ExceptionSupporter;

import com.esri.arcgis.carto.IMapServerDataAccess;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.interop.extn.ArcGISExtension;
import com.esri.arcgis.interop.extn.ServerObjectExtProperties;
import com.esri.arcgis.server.IServerObjectExtension;
import com.esri.arcgis.server.IServerObjectHelper;
import com.esri.arcgis.server.json.JSONArray;
import com.esri.arcgis.server.json.JSONObject;
import com.esri.arcgis.system.IObjectConstruct;
import com.esri.arcgis.system.IPropertySet;
import com.esri.arcgis.system.IRESTRequestHandler;
import com.esri.arcgis.system.ServerUtilities;

/**
 * The main class of this ArcGIS Server Object Extension (SOE).
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
@ArcGISExtension
@ServerObjectExtProperties(
        displayName = "Database_analyzer_for_the_SOS_extension", description = "Database_analyzer_for_the_SOS_extension")
public class DBInspector implements IServerObjectExtension, IObjectConstruct, IRESTRequestHandler {

    private static final long serialVersionUID = 1L;

    public Logger LOGGER = Logger.getLogger(DBInspector.class.getName());

    private String tableName;

    private String tablePkField;

    protected AccessGdbForAnalysis geoDB;

    private IMapServerDataAccess mapServerDataAccess;

    /**
     * constructs a new server object extension
     * 
     * @throws Exception
     */
    public DBInspector() throws Exception {
        super();
    }

    /*************************************************************************************
     * IServerObjectExtension methods:
     *************************************************************************************/

    /**
     * init() is called once, when the instance of the SOE is created.
     */
    public void init(IServerObjectHelper soh) throws IOException, AutomationException
    {
        LOGGER.info("Start initializing " + this.getClass().getName() + " SOE.");

        this.mapServerDataAccess = (IMapServerDataAccess) soh.getServerObject();

        LOGGER.info(this.getClass().getName() + " initialized.");
    }

    /**
     * shutdown() is called once when the Server Object's context is being shut
     * down and is about to go away.
     */
    public void shutdown() throws IOException, AutomationException
    {
        /*
         * The SOE should release its reference on the Server Object Helper.
         */
        LOGGER.info("Shutting down " + this.getClass().getName() + " SOE.");

        this.mapServerDataAccess = null;
        this.geoDB = null;

        // TODO: make sure all references are being cut.
    }

    /*************************************************************************************
     * IConstructObject methods:
     *************************************************************************************/
    /**
     * construct() is called only once, when the SOE is created, after
     * IServerObjectExtension.init() is called. This method hands back the
     * configuration properties for the SOE as a property set. You should
     * include any expensive initialization logic for your SOE within your
     * implementation of construct().
     */
    public void construct(IPropertySet propertySet) throws IOException
    {

        LOGGER.info("construct() is called...");

        // TODO --> read in maxNumOfResults from Manager

        try {
            LOGGER.info("Reading properties...");

            this.tableName = (String) propertySet.getProperty("tableToAnalyze");
            this.tablePkField = (String) propertySet.getProperty("tablePkField");

        } catch (Exception e) {
            LOGGER.severe("There was a problem while reading properties: \n" + e.getLocalizedMessage() + "\n" + ExceptionSupporter.createStringFromStackTrace(e));
            throw new IOException(e);
        }

        try {
            // create database access
            this.geoDB = new AccessGdbForAnalysis(this);
        } catch (Exception e) {
            LOGGER.severe("There was a problem while creating DB access: \n" + e.getLocalizedMessage() + "\n" + ExceptionSupporter.createStringFromStackTrace(e));
            throw new IOException(e);
        }
    }

    /*************************************************************************************
     * IRESTRequestHandler methods:
     *************************************************************************************/

    /**
     * This method returns the resource hierarchy of a REST based SOE in JSON
     * format.
     */
    @Override
    public String getSchema() throws IOException, AutomationException
    {
        LOGGER.info("getSchema() is called...");

        JSONObject arcGisSos = ServerUtilities.createResource("DB_Analyzer_for_ArcGIS_SOS_Extension", "A_DBAnalyzer_for_the_SOS_extension_for_ArcGIS_Server", false, false);

        JSONArray operationArray = new JSONArray();
        
        JSONObject tableNamesObject = ServerUtilities.createResource("ReadTableNamesFromDB", "reads all table names from DB", false, false);
        JSONObject analyzeProcedureTableObject = ServerUtilities.createResource("AnalyzeTableUsingProperties", "analyzes a specified table", false, false);
        
//        operationArray.put(ServerUtilities.createOperation("ReadTableNamesFromDB", "db", "json", false));
        operationArray.put(ServerUtilities.createOperation("AnalyzeTable", "tableName", "json", false));
//        operationArray.put(ServerUtilities.createOperation("AnalyzeProcedureTable", "bla", "json", false));

        JSONArray resourceArray = new JSONArray();
        resourceArray.put(tableNamesObject);
        resourceArray.put(analyzeProcedureTableObject);

        arcGisSos.put("resources", resourceArray);
//        arcGisSos.put("operations", operationArray);

        return arcGisSos.toString();
    }

    /**
     * This method handles REST requests by determining whether an operation or
     * resource has been invoked and then forwards the request to appropriate
     * methods.
     * 
     * @param capabilities
     *            The capabilities supported by the SOE. An admin can choose
     *            which capabilities are enabled on a particular SOE (in ArcGIS
     *            Manager or ArcCatalog), based on certain criteria such as
     *            security roles. This list of allowed capabilities is then sent
     *            to this method, at runtime, as a comma separated list.
     * @param resourceName
     *            Name of the resource being addressed relative to the root SOE
     *            resource. If empty, its assumed that root resource is being
     *            addressed. E.g.: "procedures/mysensor123". For resource
     *            requests, the operationName parameter of this method will be
     *            an empty string ("").
     * @param operationName
     *            Name of the operation being invoked. If empty, description of
     *            resource is returned.
     * @param operationInput
     *            Input parameters to the operation specified by operationName
     *            parameter, encoded as a JSON formatted string. The REST
     *            handler coerces the input parameters of an operation into a
     *            JSON string. The request parameter names become the JSON
     *            property names. The values are attempted to be coerced to
     *            valid JSON types - numbers, booleans, JSON objects, JSON
     *            arrays. If they cannot be coerced to any of the types
     *            mentioned above, they'll be treated as strings.
     * @param outputFormat
     *            OutputFormat of operation. The value of the format parameter f
     *            of the REST API.
     */
    @Override
    public byte[] handleRESTRequest(String capabilities,
            String resourceName,
            String operationName,
            String operationInput,
            String outputFormat,
            String requestProperties,
            String[] responseProperties) throws IOException, AutomationException
    {
        LOGGER.info("Starting to handle REST request...");

        try {
            // resource
            if (operationName.length() == 0) {
                
                if (resourceName == null || resourceName.matches("")) {
                    JSONObject json = new JSONObject();
                    json.append("Welcome to the DB Analyzer.", "Please use the links below to analyze the DB underlying this SOE.");
                    return json.toString().getBytes("utf-8");
                }
                else if (resourceName.matches("ReadTableNamesFromDB")) {
                    return geoDB.readTableNamesFromDB().toString().getBytes("utf-8");
                }
                else if (resourceName.matches("AnalyzeTableUsingProperties")) {
                    return geoDB.analyzeProcedureTable().toString().getBytes("utf-8");
                }
                else {
                    throw new Exception("Resource '" + resourceName + "' not supported.");
                }
                
                
            } else {
                // extract operation input parameters to Map:
                JSONObject inputObject = new JSONObject(operationInput);

                
                
//                if (operationName.equalsIgnoreCase("ReadTableNamesFromDB")) {
//                    return geoDB.readTableNamesFromDB().toString().getBytes("utf-8");
//                } 
                if (operationName.equalsIgnoreCase("AnalyzeTable")) {
                    return geoDB.analyzeTable(inputObject).toString().getBytes("utf-8");
                } 
//                else if (operationName.equalsIgnoreCase("AnalyzeProcedureTable")) {
//                    return geoDB.analyzeProcedureTable().toString().getBytes("utf-8");
//                } 
                else {
                    throw new Exception("Operation '" + operationName + "' on resource '" + resourceName + "' not supported.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while handle REST request", e);

            // send out error:
            return ServerUtilities.sendError(3, "An exception occurred: " + e.toString(), ExceptionSupporter.createStringArrayFromStackTrace(e.getStackTrace())).getBytes("utf-8");
        }
    }

    public IMapServerDataAccess getMapServerDataAccess()
    {
        return this.mapServerDataAccess;
    }

    public String getTable()
    {
        return tableName;
    }

    public String getTablePkField()
    {
        return tablePkField;
    }

}
