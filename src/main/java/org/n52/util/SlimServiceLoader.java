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
package org.n52.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Simplified variant of the famous java.util.ServiceLoader.
 * Services description files are read by default from
 * {@value #defaultLocationBase}. 
 * 
 * @author matthes rieke
 *
 * @param <S> the interface for which implementations are loaded.
 */
public final class SlimServiceLoader<S> {

	public static String defaultLocationBase = "META-INF/services/";

	private ClassLoader classLoaderInstance;
	private Class<S> theInterface;

	private String baseLocation;

	private Collection<S> cached;

	public static void setDefaultLocationBase(String defaultLocationBase) {
		SlimServiceLoader.defaultLocationBase = defaultLocationBase;
	}

	private SlimServiceLoader(Class<S> ntrfce, String location,
			ClassLoader loader) {
		theInterface = ntrfce;
		classLoaderInstance = loader;
		baseLocation = location;
	}

	/**
	 * @return the loaded interface implementations
	 */
	public Collection<S> implementations() {
		if (cached == null) {
			cached = loadImplementations();
		}
		return cached;
	}

	private Collection<S> loadImplementations() {
		Collection<S> result = new ArrayList<S>();

		URL baseUrl = classLoaderInstance.getResource(baseLocation+theInterface.getCanonicalName());

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(baseUrl.openStream(),
					"UTF-8"));
			while (br.ready()) {
				parseLine(br.readLine(), result);
			}
		} catch (IOException x) {
			throw new RuntimeException(x);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException y) {
				throw new RuntimeException("Error closing file stream:"
						+ baseUrl);
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private void parseLine(String line, Collection<S> result) {
		if (line == null)
			return;
		
		String tmp = line.trim();
		
		if (tmp.startsWith("#"))
			return;
		
		try {
			Class<?> clazz = Class.forName(tmp);
			if (theInterface.isAssignableFrom(clazz)) {
				result.add((S) clazz.newInstance());
			}
		} catch (ClassNotFoundException e) {
			return;
		} catch (InstantiationException e) {
			return;
		} catch (IllegalAccessException e) {
			return;
		}
		
	}

	/**
	 * Services description files are read by default from
	 * {@value #defaultLocationBase}. 
	 * 
	 * @param service the interface
	 * @return the object providing access to the implementations
	 */
	public static <S> SlimServiceLoader<S> load(Class<S> service) {
		return load(service, defaultLocationBase);
	}

	/**
	 * Services description files are read from
	 * baseLocation parameter. 
	 * 
	 * @param service the interface
	 * @param baseLocation the directory for service description files
	 * @return the object providing access to the implementations
	 */
	public static <S> SlimServiceLoader<S> load(Class<S> service,
			String baseLocation) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return load(service, baseLocation, cl);
	}

	/**
	 * Services description files are read from
	 * baseLocation parameter by the defined loader.
	 * 
	 * @param service the interface
	 * @param baseLocation the directory for service description files
	 * @param loader ClassLoader to use
	 * @return the object providing access to the implementations
	 */
	public static <S> SlimServiceLoader<S> load(Class<S> service,
			String baseLocation, ClassLoader loader) {
		return new SlimServiceLoader<S>(service, baseLocation, loader);
	}
	
}
