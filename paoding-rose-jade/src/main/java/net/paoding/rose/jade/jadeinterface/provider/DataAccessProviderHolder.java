package net.paoding.rose.jade.jadeinterface.provider;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 提供可配置的 {@link DataAccessProvider} 占位符。
 * 
 * <p>
 * 提示：使用
 * {@link net.paoding.rose.jade.jadeinterface.initializer.JadeInitializer}
 * 配置系统使用的 DataAccessProvider 实现。
 * </p>
 * 
 * @author han.liao
 */
public class DataAccessProviderHolder implements DataAccessProvider, ApplicationContextAware {

    private static DataAccessProvider provider;

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        DataAccessProviderHolder.applicationContext = applicationContext;

        if ((DataAccessProviderHolder.provider != null)
                && (DataAccessProviderHolder.provider instanceof ApplicationContextAware)) {
            ((ApplicationContextAware) DataAccessProviderHolder.provider)
                    .setApplicationContext(applicationContext);
        }
    }

    /**
     * 设置全局使用的 {@link DataAccessProvider} 实现。
     * 
     * @param provider - {@link DataAccessProvider} 实现
     */
    public static void setProvider(DataAccessProvider provider) {

        DataAccessProviderHolder.provider = provider;

        if ((DataAccessProviderHolder.applicationContext != null)
                && (DataAccessProviderHolder.provider instanceof ApplicationContextAware)) {
            ((ApplicationContextAware) DataAccessProviderHolder.provider)
                    .setApplicationContext(applicationContext);
        }
    }

    /**
     * 获取全局设置的 {@link DataAccessProvider} 实现。
     * 
     * @return {@link DataAccessProvider} 实现
     */
    public static DataAccessProvider getProvider() {
        return provider;
    }

    @Override
    public DataAccess createDataAccess(String dataSourceName) {
        return provider.createDataAccess(dataSourceName);
    }
}
