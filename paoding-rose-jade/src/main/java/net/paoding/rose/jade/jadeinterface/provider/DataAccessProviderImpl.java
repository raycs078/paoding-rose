package net.paoding.rose.jade.jadeinterface.provider;

import javax.sql.DataSource;

import net.paoding.rose.jade.jadeinterface.datasource.DataSourceFactory;

import org.springframework.transaction.PlatformTransactionManager;

/**
 * 基本的 {@link DataAccessProvider} 实现, 子类可以实现以下两个抽象方法提供定制的
 * {@link DataAccess} 与 {@link DataSourceFactory} 实现。
 * 
 * <ul>
 * <li>
 * {@link DataAccessProviderImpl#createDataAccess(javax.sql.DataSource)}
 * <li> {@link DataAccessProviderImpl#createDataSourceFactory()}
 * </ul>
 * 
 * @author han.liao
 */
public abstract class DataAccessProviderImpl implements DataAccessProvider {

    protected DataSourceFactory dataSourceFactory;

    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public DataAccess createDataAccess(String dataSourceName) {

        if (dataSourceFactory == null) {
            dataSourceFactory = createDataSourceFactory();
        }

        DataSource dataSource = dataSourceFactory.getDataSource(dataSourceName);
        if (dataSource == null) {
            throw new NullPointerException("DataSource");
        }

        return createDataAccess(dataSource);
    }

    @Override
    public PlatformTransactionManager createTransactionManager(String dataSourceName) {

        if (dataSourceFactory == null) {
            dataSourceFactory = createDataSourceFactory();
        }

        DataSource dataSource = dataSourceFactory.getDataSource(dataSourceName);
        if (dataSource == null) {
            throw new NullPointerException("DataSource");
        }

        return createTransactionManager(dataSource);
    }

    /**
     * 重载方法创建自己的 {@link DataAccess} 实现。
     * 
     * @param dataSource - 数据源
     * 
     * @return {@link DataAccess} 实现
     */
    protected abstract DataAccess createDataAccess(DataSource dataSource);

    /**
     * 重载方法创建自己的 {@link PlatformTransactionManager} 实现。
     * 
     * @param dataSource - 数据源
     * 
     * @return {@link PlatformTransactionManager} 实现
     */
    protected abstract PlatformTransactionManager createTransactionManager(DataSource dataSource);

    /**
     * 重载方法创建自己的 {@link DataSourceFactory} 实现。
     * 
     * @return {@link DataSourceFactory} 实现
     */
    protected abstract DataSourceFactory createDataSourceFactory();
}
