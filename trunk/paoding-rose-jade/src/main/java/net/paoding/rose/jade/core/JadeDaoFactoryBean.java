/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.DataAccessProvider;
import net.paoding.rose.jade.provider.Definition;
import net.paoding.rose.jade.provider.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

/**
 * 提供 DAO 对象的 Spring-framework {@link FactoryBean} 工厂。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 * 
 */
public class JadeDaoFactoryBean<T> implements FactoryBean, InitializingBean {

    private static final Log logger = LogFactory.getLog(JadeDaoFactoryBean.class);

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

        DAO dao = daoClass.getAnnotation(DAO.class);
        if (dao == null) {
            throw new IllegalArgumentException(daoClass.getName() // NL
                    + ": not @Dao annotated ");
        }

        final Definition definition = new Definition(daoClass);
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

                        if (logger.isDebugEnabled()) {
                            logger.debug("invoke: " + daoClass.getName() + "#" + method.getName());
                        }

                        if (Object.class == method.getDeclaringClass()) {
                            return method.invoke(this, args);
                        }

                        JdbcOperation operation = jdbcOperations.get(method);
                        if (operation == null) {
                            Modifier modifier = new Modifier(definition, method);
                            operation = jdbcOperationFactory.getJdbcOperation(modifier);
                            jdbcOperations.putIfAbsent(method, operation);
                        }
                        DataAccess dataAccess = dataAccessProvider.createDataAccess(daoClass);
                        return operation.execute(dataAccess, args);
                    }
                });
    }
}
