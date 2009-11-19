package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.Method;

/**
 * 定义创建: {@link JdbcOperation} 的工厂。
 * 
 * @author han.liao
 */
public interface JdbcOperationFactory {

    /**
     * 创建: {@link JdbcOperation} 对象。
     * 
     * @return
     */
    public JdbcOperation getJdbcOperation(Class<?> daoClass, Method method);
}
