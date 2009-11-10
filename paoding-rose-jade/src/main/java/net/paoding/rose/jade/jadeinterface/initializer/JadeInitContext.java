package net.paoding.rose.jade.jadeinterface.initializer;

import net.paoding.rose.jade.jadeinterface.cache.CacheProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;

/**
 * 配置 Jade 启动参数的上下文。
 * 
 * @author han.liao
 */
public abstract class JadeInitContext {

    /**
     * 配置 {@link DataAccessProvider}
     */
    public static final String DATA_ACCESS_PROVIDER = "jadeDataAccessProvider";

    /**
     * 配置 {@link DataAccessProvider} 的类名
     */
    public static final String DATA_ACCESS_PROVIDER_CLASS = "jadeDataAccessProviderClass";

    /**
     * 配置 {@link CacheProvider}
     */
    public static final String CACHE_PROVIDER = "jadeCacheProvider";

    /**
     * 配置 {@link CacheProvider} 的类名
     */
    public static final String CACHE_PROVIDER_CLASS = "jadeCacheProviderClass";

    /**
     * 放入配置参数。
     * 
     * @param name - 参数名称
     * @param value - 对象或参数值
     */
    public abstract void put(String name, Object value);

    /**
     * 取回配置参数。
     * 
     * @param <T> - 参数类型
     * 
     * @param name - 参数名称
     * 
     * @return 对象或参数值
     */
    public abstract <T> T get(String name);

    /**
     * 创建空白的配置。
     * 
     * @return 空白的配置
     */
    public static JadeInitContext newInstance() {
        return new JadeInitContextImpl();
    }
}
