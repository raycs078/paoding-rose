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

/**
 * 
 * @author qieqie
 * 
 */
public class SingleDataSourceFactory implements DataSourceFactory {

    private DataSource dataSource;

    public SingleDataSourceFactory() {
    }

    public SingleDataSourceFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource(StatementMetaData metaData,
            Map<String, Object> runtimeProperties) {
        return dataSource;
    }

}
