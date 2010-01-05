package net.paoding.rose.jade.jadeinterface.app;

import net.paoding.rose.jade.jadeinterface.app.java.JadeAppConfig;
import net.paoding.rose.jade.jadeinterface.app.java.JadeSystemConfig;
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
            logger.info("Try initializing from [web.xml]");

            // 从  web.xml 配置的属性初始化
            JadeConfig config = new JadeWebAppConfig((WebApplicationContext) applicationContext);
            dataAccessProvider = JadeConfigResolver.configResolve(config);

            if (dataAccessProvider != null) {
                logger.info("Jade initialized from [web.xml]");
            }
        }

        if (dataAccessProvider == null) {
            logger.info("Try initializing from [jade.properties]");

            // 查找 jade.properties 配置
            Resource config = ResourceUtils.findResource(applicationContext, // NL
                    "jade.properties");

            // 从  jade.properties 配置的属性初始化
            if ((config != null) && config.exists()) {
                dataAccessProvider = JadeConfigResolver.configResolve(// NL
                        new JadePropConfig(config));
            } else {
                logger.info("Resource [" + config + "] not found");
            }

            if (dataAccessProvider != null) {
                logger.info("Jade initialized from [" + config.getURL() + "]");
            }
        }

        if (dataAccessProvider == null) {
            logger.info("Try initializing from [environment variable]");

            // 从环境变量配置的属性初始化
            dataAccessProvider = JadeConfigResolver.configResolve(new JadeSystemConfig());

            if (dataAccessProvider != null) {
                logger.info("Jade initialized from [environment variable]");
            }
        }

        if (dataAccessProvider == null) {

            logger.info("Try initializing from [applicationContext-*.xml]");

            // 从  applicationContext.xml 配置的属性初始化
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
