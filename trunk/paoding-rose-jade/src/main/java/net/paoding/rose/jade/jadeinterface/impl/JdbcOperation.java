package net.paoding.rose.jade.jadeinterface.impl;

import net.paoding.rose.jade.jadeinterface.provider.DataAccess;

/**
 * 定义一组数据库操作。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author han.liao
 */
public interface JdbcOperation {

    /**
     * 执行所需的数据库操作。
     * 
     * @return
     */
    public Object execute(DataAccess dataAccess, Object[] args);
}
