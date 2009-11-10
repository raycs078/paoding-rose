package net.paoding.rose.jade.jadeinterface.initializer;

import net.paoding.rose.jade.jadeinterface.cache.CacheProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProviderHolder;
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
     */
    public static void initialize(JadeInitContext context) {

        try {
            doInitialize(context);

        } catch (ClassCastException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Jade initialize failed [ClassCastException]", e);
            }
            throw new IllegalArgumentException("ClassCastException", e);
        } catch (ClassNotFoundException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Jade initialize failed [ClassNotFoundException]", e);
            }
            throw new IllegalArgumentException("ClassNotFoundException", e);
        } catch (InstantiationException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Jade initialize failed [InstantiationException]", e);
            }
            throw new IllegalArgumentException("InstantiationException", e);
        } catch (IllegalAccessException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Jade initialize failed [IllegalAccessException]", e);
            }
            throw new IllegalArgumentException("IllegalAccessException", e);
        }
    }

    /**
     * 用所给的配置属性初始化 Jade 系统。
     * 
     * @param context - Jade 配置属性
     * 
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    protected static void doInitialize(JadeInitContext context) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {

        DataAccessProviderHolder dataAccessProviderHolder = DataAccessProviderHolder.getInstance();

        // 配置是全局唯一的，只能配置一次
        if (dataAccessProviderHolder.getProvider() != null) {
            throw new IllegalStateException("DataAccessProviderHolder");
        }

        // 获得配置的  DataAccessProvider
        DataAccessProvider dataAccessProvider = context.get(JadeInitContext.DATA_ACCESS_PROVIDER);
        if (dataAccessProvider == null) {

            Object dataAccessProviderClassName = context
                    .get(JadeInitContext.DATA_ACCESS_PROVIDER_CLASS);
            if (dataAccessProviderClassName == null) {
                throw new NullPointerException("Config \"jadeDataAccessProviderClass\" not found.");
            }

            Class<?> dataAccessProviderClass;
            if (dataAccessProviderClassName instanceof Class<?>) {
                dataAccessProviderClass = (Class<?>) dataAccessProviderClassName;
            } else if (dataAccessProviderClassName instanceof String) {
                dataAccessProviderClass = Class.forName((String) dataAccessProviderClassName);
            } else {
                throw new NullPointerException("Config \"jadeDataAccessProviderClass\" type error.");
            }

            if (!DataAccessProvider.class.isAssignableFrom(dataAccessProviderClass)) {
                throw new ClassCastException(dataAccessProviderClass + " must implements "
                        + DataAccessProvider.class.getName());
            }

            if (logger.isDebugEnabled()) {
                logger.debug("jadeDataAccessProviderClass = " + dataAccessProviderClass.getName());
            }

            dataAccessProvider = (DataAccessProvider) dataAccessProviderClass.newInstance();
        }

        // 获得配置的  CacheProvider
        CacheProvider cacheProvider = context.get(JadeInitContext.CACHE_PROVIDER);
        if (cacheProvider == null) {

            Object cacheProviderClassName = context.get(JadeInitContext.CACHE_PROVIDER_CLASS);
            if (cacheProviderClassName != null) {

                Class<?> cacheProviderClass;
                if (cacheProviderClassName instanceof Class<?>) {
                    cacheProviderClass = (Class<?>) cacheProviderClassName;
                } else if (cacheProviderClassName instanceof String) {
                    cacheProviderClass = Class.forName((String) cacheProviderClassName);
                } else {
                    throw new NullPointerException(
                            "Config \"jadeDataAccessProviderClass\" type error.");
                }

                if (!CacheProvider.class.isAssignableFrom(cacheProviderClass)) {
                    throw new ClassCastException(cacheProviderClass + " must implements "
                            + CacheProvider.class.getName());
                }

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

        // 注册全局唯一的 DataAccessProvider
        dataAccessProviderHolder.setProvider(dataAccessProvider);
    }
}
