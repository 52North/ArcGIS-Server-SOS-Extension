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
package org.n52.om.sampling;

import java.net.URI;

import com.esri.arcgis.geometry.IGeometry;

/**
 * Class representing an AQD sampling feature.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AQDSample extends Feature {
    
    private String namespace;
    
    private double inletHeight;
    
    private double buildingDistance;
    
    private double kerbDistance;

    public AQDSample(
    		URI uri, 
            String gmlId, 
            int localId,
            String name, 
            String description, 
            String sampledFeatureURI, 
            IGeometry shape, 
            String namespace,
            double inletHeight,
            double buildingDistance,
            double kerbDistance) throws IllegalArgumentException {
        super(uri, gmlId, localId, namespace, description, sampledFeatureURI, shape);
        this.namespace = namespace;
        this.inletHeight = inletHeight;
        this.buildingDistance = buildingDistance;
        this.kerbDistance = kerbDistance;
    }

    // getters and setters

    public String getNamespace()
    {
        return namespace;
    }

    public double getInletHeight()
    {
        return inletHeight;
    }

    public double getBuildingDistance()
    {
        return buildingDistance;
    }

    public double getKerbDistance()
    {
        return kerbDistance;
    }

}
