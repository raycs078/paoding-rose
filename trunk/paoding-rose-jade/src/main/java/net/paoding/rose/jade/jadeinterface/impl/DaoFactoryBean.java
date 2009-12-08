package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

import net.paoding.rose.jade.jadeinterface.annotation.Dao;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class DaoFactoryBean<T> implements FactoryBean, InitializingBean {

    private static JdbcOperationFactory jdbcOperationFactory = new JdbcOperationFactoryImpl();

    private ConcurrentHashMap<Method, JdbcOperation> jdbcOperations = new ConcurrentHashMap<Method, JdbcOperation>();

    private DataAccessProvider dataAccessProvider;

    private T dao;

    private Class<T> daoClass;

    public void setDaoClass(Class<T> daoClass) {
        this.daoClass = daoClass;
    }

    public void setDataAccessProvider(DataAccessProvider dataAccessProvider) {
        this.dataAccessProvider = dataAccessProvider;
    }

    @Override
    public T getObject() {
        return dao;
    }

    @Override
    public Class<T> getObjectType() {
        return daoClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() {
        this.dao = createDao(daoClass);
    }

    @SuppressWarnings("unchecked")
    private T createDao(final Class<T> daoClass) {

        if (!daoClass.isInterface()) {
            throw new IllegalArgumentException(daoClass.getName()
                    + ": daoClass should be a interface");
        }

        Dao dao = daoClass.getAnnotation(Dao.class);
        if (dao == null) {
            throw new IllegalArgumentException(daoClass.getName() // NL
                    + ": not @Dao annotated ");
        }

        final DataAccess dataAccess = dataAccessProvider.createDataAccess(dao.catalog());

        return (T) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(),
                new Class[] { daoClass }, new InvocationHandler() {

                    @Override
                    public String toString() {
                        return daoClass.getName() + "@"
                                + Integer.toHexString(System.identityHashCode(this));
                    }

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {

                        if (Object.class == method.getDeclaringClass()) {
                            return method.invoke(this, args);
                        }

                        JdbcOperation operation = jdbcOperations.get(method);
                        if (operation == null) {
                            operation = jdbcOperationFactory.getJdbcOperation(daoClass, method);
                            jdbcOperations.putIfAbsent(method, operation);
                        }

                        return operation.execute(dataAccess, args);
                    }
                });
    }
}
