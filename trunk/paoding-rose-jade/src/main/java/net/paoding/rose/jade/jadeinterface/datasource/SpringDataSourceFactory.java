package net.paoding.rose.jade.jadeinterface.datasource;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 获取用 Spring Framework 配置的 {@link javax.sql.DataSource} 数据源。
 * 
 * @author zhiliang.wang
 */
public class SpringDataSourceFactory implements DataSourceFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public DataSource getDataSource(String dataSourceName) {
        return (DataSource) applicationContext.getBean(dataSourceName + "DataSource",
                DataSource.class);
    }
}
