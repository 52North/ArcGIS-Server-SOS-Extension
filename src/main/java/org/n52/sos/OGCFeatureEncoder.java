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

package org.n52.sos;

import java.io.IOException;
import java.util.Collection;

import org.n52.om.sampling.AQDSample;
import org.n52.om.sampling.Feature;
import com.esri.arcgis.geometry.Point;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class OGCFeatureEncoder extends AbstractEncoder {

    /*
     * definition of anchor variables within template files:
     */
    private static String FEATURES = "@features@";
    private static String FEATURE_ID = "@feature-id@";
    private static String FEATURE_POINT = "@feature-point-location@";
    private static String FEATURE_NAME = "@feature-name@";
    private static String FEATURE_SAMPLED = "@feature-sampled-href@";
    private static String FEATURE_DESCRIPTION = "@feature-description@";
    private static String FEATURE_LOCAL_ID = "@feature-local-id@";
    private static String FEATURE_NAMESPACE = "@feature-namespace@";
    private static String FEATURE_INLET_HEIGHT = "@feature-inlet-height@";
    private static String FEATURE_BUILDING_DISTANCE = "@feature-building-distance@";
    private static String FEATURE_KERB_DISTANCE = "@feature-kerb-distance@";

    public String encodeFeatures(Collection<Feature> featureCollection) throws IOException {
                
        String responseTemplate = readText(OGCFeatureEncoder.class.getResourceAsStream("template_getfeatureofinterest_response.xml"));
        
        String featureTemplate = readText(OGCFeatureEncoder.class.getResourceAsStream("template_feature.xml"));
        
        String allFeatures = "";
        
        for (Feature feature : featureCollection) {
            
            String featureString = featureTemplate;
            
            featureString = featureString.replace(FEATURE_ID, feature.getIdentifier().getIdentifierValue());
            
            Point p = (Point)feature.getShape();
            featureString = featureString.replace(FEATURE_POINT, p.getX() + " " + p.getY());
            
            //featureString = featureString.replace(FEATURE_HREF, feature.getHref().toString()); // TODO replace with URL to feature
            
            if (feature.getSampledFeature() != null) {
                featureString = featureString.replace(FEATURE_SAMPLED, "<sam:sampledFeature xlink:href=\""+ feature.getSampledFeature() + "\"/>");
            } else {
                featureString = featureString.replace(FEATURE_SAMPLED, "<sam:sampledFeature xsi:nil=\"true\"/>");
            }
            
            if (feature.getName() != null) {
                featureString = featureString.replace(FEATURE_NAME, "<gml:name>" + feature.getName() + "</gml:name>");
            } else {
                featureString = featureString.replace(FEATURE_NAME, "");
            }
            
            if (feature.getDescription() != null) {
                featureString = featureString.replace(FEATURE_DESCRIPTION, "<gml:description>" + feature.getDescription() +"</gml:description>");
            } else {
                featureString = featureString.replace(FEATURE_DESCRIPTION, "");
            }
            
            if (feature instanceof AQDSample) {
                AQDSample aqdSample = (AQDSample) feature;
                featureString = featureString.replace(FEATURE_LOCAL_ID, aqdSample.getLocalId());
                featureString = featureString.replace(FEATURE_NAMESPACE, aqdSample.getNamespace());
                featureString = featureString.replace(FEATURE_INLET_HEIGHT, aqdSample.getInletHeight()+"");
                featureString = featureString.replace(FEATURE_BUILDING_DISTANCE, aqdSample.getBuildingDistance()+"");
                featureString = featureString.replace(FEATURE_KERB_DISTANCE, aqdSample.getKerbDistance()+"");
            }
            
            
            // add features to the allFeatures String
            allFeatures += featureString + "\n";
        }
        
        responseTemplate = responseTemplate.replace(FEATURES, allFeatures);
        
        return responseTemplate;
    }
}
