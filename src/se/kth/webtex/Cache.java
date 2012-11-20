package se.kth.webtex;

/*
  Copyright (C) 2012 KTH, Kungliga tekniska hogskolan, http://www.kth.se

  This file is part of WebTex.

  WebTex is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  WebTex is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with WebTex.  If not, see <http://www.gnu.org/licenses/>
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

/**
 * Cache meta data and keep track of the generated image files. The cache
 * has a built-in thread that expires data after one week.
 */
public class Cache implements Runnable {
    // One week expiration time in milliseconds on cached items.
    private static final long EXPIRATION_TIME = TimeUnit.DAYS.toMillis(7);
    private static final long TIME_BETWEEN_EVICTIONS = 500;
    private static final long TIME_BETWEEN_EVICTIONRUNS = TimeUnit.SECONDS.toMillis(1);

    private String dir;
    private ConcurrentMap<CacheKey, CacheData> cache; 
    private Thread cachePurger;

    // Performance counters
    private long additions = 0;
    private long diskSize = 0;
    private long expired = 0;
    private Calendar startTime = Calendar.getInstance();

    public static synchronized Cache initCache(ServletContext context, String dir) {
        if (context.getAttribute("cache") == null) {
            context.setAttribute("cache", new Cache(dir));
        }
        return (Cache) context.getAttribute("cache");
    }

    public int size() {
        return cache.size();
    }

    public long getAdditions() {
        return additions;
    }

    public long getExpired() {
        return expired;
    }
    
    public long getDiskSize() {
        return diskSize;
    }
    
    public long getUptime() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, (int) -startTime.getTimeInMillis()/1000);
        return now.getTimeInMillis();
    }


    /**
     * @param key
     * @param resolution
     * @return an integer representing offset from the x-axis.
     */
    protected CacheData get(String key, int resolution) {
        CacheKey cacheKey = new CacheKey(key, resolution);
        if (cache.containsKey(cacheKey)) {
            touch(cacheKey);
            return cache.get(cacheKey);
        } else {
            return null;
        }
    }

    
    private void touch(CacheKey key) {
        CacheData data = cache.get(key);
        data.timestamp = new Date();
    }

    /**
     * @param key
     * @param resolution
     * @return true if an entry exists for the key - resolution combination.
     */
    public boolean contains(String key, int resolution) {
    	CacheData data = get(key, resolution);
    	if (data == null) {
    		return false;
    	}
        File file = data.getFile();		
        return (file != null) && file.exists();
    }

    @Override
    public void run() {
        try {
        	while (! Thread.currentThread().isInterrupted()) {
	            for (CacheKey key : cache.keySet()) {
	            	if (Thread.currentThread().isInterrupted()) {
	            		return;
	            	}
	            	
	            	Date bestAfter = new Date(new Date().getTime() - EXPIRATION_TIME);
	                if (timestamp(key.key, key.resolution).before(bestAfter)) {
	                    remove(key.key, key.resolution);
	                }
                    Thread.sleep(TIME_BETWEEN_EVICTIONS);
	            }
	            Thread.sleep(TIME_BETWEEN_EVICTIONRUNS);
	        }
        } catch (InterruptedException e) {}
    }
    
    public void destroy() {
    	try {
        	cachePurger.interrupt();
			cachePurger.wait();
		} catch (InterruptedException e) {}
    }
    
    /**
     * @param key
     * @param resolution
     * @return
     */
    private Date timestamp(String key, int resolution) {
        CacheKey cacheKey = new CacheKey(key, resolution);
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey).timestamp;
        } else {
            return null;
        }
    }

    /**
     * Add given file to cache.
     * @param key
     * @param resolution
     * @param file to add
     * @param logMessage
     */
    public synchronized void put(String key, int resolution, int depth, File file, String logMessage) {
    	int width = 0;
    	int height = 0;

    	try {
			BufferedImage image = ImageIO.read(file);
			width = image.getWidth();
			height = image.getHeight();
			image.flush();
		} catch (IOException e) {}

		File cacheFile = fileForKey(key, resolution);
        file.renameTo(cacheFile);
        cache.put(new CacheKey(key, resolution), 
    		new CacheData(depth, cacheFile, width, height, logMessage));
        additions++;
        diskSize += cacheFile.length();
    }

    private File fileForKey(String key, int resolution) {
        String fileName;

        if (key.hashCode() < 0) {
            fileName = "A" + Math.abs(key.hashCode());
        } else {
            fileName = "B" + key.hashCode();
        }
        fileName += "-" + resolution;

        return new File(dir + File.separator + fileName + TexRunner.IMAGE_SUFFIX);
    }

    /**
     * Constructor, initializes cache.
     * @param root 
     */
    private Cache(String root) {
        setCache(root + File.separator + "tmp" + File.separator + "cache");
        cache = new ConcurrentHashMap<CacheKey, CacheData>();
        cachePurger = new Thread(this);
        cachePurger.setDaemon(true);
        cachePurger.setName("Cache Purger");
        cachePurger.start();
    }

    private Cache() {}

    private synchronized void setCache(String dir) {
        new File(dir).mkdirs();
        emptyCache(dir);
        this.dir = dir;
    }

    private void emptyCache(String dir) {
        File directory = new File(dir);
        for (File file : directory.listFiles()) {
            file.delete();
        }
    }

    /**
     * Remove an entry from the cache.
     * @param key
     * @param resolution
     */
    private synchronized void remove(String key, int resolution) {
        File cacheFile = get(key, resolution).getFile();
        cache.remove(new CacheKey(key, resolution));
        diskSize -= cacheFile.length();
        expired++;
        cacheFile.delete();
    }

    /**
     * Cache entries are keyed on the expression string and resolution.
     */
    private class CacheKey {
        public String key;
        public int resolution;

        public CacheKey(String key, Integer resolution) {
            this.key = key;
            this.resolution = resolution;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CacheKey)) {
                return false;
            }
            CacheKey otherKey = (CacheKey) o;
            return this.key.equals(otherKey.key) && this.resolution == otherKey.resolution;
        }

        @Override
        public int hashCode() {
            String hashKey = this.resolution + "-" + this.key;
            return hashKey.hashCode();
        }
    }

    /**
     * The cached data is the File object and the depth, i.e., the distance
     * along the y-axis the image should be transposed in order to line up
     * correctly on the web page.
     */
    protected final class CacheData {
        private int depth;
        private int width;
        private int height;
        private File file;
        private String logMessage;
        private Date timestamp;

        public CacheData(int depth, File file, int width, int height, String logMessage) {
            this.depth = depth;
            this.width = width;
            this.height = height;
            this.file = file;
            this.logMessage = logMessage;
            this.timestamp = new Date();
        }

		public int getDepth() {
			return depth;
		}
        
		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public File getFile() {
			return file;
		}

		public String getLogMessage() {
			return logMessage;
		}

		public Date getTimestamp() {
			return timestamp;
		}
    }
}
