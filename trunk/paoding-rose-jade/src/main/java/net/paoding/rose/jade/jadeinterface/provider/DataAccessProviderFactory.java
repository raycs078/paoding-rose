package net.paoding.rose.jade.jadeinterface.provider;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 实现 Spring-framework 的 {@link FactoryBean} 接口，以提供单例化的
 * {@link DataAccessProviderHolder} 对象。
 * 
 * @author han.liao
 */
public class DataAccessProviderFactory implements FactoryBean, ApplicationContextAware {

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Object getObject() throws Exception {
        return DataAccessProviderHolder.getInstance();
    }

    @Override
    public Class<?> getObjectType() {
        return DataAccessProvider.class;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        // 向下转播  ApplicationContext 对象
        DataAccessProviderHolder.getInstance().setApplicationContext(applicationContext);
    }
}
