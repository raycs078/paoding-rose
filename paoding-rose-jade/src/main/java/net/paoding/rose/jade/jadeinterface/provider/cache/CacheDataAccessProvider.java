package net.paoding.rose.jade.jadeinterface.provider.cache;

import javax.sql.DataSource;

import net.paoding.rose.jade.jadeinterface.cache.CacheProvider;
import net.paoding.rose.jade.jadeinterface.cache.EhCacheProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.springjdbctemplte.SpringJdbcTemplateDataAccessProvider;

/**
 * 提供 cache 版本的
 * {@link net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider}
 * 实现。
 * 
 * @author han.liao
 */
public class CacheDataAccessProvider extends SpringJdbcTemplateDataAccessProvider {

    // 可配置的缓存实现
    protected CacheProvider cacheProvider;

    public void setCacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    @Override
    public DataAccess createDataAccess(String dataSourceName) {

        // 创建  Ehcache 缓存实现
        if (cacheProvider == null) {
            cacheProvider = new EhCacheProvider();
        }

        return super.createDataAccess(dataSourceName);
    }

    @Override
    protected DataAccess createDataAccess(DataSource dataSource) {
        return new CacheDataAccess(dataSource, cacheProvider);
    }
}
