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

package org.n52.sos.dataTypes;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class ObservedProperty {

    /**
     * the description (or reference to a description) of an observed property.
     */
    private String description;
    
    /**
     * the type in which observation values for this property are measured. 
     */
    private String dataType;

    /**
     * the unit of measure used to reference values of this observed property.
     */
    private String uom;
    
    /**
     * @param description
     * @param dataType
     * @param uom
     */
    public ObservedProperty(String description, String dataType, String uom) {
        super();
        this.description = description;
        this.dataType = dataType;
        this.uom = uom;
    }

    public String getDescription()
    {
        return description;
    }

    public String getDataType()
    {
        return dataType;
    }

    public String getUnitOfMeasurement() {
            return uom;
    }

}
