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
package org.n52.sos.dataTypes;

import java.io.IOException;

import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.server.json.JSONObject;
import com.esri.arcgis.system.ServerUtilities;

public class AGSEnvelope implements EnvelopeWrapper {

	private Envelope envelope;

	public AGSEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}

	@Override
	public boolean isEmpty() throws IOException {
		return this.envelope.isEmpty();
	}

	@Override
	public JSONObject toJSON() throws IOException {
		return ServerUtilities.getJSONFromEnvelope(this.envelope);
	}

	@Override
	public Point getLowerLeft() throws IOException {
		return new Point(envelope.getLowerLeft().getX(), envelope.getLowerRight().getY());
	}

	@Override
	public Point getUpperRight() throws IOException {
		return new Point(envelope.getUpperRight().getX(), envelope.getUpperRight().getY());
	}

}
