package net.paoding.rose.jade.jadeinterface.provider;

import javax.servlet.ServletContext;

import net.paoding.rose.jade.jadeinterface.initializer.JadeInitializer;
import net.paoding.rose.jade.jadeinterface.initializer.JadeServletInitContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * 提供可配置的 {@link DataAccessProvider} 占位符。 <BR>
 * 
 * 提示：使用 {@link JadeInitializer} 初始化系统使用的 {@link DataAccessProvider} 实现。
 * 
 * @author han.liao
 */
public class DataAccessProviderHolder implements DataAccessProvider {

    private static DataAccessProviderHolder _instance = new DataAccessProviderHolder();

    private DataAccessProvider provider;

    private ApplicationContext applicationContext;

    public static DataAccessProviderHolder getInstance() {
        return _instance;
    }

    // 单例构造函数
    private DataAccessProviderHolder() {
        super();
    }

    /**
     * 设置全局使用的 {@link DataAccessProvider} 实现。
     * 
     * @param provider - {@link DataAccessProvider} 实现
     */
    public void setProvider(DataAccessProvider provider) {

        this.provider = provider;

        if ((applicationContext != null) && (provider instanceof ApplicationContextAware)) {
            // 向下转播  ApplicationContext 对象
            ((ApplicationContextAware) provider).setApplicationContext(applicationContext);
        }
    }

    /**
     * 获取全局设置的 {@link DataAccessProvider} 实现。
     * 
     * @return {@link DataAccessProvider} 实现
     */
    public DataAccessProvider getProvider() {
        return provider;
    }

    /**
     * 设置全局设置的 {@link ApplicationContext}.
     * 
     * @param applicationContext - {@link ApplicationContext}
     */
    public void setApplicationContext(ApplicationContext applicationContext) {

        this.applicationContext = applicationContext;

        if ((provider != null) && (provider instanceof ApplicationContextAware)) {
            // 向下转播  ApplicationContext 对象
            ((ApplicationContextAware) provider).setApplicationContext(applicationContext);
        }
    }

    /**
     * 获取全局设置的 {@link ApplicationContext}.
     * 
     * @return {@link ApplicationContext}
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public DataAccess createDataAccess(String dataSourceName) {

        // 自动检查是否设置 {@link DataAccessProvider} 实现。
        if (provider == null) {

            // 从  web.xml 配置的属性初始化
            if (applicationContext instanceof WebApplicationContext) {
                ServletContext servletContext = ((WebApplicationContext) applicationContext) // NL 
                        .getServletContext();
                JadeInitializer.initialize(new JadeServletInitContext(servletContext));
            }
        }

        // 检查是否初始化
        if (provider == null) {
            throw new IllegalStateException("Jade not initialized");
        }

        return provider.createDataAccess(dataSourceName);
    }
}
