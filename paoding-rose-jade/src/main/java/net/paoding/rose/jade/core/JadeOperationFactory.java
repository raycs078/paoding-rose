package net.paoding.rose.jade.core;

import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.Modifier;

/**
 * 定义创建: {@link JadeOperation} 的工厂。
 * 
 * @author han.liao
 */
public interface JadeOperationFactory {

    /**
     * 创建: {@link JadeOperation} 对象。
     * 
     * @return {@link JadeOperation} 对象
     */
    public JadeOperation getOperation(DataAccess dataAccess, Modifier modifier);
}
