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
package org.n52.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;

import org.n52.util.logging.Logger;


/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class CommonUtilities {
    
    public static final String NEW_LINE_CHAR = System.getProperty("line.separator");
	private static final String TAB_CHAR = "\t";
	private static final Logger logger = Logger.getLogger(CommonUtilities.class.getName());


	/**
	 * produces a String[] out of a String Collection. 
	 */
    public static String[] toArray(Collection<String> stringCollection) {
        String[] sArray = new String[stringCollection.size()];
        int i=0;
        for (Iterator<String> iterator = stringCollection.iterator(); iterator.hasNext();) {
            sArray[i] = (String) iterator.next();
            i++;
        }
        return sArray;
    }
    
	/**
     * produces a single String representation of a stringArray.
     */
    public static String arrayToString(String[] stringArray) {
        StringBuilder stringRep = new StringBuilder();
        stringRep.append("[");
        for (int i = 0; i < stringArray.length; i++) {
            stringRep.append(stringArray[i]);
            
            if (i < stringArray.length) {
                stringRep.append(",");
            }
        }
        stringRep.append("]");
        return stringRep.toString();
    }
    
    public static String readResource(InputStream res) {
		Scanner sc = new Scanner(res);
		StringBuilder sb = new StringBuilder();
		while (sc.hasNext()) {
			sb.append(sc.nextLine());
			sb.append(NEW_LINE_CHAR);
		}
		sc.close();
		return sb.toString();
	}

	public static String convertExceptionToString(Throwable e) {
		if (e == null) {
			return "null";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(e.getMessage());
		sb.append(":");
		sb.append(NEW_LINE_CHAR);

		int count = 0;
		for (StackTraceElement ste : e.getStackTrace()) {
			sb.append(TAB_CHAR);
			sb.append(ste.toString());
			if (++count < e.getStackTrace().length) 
				sb.append(NEW_LINE_CHAR);
		}
		
		return sb.toString();
	}
	
	public static void saveFile(File filename, String stringToStoreInFile) throws IOException {
        OutputStream out = new FileOutputStream(filename);
        out.write(stringToStoreInFile.getBytes());
        out.flush();
        out.close();
    }
	
	public static File resolveCacheBaseDir(String forDatabaseName) throws FileNotFoundException {
		String res = CommonUtilities.class.getResource("").toExternalForm();
		
		String prefix = "jar:file:/";
		if (res.startsWith(prefix)) {
			String soeJarFile = res.substring(prefix.length(), res.indexOf(".jar!"));
			String cachePath = soeJarFile.substring(0, soeJarFile.lastIndexOf("/"));
			
			File f = new File(cachePath);
			
			synchronized (CommonUtilities.class) {
				if (f.exists() && f.isDirectory()) {
					File parent = f.getParentFile().getParentFile();
					File sosSoeCache = new File(parent, "sos-soe-cache");
					if (sosSoeCache != null) {
						logger.debug("Cache base dir: "+ sosSoeCache.getAbsolutePath());
						if (!sosSoeCache.exists() && sosSoeCache.mkdir()) {
							return createDatabaseCacheFolder(sosSoeCache, forDatabaseName);
						}
						else {
							if (sosSoeCache.isDirectory()) {
								return createDatabaseCacheFolder(sosSoeCache, forDatabaseName);
							}
						}
					}
				}	
			}
			
		}
		
		throw new FileNotFoundException("Could not resolve the cache directory.");
	}

	private static File createDatabaseCacheFolder(File sosSoeCache,
			String forDatabaseName) throws FileNotFoundException {
		File target = new File(sosSoeCache, forDatabaseName);
		if (!target.exists()) {
			if (!target.mkdir()) {
				throw new FileNotFoundException("database subfolder could not be created! "+target);
			}
		}
		else if (!target.isDirectory()) {
			throw new FileNotFoundException("database subfolder path is not a directory! "+target);
		}
		return target;
	}
	
    
}