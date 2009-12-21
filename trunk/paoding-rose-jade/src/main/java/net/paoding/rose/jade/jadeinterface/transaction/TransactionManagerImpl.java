package net.paoding.rose.jade.jadeinterface.transaction;

import java.util.concurrent.ConcurrentHashMap;

import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 实现 {@link TransactionManager} 接口。
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public class TransactionManagerImpl implements TransactionManager, ApplicationContextAware {

    private TransactionDefinition definition = new DefaultTransactionDefinition();

    private ConcurrentHashMap<String, PlatformTransactionManager> managers = // NL
    new ConcurrentHashMap<String, PlatformTransactionManager>();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public TransactionStatus begin(String catalog) {

        PlatformTransactionManager manager = getPlatformTransactionManager(catalog);
        TransactionStatus status = manager.getTransaction(definition);
        return new TransactionStatusProxy(manager, status);
    }

    @Override
    public TransactionStatus begin(String catalog, TransactionDefinition definition) {

        PlatformTransactionManager manager = getPlatformTransactionManager(catalog);
        TransactionStatus status = manager.getTransaction(definition);
        return new TransactionStatusProxy(manager, status);
    }

    @Override
    public void commit(TransactionStatus status) {

        if (!(status instanceof TransactionStatusProxy)) {
            throw new IllegalArgumentException("Not TransactionStatusProxy: " + status);
        }

        TransactionStatusProxy proxy = (TransactionStatusProxy) status;
        PlatformTransactionManager manager = proxy.getPlatformTransactionManager();
        manager.commit(proxy.getTransactionStatus());
    }

    @Override
    public void rollback(TransactionStatus status) {

        if (!(status instanceof TransactionStatusProxy)) {
            throw new IllegalArgumentException("Not TransactionStatusProxy: " + status);
        }

        TransactionStatusProxy proxy = (TransactionStatusProxy) status;
        PlatformTransactionManager manager = proxy.getPlatformTransactionManager();
        manager.rollback(proxy.getTransactionStatus());
    }

    protected PlatformTransactionManager getPlatformTransactionManager(String dataSourceName) {

        PlatformTransactionManager manager = managers.get(dataSourceName);
        if (manager == null) {

            DataAccessProvider dataAccessProvider = (DataAccessProvider) applicationContext
                    .getBean("jadeDataAccessProviderHolder");
            manager = dataAccessProvider.createTransactionManager(dataSourceName);
            managers.putIfAbsent(dataSourceName, manager);
        }

        return manager;
    }
}
