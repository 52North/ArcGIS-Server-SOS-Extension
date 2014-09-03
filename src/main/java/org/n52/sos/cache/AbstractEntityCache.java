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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import org.n52.sos.db.AccessGDB;
import org.n52.util.CommonUtilities;
import org.n52.util.logging.Logger;

public abstract class AbstractEntityCache<T> {
	
	public Logger LOGGER = Logger.getLogger(AbstractEntityCache.class.getName());
	
	private File cacheFile;
	private Object cacheFileMutex = new Object();

	private long lastUpdateDuration;
	
	
	
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
	
	public void storeTemporaryEntity(T et) {
		FileOutputStream fs = null;
		synchronized (cacheFileMutex) {
			try {
				fs = new FileOutputStream(getTempCacheFile(), true);
				storeEntity(UUID.randomUUID().toString(), et, fs);	
			} catch (FileNotFoundException | CacheException e) {
				LOGGER.warn(e.getMessage(), e);
			} finally {
				try {
					if (fs != null) {
						fs.close();
					}
				} catch (IOException e) {
					LOGGER.warn(e.getMessage(), e);
				}
			}
		}
	}
	
	protected void clearTempCacheFile() throws IOException {
		synchronized (cacheFileMutex) {
			File f = getTempCacheFile();
			f.delete();
			f.createNewFile();
		}
	}
	
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
		File tempCacheFile = getTempCacheFile();
		if (entities.size() > 0) {
		
			FileOutputStream fileStream;
			try {
				fileStream = new FileOutputStream(tempCacheFile);
			} catch (FileNotFoundException e) {
				throw new CacheException(e);
			}
			
			
			LOGGER.info("storing cache to temporary file "+ tempCacheFile.getAbsolutePath());
			for (String id : entities.keySet()) {
				storeEntity(id, entities.get(id), fileStream);
			}
			
			try {
				fileStream.close();
			} catch (IOException e) {
				throw new CacheException(e);
			}
			fileStream = null;			
		}
		
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
		synchronized (cacheFileMutex) {
			return new File(this.cacheFile.getParent(), getCacheFileName()+".tmp");
		}
	}

	public Map<String, T> getEntityCollection(AccessGDB geoDB) throws CacheException, CacheNotYetAvailableException {
		LOGGER.info("getEntityCollection for cache "+getClass().getSimpleName());
		synchronized (cacheFileMutex) {
			if (this.cacheFile == null || !(this.isCacheAvailable() && this.hasCacheContent())) {
				if (geoDB != null) {
					try {
						initializeCacheFile();
						scheduleCacheUpdate();
					} catch (IOException e) {
						throw new CacheException(e);
					}
				}
				else {
					throw new CacheException("Could not access or create the cache file. AccessGDB not available! "+getCacheFileName());
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

	private void scheduleCacheUpdate() {
		AbstractCacheScheduler.Instance.instance().forceUpdate();
	}

	private Map<String, T> deserializeCacheFile() throws IOException, CacheNotYetAvailableException {
		synchronized (cacheFileMutex) {
			FileInputStream fis;
			if (hasCacheContent()) {
				fis = new FileInputStream(this.cacheFile);
			}
			else if (hasCacheContent(getTempCacheFile())) {
				fis = new FileInputStream(getTempCacheFile());
			}
			else {
				throw new CacheNotYetAvailableException();
			}
			
			return deserializeEntityCollection(fis);
		}
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
		return hasCacheContent(cacheFile);
	}
	
	private boolean hasCacheContent(File f) {
		synchronized (cacheFileMutex) {
			return f.exists() && f.length() > 0;
		}
	}
	
	public long lastUpdated() {
		synchronized (cacheFileMutex) {
			if (isCacheAvailable() && hasCacheContent()) {
				return cacheFile.lastModified();
			}	
		}
		return 0;
	}
	
	public long getLastUpdateDuration() {
		return lastUpdateDuration;
	}

//	public boolean requestUpdateLock() {
//		File f = getCacheLockFile();
//		
//		if (f != null && f.exists()) {
//			return false;
//		}
//		
//		try {
//			f.createNewFile();
//			return true;
//		} catch (IOException e) {
//			LOGGER.warn(e.getMessage(), e);
//			LOGGER.warn("Could not access cache lock file.");
//		}
//		
//		return false;
//	}
//	
//	public void freeUpdateLock() throws FileNotFoundException {
//		File f = getCacheLockFile();
//		
//		if (f != null && f.exists()) {
//			f.delete();
//		}
//	}
	
	public boolean isUpdateOngoing() {
		File f = getCacheLockFile();
		
		if (f.exists()) {
			return true;
		}
		
		return false;
	}

	private File getCacheLockFile() {
		synchronized (cacheFileMutex) {
			return new File(this.cacheFile.getParent(), this.cacheFile.getName()+".lock");
		}
	}
	
	public void updateCache(AccessGDB geoDB) throws CacheException, IOException {
		AbstractEntityCache<T> instance = getSingleInstance();
//		if (!instance.requestUpdateLock()) {
//			LOGGER.info("cache is currently already updating: "+ this.getClass().getSimpleName());
//			return;
//		}
		
		LOGGER.info("Getting DAO data for "+ this.getClass().getSimpleName());
		long start = System.currentTimeMillis();
		Collection<T> entities = getCollectionFromDAO(geoDB);
		instance.storeEntityCollection(entities);
		this.lastUpdateDuration = System.currentTimeMillis() - start;
		LOGGER.info("Update for "+ this.getClass().getSimpleName() +" took ms: "+this.lastUpdateDuration);
		
//		instance.freeUpdateLock();		
	}

	public boolean requiresUpdate() {
		if (this.isCacheAvailable()) {
			if (this.hasCacheContent()) {
				long lastUpdated = this.getSingleInstance().lastUpdated();
				
				if (System.currentTimeMillis() - lastUpdated > AbstractCacheScheduler.FIFTEEN_MINS_MS) {
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
