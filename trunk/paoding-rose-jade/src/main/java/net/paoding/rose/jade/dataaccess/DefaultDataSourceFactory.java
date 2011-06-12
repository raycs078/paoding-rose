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
package net.paoding.rose.jade.dataaccess;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import net.paoding.rose.jade.statement.StatementMetaData;

public class DefaultDataSourceFactory implements DataSourceFactory {

    private ConcurrentHashMap<String, DataSource> dataSources = new ConcurrentHashMap<String, DataSource>();

    private DataSource defaultDataSource;

    public DefaultDataSourceFactory() {
    }

    public DefaultDataSourceFactory(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public void registerDataSource(String name, DataSource dataSource) {
        if (name == null || name.length() == 0 || name.equals("*")) {
            defaultDataSource = dataSource;
        } else {
            dataSources.putIfAbsent(name, dataSource);
        }
    }

    @Override
    public DataSource getDataSource(StatementMetaData metaData,
            Map<String, Object> runtimeProperties) {
        String daoName = metaData.getDAOMetaData().getDAOClass().getName();
        String name = daoName;
        DataSource dataSource = dataSources.get(name);
        if (dataSource != null) {
            return dataSource;
        }
        while (true) {
            int index = name.lastIndexOf('.');
            if (index == -1) {
                dataSources.putIfAbsent(daoName, defaultDataSource);
                return defaultDataSource;
            }
            name = name.substring(0, index);
            dataSource = dataSources.get(name);
            if (dataSource != null) {
                dataSources.putIfAbsent(daoName, dataSource);
                return dataSource;
            }
        }
    }
}
