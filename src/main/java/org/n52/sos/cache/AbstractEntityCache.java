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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.n52.sos.db.AccessGDB;
import org.n52.util.CommonUtilities;
import org.n52.util.logging.Logger;

public abstract class AbstractEntityCache<T> {
	
	public Logger LOGGER = Logger.getLogger(AbstractEntityCache.class.getName());
	
	private File cacheFile;
	private FileOutputStream fileStream;
	private Object cacheFileMutex = new Object();
	
	
	
	public AbstractEntityCache() throws FileNotFoundException {
		initializeCacheFile();
	}

	protected void initializeCacheFile() throws FileNotFoundException {
		File baseDir = CommonUtilities.resolveCacheBaseDir();
		
		synchronized (cacheFileMutex) {
			this.cacheFile = new File(baseDir, getCacheFileName());
			
			if (this.cacheFile == null) {
				throw new FileNotFoundException("cache file "+ getCacheFileName() +" not found.");
			}
			
			if (!this.cacheFile.exists()) {
				try {
					this.cacheFile.createNewFile();
				} catch (IOException e) {
					throw new FileNotFoundException("Could not create cache file "+ getCacheFileName());
				}
			}	
		}
	}

	protected abstract String getCacheFileName();

	protected abstract String serializeEntity(T entity) throws CacheException;

	protected abstract T deserializeEntity(String line);
	
	protected abstract Collection<T> getCollectionFromDAO(AccessGDB geoDB) throws IOException;
	
	protected abstract AbstractEntityCache<T> getSingleInstance();
	
	public synchronized void storeEntity(String id, T entity, FileOutputStream fos) throws CacheException {
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		sb.append("=");
		sb.append(serializeEntity(entity));
		sb.append(System.getProperty("line.separator"));
		
		try {
			fos.write(sb.toString().getBytes());
			fos.flush();
			
		} catch (IOException e) {
			throw new CacheException(e);
		}
		
	}
	
	protected Map<String, T> deserializeEntityCollection(
			InputStream fis) {
		Map<String, T> result = new HashMap<>();
		Scanner sc = new Scanner(fis);
		
		String line;
		while (sc.hasNext()) {
			line = sc.nextLine();
			String id = line.substring(0, line.indexOf("="));
			result.put(id, deserializeEntity(line.substring(line.indexOf("=")+1, line.length())));
		}
		
		sc.close();
		
		return result;
	}
	
	public synchronized void storeEntityCollection(Collection<T> entities) throws CacheException {
		int c = 0;
		
		Map<String, T> result = new HashMap<>();
		
		for (T t : entities) {
			result.put(Integer.toString(c++), t);
		}
		
		storeEntityCollection(result);
	}
	
	public synchronized void storeEntityCollection(Map<String, T> entities) throws CacheException {
		File tempCacheFile;
		try {
			tempCacheFile = getTempCacheFile();
			this.fileStream = new FileOutputStream(tempCacheFile);
		} catch (FileNotFoundException e) {
			throw new CacheException(e);
		}
		
		
		LOGGER.info("storing cache to temporary file "+ tempCacheFile.getAbsolutePath());
		for (String id : entities.keySet()) {
			storeEntity(id, entities.get(id), this.fileStream);
		}
		
		try {
			this.fileStream.close();
		} catch (IOException e) {
			throw new CacheException(e);
		}
		this.fileStream = null;
		
		synchronized (cacheFileMutex) {
			try {
				LOGGER.info("replacing target cache file "+ cacheFile.getAbsolutePath());
				Files.copy(tempCacheFile.toPath(), this.cacheFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				tempCacheFile.delete();
			} catch (IOException e) {
				throw new CacheException(e);
			}
		}
	}
	
	private File getTempCacheFile() {
		return new File(this.cacheFile.getParent(), getCacheFileName()+".tmp");
	}

	public T getEntity(String id) throws CacheException {
		return getEntityCollection().get(id);
	}
	
	public Map<String, T> getEntityCollection() throws CacheException {
		return getEntityCollection(null);
	}
	
	public Map<String, T> getEntityCollection(AccessGDB geoDB) throws CacheException {
		synchronized (cacheFileMutex) {
			if (this.cacheFile == null || !(this.isCacheAvailable() && this.hasCacheContent())) {
				if (geoDB != null) {
					try {
						initializeCacheFile();
						updateCache(geoDB);
					} catch (IOException e) {
						throw new CacheException(e);
					}
				}
				else {
					LOGGER.warn("Could not access or create the cache file. AccessGDB not available! "+getCacheFileName());
					return Collections.emptyMap();
				}
			}
			
			try {
				LOGGER.info("Returning data from cache file...");
				return deserializeCacheFile();
			} catch (IOException e) {
				throw new CacheException(e);
			}	
		}
	}

	private Map<String, T> deserializeCacheFile() throws IOException {
		FileInputStream fis = new FileInputStream(this.cacheFile);
		
		return deserializeEntityCollection(fis);
	}
	
	protected String readStreamContent(InputStream is) {
		Scanner sc = new Scanner(is);
		StringBuilder sb = new StringBuilder();
		
		while (sc.hasNext()) {
			sb.append(sc.nextLine());
			sb.append(System.getProperty("line.separator"));
		}
		
		sc.close();
		
		return sb.toString();
	}
	
	protected String[] decodeStringArray(String string) {
		String trimmed = string.substring(1, string.length() - 1);
		
		String[] splitted = trimmed.split(", ");
		
		return splitted;
	}
	
	public boolean isCacheAvailable() {
		synchronized (cacheFileMutex) {
			return cacheFile.exists();
		}
	}
	
	public boolean hasCacheContent() {
		synchronized (cacheFileMutex) {
			return cacheFile.exists() && cacheFile.length() > 0;
		}
	}
	
	public long lastUpdated() {
		if (isCacheAvailable() && hasCacheContent()) {
			return cacheFile.lastModified();
		}
		return 0;
	}
	
	public boolean requestUpdateLock() {
		File f = getCacheLockFile();
		
		if (f != null && f.exists()) {
			return false;
		}
		
		try {
			f.createNewFile();
			return true;
		} catch (IOException e) {
			LOGGER.warn(e.getMessage(), e);
			LOGGER.warn("Could not access cache lock file.");
		}
		
		return false;
	}
	
	public void freeUpdateLock() {
		File f = getCacheLockFile();
		
		if (f != null && f.exists()) {
			f.delete();
		}
	}
	
	public boolean isUpdateOngoing() {
		File f = getCacheLockFile();
		
		if (f.exists()) {
			return true;
		}
		
		return false;
	}

	private File getCacheLockFile() {
		return new File(this.cacheFile.getParent(), this.cacheFile.getName()+".lock");
	}
	
	public void updateCache(AccessGDB geoDB) throws CacheException, IOException {
		AbstractEntityCache<T> instance = getSingleInstance();
		if (!instance.requestUpdateLock()) {
			LOGGER.info("cache is currently already updating");
			return;
		}
		
		Collection<T> entities = getCollectionFromDAO(geoDB);
		instance.storeEntityCollection(entities);
		
		instance.freeUpdateLock();		
	}

	public boolean requiresUpdate() {
		if (this.isCacheAvailable()) {
			if (this.hasCacheContent()) {
				long lastUpdated = this.getSingleInstance().lastUpdated();
				
				if (System.currentTimeMillis() - lastUpdated > CacheScheduler.MINIMUM_UPDATE_DELTA) {
					return true;
				}	
			}
			else {
				return true;
			}
		}
		
		return false;
	}

}
