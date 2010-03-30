package net.paoding.rose.jade.jadeinterface.impl;

import net.paoding.rose.jade.jadeinterface.provider.Modifier;

/**
 * 定义创建: {@link JdbcOperation} 的工厂。
 * 
 * @author han.liao
 */
public interface JdbcOperationFactory {

    /**
     * 创建: {@link JdbcOperation} 对象。
     * 
     * @return {@link JdbcOperation} 对象
     */
    public JdbcOperation getJdbcOperation(Modifier modifier);
}
