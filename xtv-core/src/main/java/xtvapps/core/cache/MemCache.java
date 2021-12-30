package xtvapps.core.cache;

import xtvapps.core.Log;

public class MemCache<T> implements Cache<T> {
	private static final String LOGTAG = MemCache.class.getSimpleName();
	private static final int KILOBYTES = 1024;

	private final LruCache<String, T> mMemoryCache;
	
	public MemCache(int cacheSize) {
		mMemoryCache = new LruCache<>(cacheSize);
	}
	
	public MemCache(float percentSize, final CacheSizeDescriptor<T> sizeDescriptor) {
		 int maxMemory = (int) (Runtime.getRuntime().maxMemory() / KILOBYTES);
		 final int cacheSize = (int)(maxMemory * percentSize);
		 Log.d(LOGTAG, "GC memCache max:" + maxMemory + ", size:" + cacheSize);
		 mMemoryCache = new LruCache<String, T>(cacheSize) {
		        @Override
		        protected int sizeOf(String key, T item) {
		        	int size = sizeDescriptor.getSize(item);
		         	Log.d(LOGTAG, "GC memCache element size:" + size + ", size:" + cacheSize + ", key:" + key);
		         	return size;
		        }
		    };
	}
	
	@Override
	public T get(String key) {
		T entry = mMemoryCache.get(key);
		Log.d(LOGTAG, entry == null ? ("Memory Cache miss for " + key) : ("Memory Cache hit for " + key));
		return entry;
	}

	@Override
	public void put(String key, T entry) {
		mMemoryCache.put(key, entry);
	}

	@Override
	public void flush() {
		Log.d(LOGTAG, "Flush");
		mMemoryCache.evictAll();
	}

	public void remove(String key) {
		mMemoryCache.remove(key);
	}

}
