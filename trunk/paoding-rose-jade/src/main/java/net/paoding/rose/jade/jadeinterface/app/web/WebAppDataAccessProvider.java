package net.paoding.rose.jade.jadeinterface.app.web;

import javax.servlet.ServletContext;

import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * 实现 Spring-framework 的 {@link FactoryBean} 接口，以提供单例化的
 * {@link DataAccessProviderHolder} 对象。
 * 
 * @author han.liao
 */
public class WebAppDataAccessProvider implements FactoryBean, ApplicationContextAware {

    protected DataAccessProvider dataAccessProvider;

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Object getObject() throws Exception {

        // 检查是否初始化
        if (dataAccessProvider == null) {
            throw new IllegalStateException("Jade not initialized");
        }

        return dataAccessProvider;
    }

    @Override
    public Class<?> getObjectType() {
        return DataAccessProvider.class;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        // 从  Web.xml 配置的属性初始化
        if (applicationContext instanceof WebApplicationContext) {

            // 获取  WebApplicationContext / ServletContext
            WebApplicationContext webApplicationContext = (WebApplicationContext) applicationContext;
            ServletContext servletContext = webApplicationContext.getServletContext();

            // 创建  DataAccessProvider 实例
            WebAppJadeInitContext webAppJadeInitContext = new WebAppJadeInitContext(servletContext);
            dataAccessProvider = JadeInitializer.initialize(webAppJadeInitContext);
        }

        if (dataAccessProvider instanceof ApplicationContextAware) {
            // 向下转播  ApplicationContext 对象
            ((ApplicationContextAware) dataAccessProvider)
                    .setApplicationContext(applicationContext);
        }
    }
}
