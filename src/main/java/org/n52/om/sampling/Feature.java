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

package org.n52.om.sampling;

import java.io.IOException;
import java.net.URI;

import org.n52.gml.Identifier;

import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.interop.AutomationException;

/**
 * Class representing a spatial sampling feature primarily used for features of
 * interest; feature might be either be provided as reference (href member
 * variable is set) or provided with complete content (href isn't set, but rest
 * of attributes are set (boundedBy is optional)
 * 
 * @author Kiesow, staschc
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class Feature {

    /** reference to sampling feature */
    private URI href;

    /** identifier of feature (optional) */
    private Identifier identifer;

    /** description of feature (optional) */
    private String description;

    /** name of feature (optional) */
    private String name;

    /** reference to the sampled feature (might be a lake for example) */
    private String sampledFeature;

    /** geometry of the sampling feature */
    private IGeometry shape;

    public Feature(Identifier identifier, String name, String description, String sampledFeatureURI, IGeometry shape) throws IllegalArgumentException {
        this.identifer = identifier;
        this.name = name;
        this.description = description;
        this.sampledFeature = sampledFeatureURI;
        this.shape = shape;
    }

    /**
     * constructor for sampling features which are referenced
     * 
     * @param href
     *            reference to the SamplingFeature
     */
    public Feature(URI href) {
        this.href = href;
    }

    // getters and setters
    /**
     * 
     * @return Returns identifier of the feature
     */
    public Identifier getIdentifier()
    {
        return identifer;
    }

    public String getDescription()
    {
        return description;
    }

    public String getName()
    {
        return name;
    }

    public IEnvelope getBoundedBy() throws AutomationException, IOException 
    {
        return shape.getEnvelope();
    }

    public String getSampledFeature()
    {
        return sampledFeature;
    }

    public IGeometry getShape()
    {
        return shape;
    }

    public URI getHref()
    {
        return href;
    }

    /**
     * @return the featureType
     * @throws IOException
     * @throws AutomationException
     */
    public String getFeatureType() throws AutomationException, IOException
    {
        if (shape.getGeometryType() == 1) {
            return "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint";

        } else if (shape.getGeometryType() == 6 || shape.getGeometryType() == 9 || shape.getGeometryType() == 13) {
            return "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingCurve";

        } else if (shape.getGeometryType() == 4) {
            return "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingSurface";
        }
        throw new RuntimeException("feature type " + shape.getGeometryType() + "needs to be supported.");
    }

    /**
     * 
     * @throws IOException
     * @throws AutomationException
     * @returns typename (e.g. SamplingPoint)
     */
    public String getTypeName() throws AutomationException, IOException
    {
        if (shape.getGeometryType() == 1) {
            return "SF_SamplingPoint";
        } else if (shape.getGeometryType() == 6 || shape.getGeometryType() == 9 || shape.getGeometryType() == 13) {
            return "SF_SamplingCurve";
        } else if (shape.getGeometryType() == 4) {
            return "SF_SamplingSurface";
        }
        throw new RuntimeException("feature type " + shape.getGeometryType() + "needs to be supported.");
    }
}
