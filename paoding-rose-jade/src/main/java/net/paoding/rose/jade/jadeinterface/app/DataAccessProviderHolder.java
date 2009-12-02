package net.paoding.rose.jade.jadeinterface.app;

import net.paoding.rose.jade.jadeinterface.app.java.JadeAppConfig;
import net.paoding.rose.jade.jadeinterface.app.java.JadeEnvConfig;
import net.paoding.rose.jade.jadeinterface.app.java.JadePropConfig;
import net.paoding.rose.jade.jadeinterface.app.web.JadeWebAppConfig;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProviderMock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;

/**
 * 实现 Spring-framework 的 {@link FactoryBean} 接口，以提供单例化的
 * {@link DataAccessProviderHolder} 对象。
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public class DataAccessProviderHolder implements FactoryBean, InitializingBean,
        ApplicationContextAware {

    protected static final Log logger = LogFactory.getLog(DataAccessProviderHolder.class);

    protected static final DataAccessProvider mockProvider = new DataAccessProviderMock();

    protected ApplicationContext applicationContext;

    protected DataAccessProvider dataAccessProvider;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (applicationContext instanceof WebApplicationContext) {
            // 从  web.xml 配置的属性初始化
            logger.info("Try initializing from [web.xml]");
            JadeConfig config = new JadeWebAppConfig((WebApplicationContext) applicationContext);
            dataAccessProvider = JadeConfigResolver.configResolve(config);
            if (dataAccessProvider != null) {
                logger.info("Jade initialized from [web.xml]");
            }
        }

        if (dataAccessProvider == null) {
            // 从  jade.properties 配置的属性初始化
            logger.info("Try initializing from [jade.properties]");
            Resource properties = applicationContext.getResource("jade.properties");
            if (properties.exists()) {
                dataAccessProvider = JadeConfigResolver.configResolve(// NL
                        new JadePropConfig(properties));
            } else {
                logger.info("Resource [" + properties + "] not found");
            }
            if (dataAccessProvider != null) {
                logger.info("Jade initialized from [" + properties.getURL() + "]");
            }
        }

        if (dataAccessProvider == null) {
            // 从环境变量配置的属性初始化
            logger.info("Try initializing from [environment variable]");
            dataAccessProvider = JadeConfigResolver.configResolve(new JadeEnvConfig());
            if (dataAccessProvider != null) {
                logger.info("Jade initialized from [environment variable]");
            }
        }

        if (dataAccessProvider == null) {
            // 从  applicationContext.xml 配置的属性初始化
            logger.info("Try initializing from [applicationContext-*.xml]");
            dataAccessProvider = JadeConfigResolver.configResolve(// NL
                    new JadeAppConfig(applicationContext));
            if (dataAccessProvider != null) {
                logger.info("Jade initialized from [applicationContext-*.xml]");
            }
        }

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
