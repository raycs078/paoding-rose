package net.paoding.rose.jade.context.spring;

import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.dataaccess.DataSourceFactory;
import net.paoding.rose.jade.statement.StatementMetaData;

import org.springframework.beans.factory.ListableBeanFactory;

/**
 * 
 * @author qieqie
 * 
 */
public class SpringDataSourceFactoryDelegate implements DataSourceFactory {

    private ListableBeanFactory beanFactory;

    private DataSourceFactory dataSourceFactory;

    public SpringDataSourceFactoryDelegate(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public DataSource getDataSource(StatementMetaData metaData,
            Map<String, Object> runtimeProperties) {
        if (dataSourceFactory == null) {
            ListableBeanFactory beanFactory = this.beanFactory;
            if (beanFactory != null) {
                if (beanFactory.containsBeanDefinition("jade.dataSourceFactory")) {
                    dataSourceFactory = (DataSourceFactory) beanFactory.getBean(
                            "jade.dataSourceFactory", DataSourceFactory.class);
                } else {
                    @SuppressWarnings("rawtypes")
                    Map beansOfType = beanFactory.getBeansOfType(DataSourceFactory.class);
                    if (beansOfType.size() > 1) {
                        throw new IllegalStateException(
                                "requires 0 or 1 DataSourceFactory, but found "
                                        + beansOfType.size());
                    }
                    if (beansOfType.size() == 1) {
                        dataSourceFactory = (DataSourceFactory) beansOfType.values().iterator()
                                .next();
                    } else {
                        dataSourceFactory = new SpringDataSourceFactory(beanFactory);
                    }
                }
                this.beanFactory = null;
            }
        }
        return dataSourceFactory.getDataSource(metaData, runtimeProperties);
    }

}
