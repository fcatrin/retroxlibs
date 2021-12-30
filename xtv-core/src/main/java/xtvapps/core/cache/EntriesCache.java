package xtvapps.core.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class EntriesCache<T> {

	public enum Type {LRU, MRU}

	private final Map<String, T> entries = new HashMap<>();
	private final Map<String, Long> lastTimeUsed = new HashMap<>();

	final Type type;
	final int maxSize;
	long expire;
	
	public EntriesCache(int maxSize, Type type) {
		this.maxSize = maxSize;
		this.type = type;
	}
	
	public EntriesCache(int maxSize, long expire) {
		this.maxSize = maxSize;
		this.type = Type.LRU;
		this.expire = expire;
	}
	
	public void flush() {
		entries.clear();
		lastTimeUsed.clear();
	}
	
	public void put(String key, T value) {
		restrictSize();
		entries.put(key, value);
		lastTimeUsed.put(key, System.currentTimeMillis());
	}
	
	public T get(String key) {
		T value = entries.get(key);
		if (value == null) return null;
		
		long t = System.currentTimeMillis();
		
		if (expire!=0) {
			long lastTime = lastTimeUsed.get(key);
			if (t-lastTime > expire) {
				entries.remove(key);
				lastTimeUsed.remove(key);
				return null;
			}
		}
		
		lastTimeUsed.put(key, t);
		
		return value;
	}
	
	private void restrictSize() {
		while (entries.size() >= maxSize && maxSize>0) {  // avoid infinite loop by usage error
		
			String recentlyUsedKey = "";
			String leastUsedKey = "";
			long recentlyUsedTime = Long.MIN_VALUE;
			long leastUsedTime = Long.MAX_VALUE;
			
			for(Entry<String, Long> use : lastTimeUsed.entrySet()) {
				String key = use.getKey();
				long time = use.getValue();
				
				if (time < leastUsedTime) {
					leastUsedTime = time;
					leastUsedKey = key;
				}
				
				if (time > recentlyUsedTime) {
					recentlyUsedTime = time;
					recentlyUsedKey = key;
				}
			}
			
			if (type == Type.MRU) {
				entries.remove(recentlyUsedKey);
				lastTimeUsed.remove(recentlyUsedKey);
			} else {
				entries.remove(leastUsedKey);
				lastTimeUsed.remove(leastUsedKey);
			}
		}
	}

}
