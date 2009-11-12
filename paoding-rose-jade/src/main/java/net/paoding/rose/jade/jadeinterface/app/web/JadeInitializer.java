package net.paoding.rose.jade.jadeinterface.app.web;

import net.paoding.rose.jade.jadeinterface.cache.CacheProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.cache.CacheDataAccessProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 使用 {@link JadeInitializer} 配置当前系统使用的 Jade 实现, 设置的类 / 对象必须实现
 * {@link net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider}
 * 接口。
 * 
 * @author han.liao
 */
public class JadeInitializer {

    protected static final Log logger = LogFactory.getLog(JadeInitializer.class);

    /**
     * 用所给的配置属性初始化 Jade 系统。
     * 
     * @param context - Jade 配置属性
     * 
     * @return {@link DataAccessProvider}
     */
    public static DataAccessProvider initialize(JadeInitContext context) {

        try {
            return doInitialize(context);

        } catch (ClassCastException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Jade initialize failed [ClassCastException]", e);
            }
        } catch (ClassNotFoundException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Jade initialize failed [ClassNotFoundException]", e);
            }
        } catch (InstantiationException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Jade initialize failed [InstantiationException]", e);
            }
        } catch (IllegalAccessException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Jade initialize failed [IllegalAccessException]", e);
            }
        }

        return null;
    }

    /**
     * 用所给的配置属性初始化 Jade 系统。
     * 
     * @param context - Jade 配置属性
     * 
     * @return {@link DataAccessProvider}
     * 
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    protected static DataAccessProvider doInitialize(JadeInitContext context)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        // 获得配置的  DataAccessProvider
        DataAccessProvider dataAccessProvider = (DataAccessProvider) context
                .get(JadeInitContext.DATA_ACCESS_PROVIDER);
        if (dataAccessProvider == null) {

            Class<?> dataAccessProviderClass = context.getClassOrName(
                    JadeInitContext.DATA_ACCESS_PROVIDER_CLASS, DataAccessProvider.class);
            if (dataAccessProviderClass == null) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Config \"jadeDataAccessProviderClass\" not found.");
                }
                return null;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("jadeDataAccessProviderClass = " + dataAccessProviderClass.getName());
            }

            dataAccessProvider = (DataAccessProvider) dataAccessProviderClass.newInstance();
        }

        // 获得配置的  CacheProvider
        CacheProvider cacheProvider = (CacheProvider) context.get(JadeInitContext.CACHE_PROVIDER);
        if (cacheProvider == null) {

            Class<?> cacheProviderClass = context.getClassOrName(
                    JadeInitContext.CACHE_PROVIDER_CLASS, CacheProvider.class);
            if (cacheProviderClass != null) {

                if (logger.isDebugEnabled()) {
                    logger.debug("jadeCacheProviderClass = " + cacheProviderClass.getName());
                }

                cacheProvider = (CacheProvider) cacheProviderClass.newInstance();
            }
        }

        // 如果配置  CacheProvider, 包装配置的 DataAccessProvider
        if (cacheProvider != null) {
            dataAccessProvider = new CacheDataAccessProvider(dataAccessProvider, cacheProvider);
        }

        return dataAccessProvider;
    }
}
