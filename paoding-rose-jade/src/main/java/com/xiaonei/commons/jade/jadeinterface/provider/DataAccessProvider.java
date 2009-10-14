package com.xiaonei.commons.jade.jadeinterface.provider;

import javax.sql.DataSource;

/**
 * 定义: DataAccess 的供应者接口。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com], han.liao
 */
public interface DataAccessProvider {

    /**
     * 用数据源创建: DataAccess 对象。
     * 
     * @param dataSource - 数据源
     * 
     * @return DataAccess 对象
     */
    public DataAccess createDataAccess(DataSource dataSource);
}
