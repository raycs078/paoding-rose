package net.paoding.rose.jade.jadeinterface.initializer;

import javax.servlet.ServletContext;

import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProviderHolder;

/**
 * 使用 {@link JadeInitializer} 配置当前系统使用的 Jade 实现, 设置的类 / 对象必须实现
 * {@link net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider}
 * 接口。
 * 
 * @author han.liao
 */
public class JadeInitializer {

    /**
     * 用所给的 Jade 实现初始化系统。
     * 
     * @param provider - Jade 实现
     */
    public static void initialize(DataAccessProvider provider) {

        if (DataAccessProviderHolder.getProvider() != null) {
            throw new IllegalStateException("DataAccessProviderHolder");
        }

        DataAccessProviderHolder.setProvider(provider);
    }

    /**
     * 用所给的 Jade 实现类初始化系统。
     * 
     * @param providerClass - Jade 实现类
     */
    public static void initialize(Class<? extends DataAccessProvider> providerClass)
            throws InstantiationException, IllegalAccessException {

        if (DataAccessProviderHolder.getProvider() != null) {
            throw new IllegalStateException("DataAccessProviderHolder");
        }

        DataAccessProvider provider = providerClass.newInstance();
        DataAccessProviderHolder.setProvider(provider);
    }

    /**
     * 用所给的 Jade 实现类初始化系统。
     * 
     * @param providerClassName - Jade 实现类的名称
     */
    public static void initialize(String providerClassName) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {

        if (DataAccessProviderHolder.getProvider() != null) {
            throw new IllegalStateException("DataAccessProviderHolder");
        }

        Class<?> providerClass = Class.forName(providerClassName);
        DataAccessProvider provider = (DataAccessProvider) providerClass.newInstance();
        DataAccessProviderHolder.setProvider(provider);
    }

    /**
     * 用所给的 ServletContext 初始化系统。
     * 
     * @param servletContext - {@link ServletContext}
     */
    public static void initialize(ServletContext servletContext) {

        String providerClassName = servletContext.getInitParameter("jadeDataAccessProviderClass");

        if (providerClassName == null) {
            throw new NullPointerException("jadeDataAccessProviderClass");
        }

        try {
            JadeInitializer.initialize(providerClassName);

        } catch (Exception e) {

            throw new IllegalArgumentException(providerClassName, e);
        }
    }
}
