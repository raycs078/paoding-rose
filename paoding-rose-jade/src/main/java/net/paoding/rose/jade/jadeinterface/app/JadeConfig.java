package net.paoding.rose.jade.jadeinterface.app;

import java.util.Map;

import net.paoding.rose.jade.jadeinterface.cache.CacheProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;

/**
 * 定义 Jade 配置参数。
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public abstract class JadeConfig {

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
     * 取回配置参数中的类名 / 类对象。
     * 
     * @param param - 参数名称
     * 
     * @return 参数中的类名 / 类对象
     */
    public final Class<?> getType(String param, Class<?> derivedClass)
            throws ClassNotFoundException {

        Object value = getConfig(param);
        if (value != null) {

            // 从配置参数获取类名 / 或者类本身
            Class<?> configClass;
            if (value instanceof Class<?>) {
                configClass = (Class<?>) value;
            } else if (value instanceof String) {
                configClass = Class.forName((String) value);
            } else {
                throw new ClassCastException("Config \"" + param + "\" must be String or Class.");
            }

            // 校验配置的类
            if (!derivedClass.isAssignableFrom(configClass)) {
                throw new ClassCastException(configClass + " must implements "
                        + derivedClass.getName());
            }
            return configClass;
        }

        return null;
    }

    /**
     * 取回配置参数。
     * 
     * @param param - 参数名称
     * 
     * @return 对象或参数值
     */
    public abstract Object getConfig(String param);

    /**
     * 从 {@link java.util.Map} 创建配置。
     * 
     * @param map - 配置属性
     * 
     * @return 空白的配置
     */
    public static JadeConfig newInstance(Map<String, Object> map) {
        return new JadeConfigImpl(map);
    }
}
