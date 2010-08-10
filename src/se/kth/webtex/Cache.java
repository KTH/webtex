package se.kth.webtex;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Cache meta data and keep track of the generated image files. The cache
 * has a built-in thread that expires data after one week.
 */
public class Cache implements Runnable {
	// One week expiration time in milliseconds on cached items.
	private static final long EXPIRATION_TIME = TimeUnit.DAYS.toMillis(7);

	private String dir;
	private Map<CacheKey, CacheData> cache; 
	private Thread cachePurger;
	
	/**
	 * @param key
	 * @param resolution
	 * @return the image file as a File object.
	 */
	public File file(String key, int resolution) {
		CacheKey cacheKey = new CacheKey(key, resolution);
		if (cache.containsKey(cacheKey)) {
			touch(cacheKey);
			return cache.get(cacheKey).file;
		} else {
			return null;
		}
	}

	/**
	 * @param key
	 * @param resolution
	 * @return an integer representing offset from the x-axis.
	 */
	public int depth(String key, int resolution) {
		CacheKey cacheKey = new CacheKey(key, resolution);
		if (cache.containsKey(cacheKey)) {
			touch(cacheKey);
			return cache.get(cacheKey).depth;
		} else {
			return 0;
		}
	}

	/**
	 * @param key
	 * @param resolution
	 * @return the log message from the image generation.
	 */
	public Iterable<String> logMessage(String key, int resolution) {
		CacheKey cacheKey = new CacheKey(key, resolution);
		if (cache.containsKey(cacheKey)) {
			touch(cacheKey);
			return cache.get(cacheKey).logMessage;
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
		File file = file(key, resolution);		
		return (file != null) && file.exists();
	}
	
	@Override
	public void run() {
		while(true) {
			for (CacheKey key : cache.keySet()) {
				Date bestAfter = new Date(new Date().getTime() - EXPIRATION_TIME);
				if (timestamp(key.key, key.resolution).before(bestAfter)) {
					remove(key.key, key.resolution);
				}
				try {
					Thread.sleep(TimeUnit.SECONDS.toMillis(1));
				} catch (InterruptedException e) {
				}
			}
		}
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
	public void put(String key, int resolution, int depth, File file, Iterable<String> logMessage) {
		File cacheFile = fileForKey(key, resolution);
		file.renameTo(cacheFile);
		cache.put(new CacheKey(key, resolution), new CacheData(depth, cacheFile, logMessage));
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
	public Cache(String root) {
		setCache(root + File.separator + "tmp" + File.separator + "cache");
		this.cache = new ConcurrentHashMap<CacheKey, CacheData>();
		this.cachePurger = new Thread(this);
		cachePurger.start();
	}

	private void setCache(String dir) {
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
	private void remove(String key, int resolution) {
		File cacheFile = file(key, resolution);
		cache.remove(new CacheKey(key, resolution));
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
	private class CacheData {
		public int depth;
		public File file;
		public Iterable<String> logMessage;
		public Date timestamp;
		
		public CacheData(int depth, File file, Iterable<String> logMessage) {
			this.depth = depth;
			this.file = file;
			this.logMessage = logMessage;
			this.timestamp = new Date();
		}
	}
}
