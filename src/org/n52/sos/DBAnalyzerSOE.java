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
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.n52.om.observation.collections.GenericObservationCollection;
import org.n52.om.sampling.SpatialSamplingFeature;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimeFactory;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.dataTypes.ServiceDescription;
import org.n52.sos.db.AccessGDB;
import org.n52.sos.db.AccessObservationGDB;
import org.n52.util.ExceptionSupporter;
import org.n52.util.logging.Log;

import com.esri.arcgis.carto.IMapServerDataAccess;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.interop.extn.ArcGISExtension;
import com.esri.arcgis.interop.extn.ServerObjectExtProperties;
import com.esri.arcgis.server.IServerObjectExtension;
import com.esri.arcgis.server.IServerObjectHelper;
import com.esri.arcgis.server.SOAPRequestHandler;
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
        displayName = "DBAnalyzer_for_the_SOS_extension",
        description = "DBAnalyzer_for_the_SOS_extension"
        )
public class DBAnalyzerSOE extends SOSExt {

    private static final long serialVersionUID = 1L;

    public Logger LOGGER = Logger.getLogger(DBAnalyzerSOE.class.getName());
    
    /**
     * constructs a new server object extension
     * 
     * @throws Exception
     */
    public DBAnalyzerSOE() throws Exception {
        super();
    }

    /*************************************************************************************
     * IConstructObject methods:
     *************************************************************************************/
    /**
     * construct() is called only once, when the SOE is created, after IServerObjectExtension.init() 
     * is called. This method hands back the configuration properties for the SOE as a property set.
     * You should include any expensive initialization logic for your SOE within your implementation 
     * of construct().  
     */
    public void construct(IPropertySet propertySet) throws IOException {
        
        LOGGER.info("construct() is called...");
        
        // create database access
        this.geoDB = new AccessGDB(this);
        
        LOGGER.info("construct() is finished.");
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
            return geoDB.getanalyzeDB().toString().getBytes("utf-8");
                
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while handle REST request", e);
            
            // send out error:
            return ServerUtilities.sendError(3, "An exception occurred: " + e.toString(), ExceptionSupporter.createStringArrayFromStackTrace(e.getStackTrace())).getBytes("utf-8");
        }
    }
}


