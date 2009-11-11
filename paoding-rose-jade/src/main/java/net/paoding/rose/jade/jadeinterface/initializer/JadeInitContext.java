package net.paoding.rose.jade.jadeinterface.initializer;

import java.util.Map;

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
     * 取回配置参数中的类名 / 类对象。
     * 
     * @param param - 参数名称
     * 
     * @return 参数中的类名 / 类对象
     * @throws ClassNotFoundException
     */
    public Class<?> getClassOrName(String param, Class<?> derivedClass)
            throws ClassNotFoundException {

        // 获取配置参数
        Object value = get(param);
        if (value != null) {
            Class<?> clazz;

            // 从配置参数获取类
            if (value instanceof Class<?>) {
                clazz = (Class<?>) value;
            } else if (value instanceof String) {
                clazz = Class.forName((String) value);
            } else {
                throw new ClassCastException("Config \"" + param + "\" must be String or Class.");
            }

            // 校验配置的类
            if (!derivedClass.isAssignableFrom(clazz)) {
                throw new ClassCastException(clazz + " must implements " + derivedClass.getName());
            }

            return clazz;
        }

        // 获取配置失败
        return null;
    }

    /**
     * 放入配置参数。
     * 
     * @param param - 参数名称
     * @param value - 对象或参数值
     */
    public abstract void put(String param, Object value);

    /**
     * 取回配置参数。
     * 
     * @param param - 参数名称
     * 
     * @return 对象或参数值
     */
    public abstract Object get(String param);

    /**
     * 创建空白的配置。
     * 
     * @return 空白的配置
     */
    public static JadeInitContext newInstance() {

        return new JadeInitContextImpl();
    }

    /**
     * 从 {@link java.util.Map} 创建配置。
     * 
     * @param map - 配置属性
     * 
     * @return 空白的配置
     */
    public static JadeInitContext newInstance(Map<String, Object> map) {

        return new JadeInitContextImpl(map);
    }
}
