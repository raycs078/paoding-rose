package net.paoding.rose.jade.jadeinterface.provider.springjdbctemplte;

import javax.sql.DataSource;

import net.paoding.rose.jade.jadeinterface.datasource.DataSourceFactory;
import net.paoding.rose.jade.jadeinterface.datasource.SpringDataSourceFactory;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProviderImpl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 提供 SpringJdbcTemplate 实现的 {@link DataAccessProvider}.
 * 
 * @author han.liao
 */
public class SpringJdbcTemplateDataAccessProvider extends DataAccessProviderImpl implements
        ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected DataAccess createDataAccess(DataSource dataSource) {
        return new SpringJdbcTemplateDataAccess(dataSource);
    }

    @Override
    protected PlatformTransactionManager createTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Override
    protected DataSourceFactory createDataSourceFactory() {

        // 创建  springDataSourceFactory
        SpringDataSourceFactory springDataSourceFactory = new SpringDataSourceFactory();
        springDataSourceFactory.setApplicationContext(applicationContext);

        return springDataSourceFactory;
    }
}
