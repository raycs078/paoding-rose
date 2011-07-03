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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.dataaccess.DataSourceFactory;
import net.paoding.rose.jade.statement.StatementMetaData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @author qieqie.wang
 */
public class SpringDataSourceFactory implements DataSourceFactory, ApplicationContextAware {

    private Log logger = LogFactory.getLog(getClass());

    private ListableBeanFactory applicationContext;

    private ConcurrentHashMap<Class<?>, DataSource> cached = new ConcurrentHashMap<Class<?>, DataSource>();

    public SpringDataSourceFactory() {
    }

    public SpringDataSourceFactory(ListableBeanFactory applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public DataSource getDataSource(StatementMetaData metaData,
            Map<String, Object> runtimeProperties) {
        Class<?> daoClass = metaData.getDAOMetaData().getDAOClass();
        DataSource dataSource = cached.get(daoClass);
        if (dataSource != null) {
            return dataSource;
        }

        dataSource = getDataSourceByDirectory(daoClass, daoClass.getName());
        if (dataSource != null) {
            cached.put(daoClass, dataSource);
            return dataSource;
        }
        String catalog = daoClass.getAnnotation(DAO.class).catalog();
        if (catalog.length() > 0) {
            dataSource = getDataSourceByDirectory(daoClass,
                    catalog + "." + daoClass.getSimpleName());
        }
        if (dataSource != null) {
            cached.put(daoClass, dataSource);
            return dataSource;
        }
        dataSource = getDataSourceByKey(daoClass, "jade.dataSource");
        if (dataSource != null) {
            cached.put(daoClass, dataSource);
            return dataSource;
        }
        dataSource = getDataSourceByKey(daoClass, "dataSource");
        if (dataSource != null) {
            cached.put(daoClass, dataSource);
            return dataSource;
        }
        return null;
    }

    private DataSource getDataSourceByDirectory(Class<?> daoClass, String catalog) {
        String tempCatalog = catalog;
        DataSource dataSource;
        while (tempCatalog != null && tempCatalog.length() > 0) {
            dataSource = getDataSourceByKey(daoClass, "jade.dataSource." + tempCatalog);
            if (dataSource != null) {
                return dataSource;
            }
            int index = tempCatalog.lastIndexOf('.');
            if (index == -1) {
                tempCatalog = null;
            } else {
                tempCatalog = tempCatalog.substring(0, index);
            }
        }
        return null;
    }

    private DataSource getDataSourceByKey(Class<?> daoClass, String key) {
        if (applicationContext.containsBean(key)) {
            DataSource dataSource = (DataSource) applicationContext.getBean(key, DataSource.class);
            if (logger.isDebugEnabled()) {
                logger.debug("found dataSource: " + key + " for DAO " + daoClass.getName());
            }
            return dataSource;
        }
        return null;
    }
}
