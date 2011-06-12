/*
 * Copyright 2009-2012 the original author or authors.
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
package net.paoding.rose.jade.context.spring;

import java.lang.reflect.Proxy;

import net.paoding.rose.jade.context.JadeInvocationHandler;
import net.paoding.rose.jade.dataaccess.DataAccessFactory;
import net.paoding.rose.jade.dataaccess.DefaultDataAccessFactory;
import net.paoding.rose.jade.dataaccess.DataSourceFactory;
import net.paoding.rose.jade.rowmapper.DefaultRowMapperFactory;
import net.paoding.rose.jade.rowmapper.RowMapperFactory;
import net.paoding.rose.jade.statement.DAOMetaData;
import net.paoding.rose.jade.statement.InterpreterFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 提供 DAO 对象的 Spring-framework {@link FactoryBean} 工厂。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 * 
 */
public class DAOFactoryBean implements FactoryBean, InitializingBean, ApplicationContextAware {

    private Object daoObject;

    private Class<?> daoClass;

    private DataAccessFactory dataAccessFactory;

    private DataSourceFactory dataSourceFactory;

    private RowMapperFactory rowMapperFactory;

    private InterpreterFactory interpreterFactory;

    private ApplicationContext applicationContext;

    public void setDAOClass(Class<?> daoClass) {
        this.daoClass = daoClass;
    }

    /**
     * 可选设置，如无设置则有默认设置
     * 
     * @param dataAccessFactory
     */
    public void setDataAccessFactory(DataAccessFactory dataAccessFactory) {
        this.dataAccessFactory = dataAccessFactory;
    }

    /**
     * 可选设置，如无设置则有默认设置
     * 
     * @param dataSourceFactory
     */
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataAccessFactory = null;
    }

    /**
     * 可选设置，如无设置则有默认设置
     * 
     * @param rowMapperFactory
     */
    public void setRowMapperFactory(RowMapperFactory rowMapperFactory) {
        this.rowMapperFactory = rowMapperFactory;
    }

    /**
     * 可选设置，如无设置则有默认设置
     * 
     * @param interpreterFactory
     */
    public void setInterpreterFactory(InterpreterFactory interpreterFactory) {
        this.interpreterFactory = interpreterFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.isTrue(daoClass.isInterface(), "not a interface class: " + daoClass.getName());
        if (dataAccessFactory == null) {
            if (dataSourceFactory == null) {
                dataSourceFactory = new LazySpringDataSourceFactory(applicationContext);
            }
            dataAccessFactory = new DefaultDataAccessFactory(dataSourceFactory);
        }
        if (rowMapperFactory == null) {
            rowMapperFactory = new DefaultRowMapperFactory();
        }
        if (interpreterFactory == null) {
            interpreterFactory = new SpringInterpreterFactory(this.applicationContext);
        }
    }

    @Override
    public Object getObject() {
        if (daoObject == null) {
            synchronized (this) {
                if (daoObject == null) {
                    daoObject = createDAO(daoClass);
                }
            }
        }
        Assert.notNull(daoObject);
        return daoObject;
    }

    protected Object createDAO(Class<?> daoClass) {
        DAOMetaData daoMetaData = new DAOMetaData(daoClass);
        JadeInvocationHandler handler = new JadeInvocationHandler(//
                daoMetaData, interpreterFactory, rowMapperFactory, dataAccessFactory);
        return Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), new Class[] { daoClass },
                handler);
    }

    @Override
    public Class<?> getObjectType() {
        return daoClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
