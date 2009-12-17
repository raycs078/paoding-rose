package net.paoding.rose.jade.jadeinterface.app;

import net.paoding.rose.jade.jadeinterface.cache.CacheProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;

/**
 * 提供 jade 的配置工具。
 * 
 * @author han.liao
 */
public class JadeConfigurer {

    /**
     * 配置 jade {@link DataAccessProvider} 实现。
     * 
     * @param providerClass - {@link DataAccessProvider} 实现
     */
    public static void setDataAccessProvider(Class<? extends DataAccessProvider> providerClass) {

        System.setProperty(JadeConfig.DATA_ACCESS_PROVIDER_CLASS, providerClass.getName());
    }

    /**
     * 配置 jade {@link CacheProvider} 实现。
     * 
     * @param providerClass - {@link CacheProvider} 实现
     */
    public static void setCacheProvider(Class<? extends CacheProvider> providerClass) {

        System.setProperty(JadeConfig.DATA_ACCESS_PROVIDER_CLASS, providerClass.getName());
    }
}
