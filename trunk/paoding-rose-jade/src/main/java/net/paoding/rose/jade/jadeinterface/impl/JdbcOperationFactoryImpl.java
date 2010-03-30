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
package net.paoding.rose.jade.jadeinterface.impl;

import java.util.regex.Pattern;

import net.paoding.rose.jade.jadeinterface.annotation.SQL;
import net.paoding.rose.jade.jadeinterface.annotation.SQLType;
import net.paoding.rose.jade.jadeinterface.provider.Modifier;

import org.springframework.jdbc.core.RowMapper;

/**
 * 实现创建: {@link JdbcOperation} 的工厂。
 * 
 * @author han.liao
 */
public class JdbcOperationFactoryImpl implements JdbcOperationFactory {

    private static Pattern SELECT_PATTERN = Pattern.compile("^\\s*SELECT\\s+",
            Pattern.CASE_INSENSITIVE);

    private RowMapperFactory rowMapperFactory = new RowMapperFactoryImpl();

    @Override
    public JdbcOperation getJdbcOperation(Modifier modifier) {

        // 检查方法的  Annotation
        SQL sqlAnnotation = modifier.getAnnotation(SQL.class);
        if (sqlAnnotation == null) {
            throw new UnsupportedOperationException( // NL
                    "DAO method without @SQL annotated: " + modifier);
        }

        String jdQL = sqlAnnotation.value();
        SQLType sqlType = sqlAnnotation.type();
        if (sqlType == SQLType.AUTO_DETECT) {
            // 用正则表达式匹配  SELECT 语句
            if (SELECT_PATTERN.matcher(jdQL).find()) {
                sqlType = SQLType.READ;
            } else {
                sqlType = SQLType.WRITE;
            }
        }

        if (SQLType.READ == sqlType) {
            // 获得  RowMapper
            RowMapper rowMapper = rowMapperFactory.getRowMapper(modifier);
            // SELECT 查询
            return new SelectOperation(jdQL, modifier, rowMapper);

        } else if (SQLType.WRITE == sqlType) {
            // INSERT / UPDATE / DELETE 查询
            return new UpdateOperation(jdQL, modifier);
        }

        // 抛出检查异常
        throw new AssertionError("Unknown SQL type: " + sqlType);
    }
}
