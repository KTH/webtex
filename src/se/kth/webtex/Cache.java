package se.kth.webtex;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache meta data and keep track of the generated image files.
 */
public class Cache {
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
		public String logMessage;
		
		public CacheData(int depth, File file, String logMessage) {
			this.depth = depth;
			this.file = file;
			this.logMessage = logMessage;
		}
	}
	
	private static final String CACHE_DIRECTORY = "/tmp/webtex/cache";
	
	private String dir;
	private Map<CacheKey, CacheData> cache; 
	
	/**
	 * Constructor, initializes cache.
	 */
	public Cache() {
		setCache(CACHE_DIRECTORY);
	}
	
	public File file(String key, int resolution) {
		CacheKey cacheKey = new CacheKey(key, resolution);
		if (cache.containsKey(cacheKey)) {
			return cache.get(cacheKey).file;
		} else {
			return null;
		}
	}

	public int depth(String key, int resolution) {
		CacheKey cacheKey = new CacheKey(key, resolution);
		if (cache.containsKey(cacheKey)) {
			return cache.get(cacheKey).depth;
		} else {
			return 0;
		}
	}

	public String logMessage(String key, int resolution) {
		CacheKey cacheKey = new CacheKey(key, resolution);
		if (cache.containsKey(cacheKey)) {
			return cache.get(cacheKey).logMessage;
		} else {
			return null;
		}
	}
	
	public boolean contains(String key, int resolution) {
		return cache.containsKey(new CacheKey(key, resolution));
	}
	
	/**
	 * Add given file to cache.
	 * @param file to add
	 * @param logMessage TODO
	 */
	public void put(String key, int resolution, int depth, File file, String logMessage) {
		File cacheFile = fileForKey(key, resolution);
		file.renameTo(cacheFile);
		cache.put(new CacheKey(key, resolution), new CacheData(depth, cacheFile, logMessage));
	}

	/**
	 * Set the root directory to cache files in. The directory will be created if it
	 * does not exist. It must be writable.
	 * @param dir root path.
	 */
	public void setCache(String dir) {
		new File(dir).mkdirs();
		emptyCache(dir);
		this.dir = dir;
		this.cache = new ConcurrentHashMap<CacheKey, CacheData>(); 
	}
	
	private void emptyCache(String dir) {
		File directory = new File(dir);
		for (File file : directory.listFiles()) {
			file.delete();
		}
	}

	/**
	 * Creates a file object for given key.
	 * @param key for the file
	 * @return a file object corresponding to the key
	 */
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
}
