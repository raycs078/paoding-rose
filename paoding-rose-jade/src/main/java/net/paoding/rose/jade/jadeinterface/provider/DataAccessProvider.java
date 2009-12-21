package net.paoding.rose.jade.jadeinterface.provider;

import org.springframework.transaction.PlatformTransactionManager;

/**
 * 定义: DataAccess 的供应者接口。
 * 
 * @author zhilang.wang, han.liao
 */
public interface DataAccessProvider {

    /**
     * 创建一个: {@link DataAccess} 对象。
     * 
     * @param dataSourceName - 数据源名称
     * 
     * @return DataAccess 对象
     */
    public DataAccess createDataAccess(String dataSourceName);

    /**
     * 创建一个: {@link PlatformTransactionManager} 对象。
     * 
     * @param dataSourceName - 数据源名称
     * 
     * @return PlatformTransactionManager 对象
     */
    public PlatformTransactionManager createTransactionManager(String dataSourceName);
}
