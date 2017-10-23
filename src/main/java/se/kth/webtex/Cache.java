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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

/**
 * Cache meta data and keep track of the generated image files. The cache
 * has a built-in thread that expires data after one week.
 */
public class Cache extends LinkedHashMap<Cache.CacheKey, Cache.CacheData> {
    private static final long serialVersionUID = 1L;
    private static final long startTime = System.currentTimeMillis();

    private String dir;

    // Performance counters
    private long additions = 0;
    private long diskSize = 0;
    private long expired = 0;
    private int size = 1000;

    // Hide default constructor.
    private Cache() {}

    public static synchronized Cache initCache(ServletContext context, String dir, int size) {
        if (context.getAttribute("cache") == null) {
            Cache cache = new Cache(dir);
            cache.setSize(size);
            context.setAttribute("cache", cache);
        }
        return (Cache) context.getAttribute("cache");
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<Cache.CacheKey, Cache.CacheData> eldest) {
        if (size() > size) {
            remove(eldest.getKey());
        }
        return false;
    }

    /**
     * Set the size of the FIFO cache.
     * 
     * @param size the size of the cache.
     */
    protected void setSize(int size) {
        this.size = size;
    }

    /**
     * Get the current size of the FIFO cache.
     * 
     * @return the current size.
     */
    protected int getSize() {
        return size;
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
        return System.currentTimeMillis() - startTime;
    }


    /**
     * @param key
     * @param resolution
     * @return an integer representing offset from the x-axis.
     */
    protected CacheData get(String key, int resolution) {
        CacheKey cacheKey = new CacheKey(key, resolution);
        return get(cacheKey);
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
        File cacheFile = null;

        try {
            BufferedImage image = ImageIO.read(file);
            width = image.getWidth();
            height = image.getHeight();
            image.flush();
            cacheFile = fileForKey(key, resolution);
            file.renameTo(cacheFile);
            diskSize += cacheFile.length();
        } catch (Exception e) {}

        put(new CacheKey(key, resolution), new CacheData(depth, cacheFile, width, height, logMessage));
        additions++;
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
        createCache(root + File.separator + "tmp" + File.separator + "cache");
    }

    private synchronized void createCache(String dir) {
        File directory = new File(dir);
        directory.mkdirs();
        for (File file : directory.listFiles()) {
            file.delete();
        }
        this.dir = dir;
    }

    public synchronized CacheData remove(Object key) {
        CacheKey cacheKey = (CacheKey) key;
        CacheData data = super.remove(cacheKey);

        File cacheFile = data.getFile();
        expired++;
        if (cacheFile != null) {
            diskSize -= cacheFile.length();
            cacheFile.delete();
        }
        return data;
    }

    public synchronized boolean remove(Object key, Object value) {
        return remove(key) != null;
    }

    /**
     * Cache entries are keyed on the expression string and resolution.
     */
    protected final class CacheKey {
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
