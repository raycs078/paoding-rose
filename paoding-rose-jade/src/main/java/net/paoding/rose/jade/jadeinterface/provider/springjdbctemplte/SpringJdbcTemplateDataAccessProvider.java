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
package net.paoding.rose.jade.jadeinterface.provider.springjdbctemplte;

import javax.sql.DataSource;

import net.paoding.rose.jade.jadeinterface.datasource.DataSourceFactory;
import net.paoding.rose.jade.jadeinterface.datasource.SpringDataSourceFactory;
import net.paoding.rose.jade.jadeinterface.provider.AbstractDataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 提供 SpringJdbcTemplate 实现的 {@link DataAccessProvider}.
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public class SpringJdbcTemplateDataAccessProvider extends AbstractDataAccessProvider implements
        ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected DataAccess createDataAccess(DataSource dataSource) {
        return new SpringJdbcTemplateDataAccess(dataSource);
    }

    @Override
    protected DataSourceFactory createDataSourceFactory() {

        // 创建  springDataSourceFactory
        SpringDataSourceFactory springDataSourceFactory = new SpringDataSourceFactory();
        springDataSourceFactory.setApplicationContext(applicationContext);

        return springDataSourceFactory;
    }
}
