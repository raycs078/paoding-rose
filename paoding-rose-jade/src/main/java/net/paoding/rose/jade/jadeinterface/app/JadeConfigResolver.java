package net.paoding.rose.jade.jadeinterface.app;

import net.paoding.rose.jade.jadeinterface.cache.CacheProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.cache.CacheDataAccessProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JadeConfigResolver {

    protected static final Log logger = LogFactory.getLog(JadeConfigResolver.class);

    /**
     * 用所给的配置属性初始化 Jade 系统。
     * 
     * @param config - Jade 配置属性
     * 
     * @return {@link DataAccessProvider}
     */
    public static DataAccessProvider configResolve(JadeConfig config) {

        try {
            DataAccessProvider dataAccessProvider = findDataAccessProvider(config);
            if (dataAccessProvider != null) {

                // 如果配置  CacheProvider, 包装配置的 DataAccessProvider
                CacheProvider cacheProvider = findCacheProvider(config);
                if (cacheProvider != null) {
                    dataAccessProvider = new CacheDataAccessProvider( // NL
                            dataAccessProvider, cacheProvider);
                }
            }

            return dataAccessProvider;

        } catch (ClassCastException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Config resolve failed [ClassCastException]", e);
            }
        } catch (ClassNotFoundException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Config resolve failed [ClassNotFoundException]", e);
            }
        } catch (InstantiationException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Config resolve failed [InstantiationException]", e);
            }
        } catch (IllegalAccessException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Config resolve failed [IllegalAccessException]", e);
            }
        }

        return null;
    }

    protected static DataAccessProvider findDataAccessProvider(JadeConfig config)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        // 获得配置的  DataAccessProvider
        DataAccessProvider dataAccessProvider = (DataAccessProvider) config
                .getConfig(JadeConfig.DATA_ACCESS_PROVIDER);
        if (dataAccessProvider == null) {

            Class<?> dataAccessProviderClass = config.getType(
                    JadeConfig.DATA_ACCESS_PROVIDER_CLASS, DataAccessProvider.class);
            if (dataAccessProviderClass != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(JadeConfig.DATA_ACCESS_PROVIDER_CLASS + " = "
                            + dataAccessProviderClass.getName());
                }

                dataAccessProvider = (DataAccessProvider) dataAccessProviderClass.newInstance();
            }

            if (dataAccessProvider == null) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Config " + JadeConfig.DATA_ACCESS_PROVIDER_CLASS + " not found.");
                }
            }
        }

        return dataAccessProvider;
    }

    protected static CacheProvider findCacheProvider(JadeConfig config)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        // 获得配置的  CacheProvider
        CacheProvider cacheProvider = (CacheProvider) config.getConfig(JadeConfig.CACHE_PROVIDER);
        if (cacheProvider == null) {

            Class<?> cacheProviderClass = config.getType(JadeConfig.CACHE_PROVIDER_CLASS,
                    CacheProvider.class);
            if (cacheProviderClass != null) {

                if (logger.isDebugEnabled()) {
                    logger.debug(JadeConfig.CACHE_PROVIDER_CLASS + " = "
                            + cacheProviderClass.getName());
                }

                cacheProvider = (CacheProvider) cacheProviderClass.newInstance();
            }
        }

        return cacheProvider;
    }
}
