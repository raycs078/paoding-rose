package net.paoding.rose.jade.jadeinterface.datasource;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * 从 {@link javax.servlet.ServletContext} 获取配置的
 * {@link javax.sql.DataSource} 数据源。
 * 
 * @author zhiliang.wang
 */
public class ServletContextDataSourceFactory implements DataSourceFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private SpringDataSourceFactory springDataSourceFactory;

    public static final String DATASOURCE_NAME_PREFIX = // NL
        ServletContextDataSourceFactory.class.getName() + ".";

    public void setSpringDataSourceFactory(SpringDataSourceFactory springDataSourceFactory) {
        this.springDataSourceFactory = springDataSourceFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public DataSource getDataSource(String dataSourceName) {
        if (this.applicationContext instanceof WebApplicationContext) {

            ServletContext servletContext = ((WebApplicationContext) applicationContext) // NL 
                    .getServletContext();
            return (DataSource) servletContext
                    .getAttribute(DATASOURCE_NAME_PREFIX + dataSourceName);

        } else {

            return springDataSourceFactory.getDataSource(dataSourceName);
        }
    }
}
