package net.paoding.rose.jade.jadeinterface.impl;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class SpingDataSourceFactory implements DataSourceFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public DataSource getDataSource(String name) {
        return (DataSource) applicationContext.getBean(name + "DataSource", DataSource.class);
    }
}
