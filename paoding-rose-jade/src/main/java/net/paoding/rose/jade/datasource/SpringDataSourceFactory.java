package net.paoding.rose.jade.datasource;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 获取用 Spring Framework 配置的 {@link javax.sql.DataSource} 数据源。
 * 
 * @author zhiliang.wang
 */
public class SpringDataSourceFactory implements DataSourceFactory, ApplicationContextAware {

    private Log logger = LogFactory.getLog(getClass());

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public DataSource getDataSource(String catalog) {
        assert catalog != null;
        catalog = catalog.trim();
        String tempCatalog = catalog;
        while (tempCatalog != null) {
            String key = "jade.dataSource." + tempCatalog;
            if (applicationContext.containsBean(key)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("found dataSource '" + key + "' for catalog '" + catalog + "'.");
                }
                return (DataSource) applicationContext.getBean(key, DataSource.class);
            }
            int index = tempCatalog.lastIndexOf('.');
            if (index == -1) {
                tempCatalog = null;
            } else {
                tempCatalog = tempCatalog.substring(0, index);
            }
        }
        String key = "jade.dataSource";
        if (applicationContext.containsBean(key)) {
            if (logger.isDebugEnabled()) {
                logger.debug("found dataSource '" + key + "' for catalog '" + catalog + "'.");
            }
            return (DataSource) applicationContext.getBean(key, DataSource.class);
        }
        key = "dataSource";
        if (applicationContext.containsBean(key)) {
            if (logger.isDebugEnabled()) {
                logger.debug("found dataSource '" + key + "' for catalog '" + catalog + "'.");
            }
            return (DataSource) applicationContext.getBean(key, DataSource.class);
        }
        throw new IllegalArgumentException("not found dataSource for catalog: '" + catalog
                + "'; you should set a dataSource bean"
                + " (with id='jade.dataSource[.daopackage[.daosimpleclassname]]' or 'dataSource' )"
                + "in applicationContext for this catalog.");
    }
}
