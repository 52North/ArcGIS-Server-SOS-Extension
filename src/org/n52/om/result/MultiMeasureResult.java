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

package org.n52.om.result;

import java.util.ArrayList;
import java.util.List;

import org.n52.oxf.valueDomains.time.ITimePosition;

/**
 * Result representing a result consisting of multiple {@link MeasureResult}s.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 * 
 */
public class MultiMeasureResult implements IResult {

	private List<MeasureResult> resultValues;
	
    public MultiMeasureResult() {
        resultValues = new ArrayList<MeasureResult>();
    }

    /**
     * @return the start (earliest) time among all dateTimeBegins of the observations.
     */
    @Override
    public ITimePosition getDateTimeBegin()
    {
    	ITimePosition timestart = null;
    	
        for (IResult resVal : resultValues) {
            ITimePosition time = (ITimePosition)resVal.getDateTimeBegin();
            
            if (timestart == null) {
            	timestart = time;
            }
            else {
                if (timestart.after(time)) {
                	timestart = time;
                }
            }
        }
        
        return timestart;
    }
    
    /**
     * @return the end (latest) time among all dateTimeEnds of the observations.
     */
    @Override
    public ITimePosition getDateTimeEnd()
    {
    	ITimePosition timeEnd = null;
    	
        for (IResult resVal : resultValues) {
            ITimePosition time = (ITimePosition)resVal.getDateTimeEnd();
            
            if (timeEnd == null) {
            	timeEnd = time;
            }
            else {
                if (timeEnd.after(time)) {
                	timeEnd = time;
                }
            }
        }
        
        return timeEnd;
    }
	
    @Override
    public List<MeasureResult> getValue()
    {
        return resultValues;
    }
    

	public void addResultValue(MeasureResult result)
	{
		resultValues.add(result);
	}
}
