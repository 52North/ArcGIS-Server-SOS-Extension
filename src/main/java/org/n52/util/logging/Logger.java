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
package org.n52.util.logging;

import java.io.IOException;

import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ILog2;

/**
 * Logging mechanism that logs to ArcGIS Server's built-in
 * logging interface. This class' methods emulate commonly
 * used logging interfaces.
 * 
 * @author matthes rieke
 *
 */
public class Logger {

	private static final int SEVERE = 1;
	private static final int WARNING = 2;
	private static final int INFO = 3;
	private static final int FINE = 4;
	private static final int VERBOSE = 5;
	private static final int DEBUG = 100;
	
	private static final int DEFAULT_CODE = 519348;
	
	private static ILog2 serverLogger;
	private String name;

	public Logger(String name) {
		this.name = name;
	}

	public static void init(ILog2 serverLog) {
		serverLogger = serverLog;
	}

	public static Logger getLogger(String name) {
		return new Logger(name);
	}
	
	protected final void log(int level, String message, Exception e) {
		String concatMessage;
		if (e != null) {
			concatMessage = createConcatenatedMessage(message, e);
		}
		else {
			concatMessage = message;
		}
		log(level, DEFAULT_CODE, concatMessage);
	}
	
	private String createConcatenatedMessage(String message, Exception e) {
		StringBuilder sb = new StringBuilder(message);
		String sep = System.getProperty("line.separator");
		sb.append(":");
		sb.append(sep);
		
		for (StackTraceElement ste : e.getStackTrace()) {
			sb.append("\t");
			sb.append(ste.toString());
			sb.append(sep);
		}
		
		return sb.toString();
	}

	protected final void log(int level, int code, String message) {
		if (serverLogger == null) return;
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			sb.append(this.name);
			sb.append("] ");
			sb.append(message);
			serverLogger.addMessage(level, code, sb.toString());
		} catch (AutomationException e) {
		} catch (IOException e) {
		}
	}

	public void debug(String message) {
		log(DEBUG, message, null);
	}
	
	public void verbose(String message) {
		log(VERBOSE, message, null);
	}
	
	public void fine(String message) {
		log(FINE, message, null);
	}
	
	public void info(String message) {
		log(INFO, message, null);
	}

	public void warn(String message) {
		log(WARNING, message, null);
	}
	
	public void warn(String message, Exception e) {
		log(SEVERE, message, e);
	}

	public void severe(String message) {
		log(SEVERE, message, null);
	}

	public void severe(String message, Exception e) {
		log(SEVERE, message, e);
	}

}
