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

import java.util.regex.Pattern;

import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.Modifier;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

/**
 * 实现创建: {@link JdbcOperation} 的工厂。
 * 
 * @author han.liao
 */
public class JdbcOperationFactoryImpl implements JdbcOperationFactory {

    private static Pattern[] SELECT_PATTERNS = new Pattern[] {
    //
            Pattern.compile("^\\s*SELECT\\s+", Pattern.CASE_INSENSITIVE), //
            Pattern.compile("^\\s*SHOW\\s+", Pattern.CASE_INSENSITIVE), //
            Pattern.compile("^\\s*DESC\\s+", Pattern.CASE_INSENSITIVE), //
            Pattern.compile("^\\s*DESCRIBE\\s+", Pattern.CASE_INSENSITIVE), //
    };

    private RowMapperFactory rowMapperFactory = new RowMapperFactoryImpl();

    @Override
    public JdbcOperation getJdbcOperation(DataAccess dataAccess, Modifier modifier) {

        // 检查方法的  Annotation
        SQL sql = modifier.getAnnotation(SQL.class);
        Assert.notNull(sql, "@SQL is required for method " + modifier);

        String sqlString = sql.value();
        SQLType sqlType = sql.type();
        if (sqlType == SQLType.AUTO_DETECT) {
            for (int i = 0; i < SELECT_PATTERNS.length; i++) {
                // 用正则表达式匹配  SELECT 语句
                if (SELECT_PATTERNS[i].matcher(sqlString).find()) {
                    sqlType = SQLType.READ;
                    break;
                }
            }
            if (sqlType == SQLType.AUTO_DETECT) {
                sqlType = SQLType.WRITE;
            }
        }

        //
        if (SQLType.READ == sqlType) {
            // 获得  RowMapper
            RowMapper rowMapper = rowMapperFactory.getRowMapper(modifier);
            // SELECT 查询
            return new SelectOperation(dataAccess, sqlString, modifier, rowMapper);

        } else if (SQLType.WRITE == sqlType) {
            // INSERT / UPDATE / DELETE 查询
            return new UpdateOperation(dataAccess, sqlString, modifier);
        }

        // 抛出检查异常
        throw new AssertionError("Unknown SQL type: " + sqlType);
    }
}
