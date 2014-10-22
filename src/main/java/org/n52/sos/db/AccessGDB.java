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
package org.n52.sos.db;

import java.io.IOException;

import org.n52.sos.dataTypes.ServiceDescription;


public interface AccessGDB {

	AccessGdbForProcedures getProcedureAccess();

	AccessGdbForFeatures getFeatureAccess();

	AccessGdbForObservations getObservationAccess();

	AccessGdbForOfferings getOfferingAccess();

	AccessGdbForAnalysis getAnalysisAccess();

	InsertGdbForObservations getObservationInsert();

	ServiceDescription getServiceDescription() throws IOException;


	/**
	 * @return true, if geometries of features should be resolved via
	 * associated stations if they are missing
	 */
	boolean isResolveGeometriesFromStations();

	/**
	 * @return the name of the underlying database
	 */
	String getDatabaseName();

}
