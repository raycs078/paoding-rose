package net.paoding.rose.jade.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供 ConcurrentHashMap 缓存池的 {@link CacheProvider} 实现。
 * 
 * @author han.liao
 */
public class MockCacheProvider implements CacheProvider {

    private ConcurrentHashMap<String, MockCache> caches = new ConcurrentHashMap<String, MockCache>();

    private int defaultMaxSize = 100; // 默认值

    public int getDefaultMaxSize() {
        return defaultMaxSize;
    }

    public void setDefaultMaxSize(int defaultMaxSize) {
        this.defaultMaxSize = defaultMaxSize;
    }

    @Override
    public Cache getCacheByPool(String poolName) {

        MockCache cache = caches.get(poolName);
        if (cache == null) {
            cache = new MockCache(poolName);

            MockCache cacheExist = caches.putIfAbsent(poolName, cache);
            if (cacheExist != null) {
                cache = cacheExist;
            }
        }

        return cache;
    }
}
