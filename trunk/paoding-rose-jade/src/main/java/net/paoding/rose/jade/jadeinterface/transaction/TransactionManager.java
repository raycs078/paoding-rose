package net.paoding.rose.jade.jadeinterface.transaction;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * 提供简便的单数据源事务支持。
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public interface TransactionManager {

    /**
     * 使用默认的事务定义开始一个事务并返回 {@link TransactionStatus} 对象。
     * 
     * @param catalog - 数据源, 参考 {@link Catalogs}
     * 
     * @return 返回 {@link TransactionStatus} 对象
     */
    public TransactionStatus begin(String catalog);

    /**
     * 开始一个事务并返回 {@link TransactionStatus} 对象。
     * 
     * @param catalog - 数据源, 参考 {@link Catalogs}
     * @param definition - 事务定义
     * 
     * @return 返回 {@link TransactionStatus} 对象
     */
    public TransactionStatus begin(String catalog, TransactionDefinition definition);

    /**
     * 提交一个已经开始的事务。
     * 
     * @param status - 事务的 {@link TransactionStatus} 对象
     */
    public void commit(TransactionStatus status);

    /**
     * 回滚一个已经开始的事务。
     * 
     * @param status - 事务的 {@link TransactionStatus} 对象
     */
    public void rollback(TransactionStatus status);
}
