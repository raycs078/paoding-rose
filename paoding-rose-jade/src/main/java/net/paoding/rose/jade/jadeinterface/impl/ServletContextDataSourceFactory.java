package net.paoding.rose.jade.jadeinterface.impl;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * 
 * @author zhiliang.wang
 */
public class ServletContextDataSourceFactory implements DataSourceFactory, ApplicationContextAware {

    private SpingDataSourceFactory springDataSourceFactory;

    private ApplicationContext applicationContext;

    public static final String DATASOURCE_NAME_PREFIX = ServletContextDataSourceFactory.class
            .getName()
            + ".";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setSpringDataSourceFactory(SpingDataSourceFactory springDataSourceFactory) {
        this.springDataSourceFactory = springDataSourceFactory;
    }

    @Override
    public DataSource getDataSource(String name) {
        if (this.applicationContext instanceof WebApplicationContext) {
            ServletContext servletContext = ((WebApplicationContext) applicationContext)
                    .getServletContext();
            return (DataSource) servletContext.getAttribute(DATASOURCE_NAME_PREFIX + name);
        } else {
            return springDataSourceFactory.getDataSource(name);
        }
    }
}
