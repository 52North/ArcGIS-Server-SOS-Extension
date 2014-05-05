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
package org.n52.sos.cache;

import java.io.IOException;

import org.n52.sos.dataTypes.EnvelopeWrapper;
import org.n52.sos.dataTypes.Point;

import com.esri.arcgis.server.json.JSONObject;

public class CachedEnvelop implements EnvelopeWrapper {

	private Point lowerLeft;
	private Point upperRight;

	public CachedEnvelop(String[] split) {
		this.lowerLeft = new Point(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
		this.upperRight = new Point(Double.parseDouble(split[2]), Double.parseDouble(split[3]));
	}

	@Override
	public boolean isEmpty() throws IOException {
		return this.lowerLeft.equals(this.upperRight);
	}

	@Override
	public JSONObject toJSON() throws IOException {
		//TODO implement
		return new JSONObject();
	}

	@Override
	public Point getLowerLeft() throws IOException {
		return this.lowerLeft;
	}

	@Override
	public Point getUpperRight() throws IOException {
		return this.upperRight;
	}

}
