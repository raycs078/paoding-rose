package net.paoding.rose.jade.datasource;

import javax.sql.DataSource;

/**
 * 定义创建 {@link javax.sql.DataSource} 的工厂。
 * 
 * @author han.liao
 */
public interface DataSourceFactory {

    /**
     * 获取指定名称的 {@link javax.sql.DataSource} 实例。
     * 
     * @param dataSourceName - 数据源名称
     * 
     * @return {@link javax.sql.DataSource} 实例
     */
    DataSource getDataSource(String dataSourceName);
}
