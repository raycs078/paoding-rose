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

import net.paoding.rose.jade.cache.CacheProvider;
import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.DataAccessProvider;
import net.paoding.rose.jade.provider.cache.CacheDataAccess;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author 王志亮
 * 
 */
public class JadeDataAccessProvider implements DataAccessProvider {

    private DataAccessProvider targetAccessProvider;

    @Autowired(required = false)
    private CacheProvider cacheProvider;

    public void setTargetAccessProvider(DataAccessProvider targetAccessProvider) {
        this.targetAccessProvider = targetAccessProvider;
    }

    public void setCacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    @Override
    public DataAccess createDataAccess(Class<?> daoClass) {
        DataAccess dataAccess = targetAccessProvider.createDataAccess(daoClass);
        dataAccess = new SQLThreadLocalWrapper(dataAccess);
        if (cacheProvider != null) {
            dataAccess = new CacheDataAccess(dataAccess, cacheProvider);
        }
        return dataAccess;
    }

}
