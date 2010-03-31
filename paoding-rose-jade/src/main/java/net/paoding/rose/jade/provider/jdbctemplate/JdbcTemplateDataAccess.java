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
package net.paoding.rose.jade.provider.jdbctemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.Modifier;
import net.paoding.rose.jade.provider.SQLInterpreter;
import net.paoding.rose.jade.provider.SQLInterpreterResult;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.util.Assert;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public class JdbcTemplateDataAccess implements DataAccess {

    private SQLInterpreter[] interpreters = new SQLInterpreter[0];

    private JdbcTemplate jdbcTemplate = new JdbcTemplate();

    protected JdbcTemplateDataAccess() {
    }

    protected JdbcTemplateDataAccess(DataSource dataSource) {
        jdbcTemplate.setDataSource(dataSource);
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate.setDataSource(dataSource);
    }

    public void setInterpreters(SQLInterpreter[] interpreters) {
        this.interpreters = interpreters;
    }

    @Override
    public List<?> select(final String sql, Modifier modifier, Map<String, Object> parameters,
            RowMapper rowMapper) {
        String sqlString = sql;
        Object[] arrayParameters = null;
        SQLInterpreterResult ir = null;
        for (SQLInterpreter interpreter : interpreters) {
            ir = interpreter.interpret(jdbcTemplate.getDataSource(), sql, modifier, parameters,
                    arrayParameters);
            if (ir != null) {
                sqlString = ir.getSQL();
                arrayParameters = ir.getParameters();
            }
        }
        return selectByJdbcTemplate(sqlString, arrayParameters, rowMapper);
    }

    @Override
    public int update(final String sql, Modifier modifier, Map<String, Object> parameters) {
        String sqlString = sql;
        Object[] arrayParameters = null;
        SQLInterpreterResult ir = null;
        for (SQLInterpreter interpreter : interpreters) {
            ir = interpreter.interpret(jdbcTemplate.getDataSource(), sql, modifier, parameters,
                    arrayParameters);
            if (ir != null) {
                sqlString = ir.getSQL();
                arrayParameters = ir.getParameters();
            }
        }
        return updateByJdbcTemplate(sqlString, arrayParameters);
    }

    @Override
    public Number insertReturnId(final String sql, Modifier modifier, Map<String, Object> parameters) {
        String sqlString = sql;
        Object[] arrayParameters = null;
        SQLInterpreterResult ir = null;
        for (SQLInterpreter interpreter : interpreters) {
            ir = interpreter.interpret(jdbcTemplate.getDataSource(), sqlString, modifier,
                    parameters, arrayParameters);
            if (ir != null) {
                sqlString = ir.getSQL();
                arrayParameters = ir.getParameters();
            }
        }
        return insertReturnIdByJdbcTemplate(sqlString, arrayParameters);
    }

    /**
     * 执行 SELECT 语句。
     * 
     * @param sql - 执行的语句
     * @param parameters - 参数
     * @param rowMapper - 对象映射方式
     * 
     * @return 返回的对象列表
     */
    protected List<?> selectByJdbcTemplate(String sql, Object[] parameters, RowMapper rowMapper) {

        if (parameters != null && parameters.length > 0) {

            return jdbcTemplate.query(sql, parameters, rowMapper);

        } else {

            return jdbcTemplate.query(sql, rowMapper);
        }
    }

    /**
     * 执行 UPDATE / DELETE 语句。
     * 
     * @param sql - 执行的语句
     * @param parameters - 参数
     * 
     * @return 更新的记录数目
     */
    protected int updateByJdbcTemplate(String sql, Object[] parameters) {

        if (parameters != null && parameters.length > 0) {

            return jdbcTemplate.update(sql, parameters);

        } else {

            return jdbcTemplate.update(sql);
        }
    }

    public int[] batchUpdate(String sql, Modifier modifier, List<Map<String, Object>> parametersList) {
        if (parametersList.size() == 0) {
            return new int[0];
        }
        List<Object[]> a = new ArrayList<Object[]>(parametersList.size());
        String sqlString = null;
        for (int i = 0; i < a.size(); i++) {
            Object[] arrayParameters = null;
            SQLInterpreterResult ir = null;
            for (SQLInterpreter interpreter : interpreters) {
                ir = interpreter.interpret(jdbcTemplate.getDataSource(), sql, modifier,
                        parametersList.get(i), arrayParameters);
                if (ir != null) {
                    if (sqlString != null && !sqlString.equals(ir.getSQL())) {
                        throw new IllegalArgumentException("batchUpdate");
                    }
                    sqlString = ir.getSQL();
                    arrayParameters = ir.getParameters();
                }
            }
            a.add(arrayParameters);
        }
        if (sqlString == null) {
            sqlString = sql;
        }
        return batchUpdateByJdbcTemplate(sqlString, a);
    }

    /**
     * 执行 UPDATE / DELETE 语句。
     * 
     * @param sql - 执行的语句
     * @param parameters - 参数
     * 
     * @return 更新的记录数目
     */
    protected int[] batchUpdateByJdbcTemplate(String sql, final List<Object[]> parametersList) {
        if (parametersList == null || parametersList.size() == 0) {
            return new int[0];
        }
        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public int getBatchSize() {
                return parametersList.size();
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] args = parametersList.get(i);
                for (int j = 0; j < args.length; j++) {
                    Object arg = args[j];
                    if (arg instanceof SqlParameterValue) {
                        SqlParameterValue paramValue = (SqlParameterValue) arg;
                        StatementCreatorUtils.setParameterValue(ps, j + 1, paramValue, paramValue
                                .getValue());
                    } else {
                        StatementCreatorUtils.setParameterValue(ps, j + 1,
                                SqlTypeValue.TYPE_UNKNOWN, arg);
                    }
                }
            }
        });

    }

    /**
     * 执行 INSERT 语句，并返回插入对象的 ID.
     * 
     * @param sql - 执行的语句
     * @param parameters - 参数
     * 
     * @return 插入对象的 ID
     */
    protected Number insertReturnIdByJdbcTemplate(String sql, Object[] parameters) {

        if (parameters != null && parameters.length > 0) {

            PreparedStatementCallbackReturnId callbackReturnId = new PreparedStatementCallbackReturnId(
                    new ArgPreparedStatementSetter(parameters));

            return (Number) jdbcTemplate.execute(new GenerateKeysPreparedStatementCreator(sql),
                    callbackReturnId);

        } else {

            PreparedStatementCallbackReturnId callbackReturnId = new PreparedStatementCallbackReturnId();

            return (Number) jdbcTemplate.execute(new GenerateKeysPreparedStatementCreator(sql),
                    callbackReturnId);
        }
    }

    // 创建  PreparedStatement 时指定  Statement.RETURN_GENERATED_KEYS 属性
    private static class GenerateKeysPreparedStatementCreator implements PreparedStatementCreator,
            SqlProvider {

        private final String sql;

        public GenerateKeysPreparedStatementCreator(String sql) {
            Assert.notNull(sql, "SQL must not be null");
            this.sql = sql;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            return con.prepareStatement(this.sql, Statement.RETURN_GENERATED_KEYS);
        }

        public String getSql() {
            return this.sql;
        }
    }
}
