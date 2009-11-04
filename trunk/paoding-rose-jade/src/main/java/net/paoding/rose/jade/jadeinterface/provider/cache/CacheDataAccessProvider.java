package net.paoding.rose.jade.jadeinterface.provider.cache;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import net.paoding.rose.jade.jadeinterface.cache.CacheProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.exql.ExqlDataAccessProvider;

import org.springframework.web.context.WebApplicationContext;

/**
 * 提供 cache 版本的
 * {@link net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider}
 * 实现。
 * 
 * @author han.liao
 */
public class CacheDataAccessProvider extends ExqlDataAccessProvider {

    // 可配置的缓存实现
    protected CacheProvider cacheProvider;

    public void setCacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    @Override
    public DataAccess createDataAccess(String dataSourceName) {

        if (cacheProvider == null) {

            if (applicationContext instanceof WebApplicationContext) {

                ServletContext servletContext = ((WebApplicationContext) applicationContext) // NL 
                        .getServletContext();

                cacheProvider = initCacheProvider(servletContext);
            }
        }

        return super.createDataAccess(dataSourceName);
    }

    @Override
    protected DataAccess createDataAccess(DataSource dataSource) {

        DataAccess dataAccess = super.createDataAccess(dataSource);

        if (cacheProvider != null) {
            return new CacheDataAccess(dataAccess, cacheProvider);
        }

        return dataAccess;
    }

    /**
     * 根据 web.xml 配置初始化 {@link CacheProvider}.
     * 
     * @param servletContext - 容器的 {@link ServletContext}
     * 
     * @return {@link CacheProvider} 实例
     */
    protected CacheProvider initCacheProvider(ServletContext servletContext) {

        String providerClassName = servletContext.getInitParameter("jadeCacheProviderClass");

        try {
            if (providerClassName != null) {
                Class<?> providerClass = Class.forName(providerClassName);
                return (CacheProvider) providerClass.newInstance();
            }

        } catch (Exception e) {
            throw new IllegalArgumentException(providerClassName, e);
        }

        return null;
    }
}
