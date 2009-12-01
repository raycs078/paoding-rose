package net.paoding.rose.jade.jadeinterface.app;

import net.paoding.rose.jade.jadeinterface.app.java.JadeAppConfig;
import net.paoding.rose.jade.jadeinterface.app.web.JadeWebAppConfig;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProviderMock;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * 实现 Spring-framework 的 {@link FactoryBean} 接口，以提供单例化的
 * {@link DataAccessProviderHolder} 对象。
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public class DataAccessProviderHolder implements FactoryBean, InitializingBean,
        ApplicationContextAware {

    protected static final DataAccessProvider mockProvider = new DataAccessProviderMock();

    protected ApplicationContext applicationContext;

    protected DataAccessProvider dataAccessProvider;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        JadeConfig config;

        if (applicationContext instanceof WebApplicationContext) {
            // 从  web.xml 配置的属性初始化
            config = new JadeWebAppConfig((WebApplicationContext) applicationContext);
        } else {
            // 从  applicationContext.xml 配置的属性初始化
            config = new JadeAppConfig(applicationContext);
        }

        // 从配置取得 dataAccessProvider 对象
        dataAccessProvider = JadeConfigResolver.configResolve(config);

        if (dataAccessProvider instanceof ApplicationContextAware) {
            // 向下转播  ApplicationContext 对象
            ((ApplicationContextAware) dataAccessProvider)
                    .setApplicationContext(applicationContext);
        }
    }

    @Override
    public boolean isSingleton() {
        return (dataAccessProvider != null);
    }

    @Override
    public Class<?> getObjectType() {
        return DataAccessProvider.class;
    }

    @Override
    public Object getObject() throws Exception {

        // 检查是否初始化
        if (dataAccessProvider == null) {
            return mockProvider;
        }

        return dataAccessProvider;
    }
}
