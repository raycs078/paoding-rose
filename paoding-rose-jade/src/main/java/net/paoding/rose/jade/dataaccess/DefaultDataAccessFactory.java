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

import javax.sql.DataSource;

import net.paoding.rose.jade.statement.StatementMetaData;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 
 * @author qieqie
 * 
 */
public class DefaultDataAccessFactory implements DataAccessFactory {

    protected final DataSourceFactory dataSourceFactory;

    public DefaultDataAccessFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    @Override
    public DataAccess getDataAccess(StatementMetaData metaData,
            Map<String, Object> runtimeProperties) {
        DataSource dataSource = dataSourceFactory.getDataSource(metaData, runtimeProperties);
        if (dataSource == null) {
            throw new NullPointerException("not found dataSource for: " + metaData);
        }
        return new DataAccessImpl(new JdbcTemplate(dataSource));
    }

}
