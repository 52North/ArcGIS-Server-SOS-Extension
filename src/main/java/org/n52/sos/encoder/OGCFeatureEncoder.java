/*
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 * 
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.n52.sos.encoder;

import java.io.IOException;
import java.util.Collection;

import javax.activation.UnsupportedDataTypeException;

import org.n52.om.sampling.AQDSample;
import org.n52.om.sampling.Feature;
import org.n52.util.logging.Logger;

import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.Point;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class OGCFeatureEncoder extends AbstractEncoder {

    /*
     * definition of anchor variables within template files:
     */
    private static String FEATURES = "@features@";
    private static String FEATURE_GML_ID = "@feature-gml-id@";
    private static String FEATURE_POINT = "@feature-point-location@";
    private static String FEATURE_NAME = "@feature-name@";
    private static String FEATURE_SAMPLED = "@feature-sampled-href@";
    private static String FEATURE_DESCRIPTION = "@feature-description@";
    private static String FEATURE_GEOMETRY = "@feature-geometry@";
    private static String FEATURE_NAMESPACE = "@feature-namespace@";
    private static String FEATURE_INLET_HEIGHT = "@feature-inlet-height@";
    private static String FEATURE_BUILDING_DISTANCE = "@feature-building-distance@";
    private static String FEATURE_KERB_DISTANCE = "@feature-kerb-distance@";
	private static String responseTemplate;
	private static String featureTemplate;

    static {
    	try {
			responseTemplate = readText(OGCFeatureEncoder.class.getResourceAsStream("template_getfeatureofinterest_response.xml"));
			featureTemplate = readText(OGCFeatureEncoder.class.getResourceAsStream("template_feature.xml"));
		} catch (IOException e) {
			Logger.getLogger(OGCFeatureEncoder.class.getName()).warn(e.getMessage(), e);
		}
    	
    }
    
    public String encodeFeatures(Collection<Feature> featureCollection) throws IOException {
                
        StringBuilder allFeatures = new StringBuilder("");
        
        for (Feature feature : featureCollection) {
            
            StringBuilder featureString = new StringBuilder(featureTemplate);
            
            if (feature.getGmlId() != null) {
            	replace(featureString, FEATURE_GML_ID, "gml:id=\"" + feature.getGmlId() + "\"");
            }
            else {
            	replace(featureString, FEATURE_GML_ID, "");
            }
            
            if (feature.getShape() != null) {
            	IGeometry geometry = feature.getShape();
            	
	            int dimension  = geometry.getDimension();
	            int epsgCode   = geometry.getSpatialReference().getFactoryCode();
	            String epsgUrn = "urn:ogc:def:crs:EPSG::" + epsgCode;
	            
            	if (geometry instanceof Point) {
		            Point p = (Point)geometry;
		            replace(featureString, FEATURE_POINT, p.getX() + " " + p.getY());
		            
		            String featureGeometry =
		            "<sams:shape>"
					+	"<gml:Point gml:id=\"SamplingFeaturePoint_" + feature.getLocalId() + "\" srsDimension=\"" + dimension + "\" srsName=\"" + epsgUrn + "\">"
					+		"<gml:pos>" + p.getX() + " " + p.getY() + "</gml:pos>"
					+	"</gml:Point>"
					+"</sams:shape>";
		            
		            replace(featureString, FEATURE_GEOMETRY, featureGeometry);
            	}
            	else {
					throw new UnsupportedDataTypeException("Cannot encode geometry of feature.");
				}
            }
            else {
            	replace(featureString, FEATURE_GEOMETRY, "");
            }
            
            if (feature.getSampledFeature() != null) {
            	replace(featureString, FEATURE_SAMPLED, "<sam:sampledFeature xlink:href=\""+ feature.getSampledFeature() + "\" />");
            } else {
            	replace(featureString, FEATURE_SAMPLED, "<sam:sampledFeature nilReason=\"inapplicable\" />");
            }
            
            if (feature.getName() != null) {
            	replace(featureString, FEATURE_NAME, "<gml:name>" + feature.getName() + "</gml:name>");
            } else {
            	replace(featureString, FEATURE_NAME, "");
            }
            
            if (feature.getDescription() != null) {
            	replace(featureString, FEATURE_DESCRIPTION, "<gml:description>" + feature.getDescription() +"</gml:description>");
            } else {
            	replace(featureString, FEATURE_DESCRIPTION, "");
            }
            
            if (feature instanceof AQDSample) {
                AQDSample aqdSample = (AQDSample) feature;
                replace(featureString, FEATURE_INLET_HEIGHT, aqdSample.getInletHeight()+"");
                replace(featureString, FEATURE_BUILDING_DISTANCE, aqdSample.getBuildingDistance()+"");
                replace(featureString, FEATURE_KERB_DISTANCE, aqdSample.getKerbDistance()+"");
            }
            
            // add features to the allFeatures String
            allFeatures.append(featureString);
        }
        
        String response = responseTemplate.replace(FEATURES, allFeatures);
        
        return response;
    }
    

    public static StringBuilder replace(StringBuilder builder,
            String replaceWhat,
            String replaceWith)
    {
        int indexOfTarget = -1;
        while ((indexOfTarget = builder.indexOf(replaceWhat)) > 0) {
            builder.replace(indexOfTarget, indexOfTarget + replaceWhat.length(), replaceWith);
        }
        return builder;
    }
}
