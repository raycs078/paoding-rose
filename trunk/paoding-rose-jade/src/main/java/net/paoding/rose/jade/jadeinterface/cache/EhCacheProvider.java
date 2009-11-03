package net.paoding.rose.jade.jadeinterface.cache;

import net.sf.ehcache.CacheManager;

/**
 * 提供 Ehcache 缓存池的 {@link CacheProvider} 实现。
 * 
 * <p>
 * EhCache 网址 <a href="http://ehcache.org/">http://ehcache.org/</a>
 * </p>
 * 
 * @author han.liao
 */
public class EhCacheProvider implements CacheProvider {

    private CacheManager cacheManager = new CacheManager();

    @Override
    public Cache getCacheByPool(String poolName) {

        if (!cacheManager.cacheExists(poolName)) {
            cacheManager.addCache(poolName);
        }

        net.sf.ehcache.Cache ehCache = cacheManager.getCache(poolName);

        return new EhCache(ehCache);
    }
}
