package net.paoding.rose.jade.jadeinterface.cache;

/**
 * 定义 CacheProvider 接口从缓存池名称获取实例。
 * 
 * @author han.liao
 */
public interface CacheProvider {

    /**
     * 从缓存池的名称获取实例。
     * 
     * @param poolName - 缓存池的名称
     * 
     * @return 缓存池实例
     */
    Cache getCacheByPool(String poolName);
}
