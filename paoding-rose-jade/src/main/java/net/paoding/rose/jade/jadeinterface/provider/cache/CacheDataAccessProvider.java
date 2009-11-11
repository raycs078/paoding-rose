package net.paoding.rose.jade.jadeinterface.provider.cache;

import net.paoding.rose.jade.jadeinterface.cache.CacheProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 提供支持缓存的 {@link DataAccessProvider} 包装器实现。
 * 
 * @author han.liao
 */
public class CacheDataAccessProvider implements DataAccessProvider, ApplicationContextAware {

    protected final DataAccessProvider dataAccessProvider;

    protected final CacheProvider cacheProvider;

    public CacheDataAccessProvider(DataAccessProvider dataAccessProvider,
            CacheProvider cacheProvider) {
        this.dataAccessProvider = dataAccessProvider;
        this.cacheProvider = cacheProvider;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        if (dataAccessProvider instanceof ApplicationContextAware) {
            // 向下转播  ApplicationContext 对象
            ((ApplicationContextAware) dataAccessProvider)
                    .setApplicationContext(applicationContext);
        }
    }

    @Override
    public DataAccess createDataAccess(String dataSourceName) {

        // 含缓存逻辑的  DataAccess
        return new CacheDataAccess( // NL
                dataAccessProvider.createDataAccess(dataSourceName), // NL
                cacheProvider);
    }
}
