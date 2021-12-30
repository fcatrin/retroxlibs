package xtvapps.core.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruCache<K, V> {
    private final LinkedHashMap<K, Entry<V>> map = new LinkedHashMap<>();
    private final int size;

    public LruCache(int size) {
        this.size = size;
    }

    public V get(K key) {
        synchronized (this) {
            Entry<V> entry = map.get(key);
            return entry == null ? null : entry.value;
        }
    }

    public void put(K key, V value) {
        synchronized (this) {
            Entry<V> entry = new Entry<>(value);
            map.put(key, entry);
            trimToSize(size);
        }
    }

    public void remove(K key){
        synchronized (this) {
            map.remove(key);
        }
    }

    public void evictAll() {
        map.clear();
    }

    public int getSize() {
        int total = 0;
        synchronized (this) {
            for (Map.Entry<K, Entry<V>> entry : map.entrySet()) {
                total += sizeOf(entry.getKey(), entry.getValue().value);
            }
        }
        return total;
    }

    protected int sizeOf(K key, V item) {
        return 1;
    }

    private void trimToSize(int size) {
        while (getSize() > size) {
            removeEldest();
        }
    }

    private void removeEldest() {
        int minUsage = Integer.MAX_VALUE;
        K eldestKey = null;

        synchronized (this) {
            for (Map.Entry<K, Entry<V>> entry : map.entrySet()) {
                if (entry.getValue().usage < minUsage) {
                    minUsage = entry.getValue().usage;
                    eldestKey = entry.getKey();
                }
            }

            map.remove(eldestKey);
        }
    }

    private static class Entry<V> {
        private final V value;
        private int usage = 0;

        public Entry(V value) {
            this.value = value;
        }

        public V getValue() {
            usage++;
            return value;
        }
   }
}
