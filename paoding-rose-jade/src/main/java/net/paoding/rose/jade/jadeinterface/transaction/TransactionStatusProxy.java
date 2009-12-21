package net.paoding.rose.jade.jadeinterface.transaction;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * 提供事务状态的代理。
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public class TransactionStatusProxy implements TransactionStatus {

    private PlatformTransactionManager manager;

    private TransactionStatus status;

    public TransactionStatusProxy(PlatformTransactionManager manager, // NL
            TransactionStatus status) {
        this.manager = manager;
        this.status = status;
    }

    public PlatformTransactionManager getPlatformTransactionManager() {
        return manager;
    }

    public TransactionStatus getTransactionStatus() {
        return status;
    }

    @Override
    public boolean hasSavepoint() {
        return status.hasSavepoint();
    }

    @Override
    public boolean isCompleted() {
        return status.isCompleted();
    }

    @Override
    public boolean isNewTransaction() {
        return status.isNewTransaction();
    }

    @Override
    public boolean isRollbackOnly() {
        return status.isRollbackOnly();
    }

    @Override
    public void setRollbackOnly() {
        status.setRollbackOnly();
    }

    @Override
    public Object createSavepoint() throws TransactionException {
        return status.createSavepoint();
    }

    @Override
    public void releaseSavepoint(Object savepoint) throws TransactionException {
        status.releaseSavepoint(savepoint);
    }

    @Override
    public void rollbackToSavepoint(Object savepoint) throws TransactionException {
        status.rollbackToSavepoint(savepoint);
    }
}
