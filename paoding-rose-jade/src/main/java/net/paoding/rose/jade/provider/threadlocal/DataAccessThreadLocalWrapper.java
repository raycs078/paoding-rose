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
package net.paoding.rose.jade.provider.threadlocal;

import java.util.List;
import java.util.Map;

import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.Modifier;

import org.springframework.jdbc.core.RowMapper;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 * 
 */

public class DataAccessThreadLocalWrapper implements DataAccess {

    private DataAccess dataAccess;

    @Override
    public List<?> select(String sql, Modifier modifier, Map<String, ?> parameters,
            RowMapper rowMapper) {
        JadeThreadLocal.set(SQLType.READ, sql, modifier, parameters);
        try {
            return dataAccess.select(sql, modifier, parameters, rowMapper);
        } finally {
            JadeThreadLocal.remove();
        }
    }

    @Override
    public int update(String sql, Modifier modifier, Map<String, ?> parameters) {
        JadeThreadLocal.set(SQLType.WRITE, sql, modifier, parameters);
        try {
            return dataAccess.update(sql, modifier, parameters);
        } finally {
            JadeThreadLocal.remove();
        }
    }

    @Override
    public Number insertReturnId(String sql, Modifier modifier, Map<String, ?> parameters) {
        JadeThreadLocal.set(SQLType.WRITE, sql, modifier, parameters);
        try {
            return dataAccess.insertReturnId(sql, modifier, parameters);
        } finally {
            JadeThreadLocal.remove();
        }
    }

}
