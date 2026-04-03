package com.speechify;

/**
 *
 * Use the provided com.speechify.LRUCacheProviderTest in `src/test/java/LruCacheTest.java` to validate your
 * implementation.
 *
 * You may:
 *  - Read online API references for Java standard library or JVM collections.
 * You must not:
 *  - Read guides about how to code an LRU cache.
 */

public class LRUCacheProvider {
    public static <T> LRUCache<T> createLRUCache(CacheLimits options) {
        return new LruCacheImpl<>(options.getMaxItemsCount());
    }

    private static class LruCacheImpl<T> implements LRUCache<T> {
        private final int maxItemsCount;
        private final java.util.LinkedHashMap<String, T> map;

        private LruCacheImpl(int maxItemsCount) {
            this.maxItemsCount = maxItemsCount;
            this.map = new java.util.LinkedHashMap<String, T>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, T> eldest) {
                    return size() > LruCacheImpl.this.maxItemsCount;
                }
            };
        }

        @Override
        public synchronized T get(String key) {
            return map.get(key);
        }

        @Override
        public synchronized void set(String key, T value) {
            map.put(key, value);
        }
    }
}
