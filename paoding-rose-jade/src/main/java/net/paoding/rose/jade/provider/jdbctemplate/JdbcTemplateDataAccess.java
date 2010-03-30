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
import java.util.List;

import javax.sql.DataSource;

import net.paoding.rose.jade.provider.DataAccess;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.util.Assert;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public abstract class JdbcTemplateDataAccess implements DataAccess {

    private JdbcTemplate jdbcTemplate = new JdbcTemplate();

    protected JdbcTemplateDataAccess() {
    }

    protected JdbcTemplateDataAccess(DataSource dataSource) {
        jdbcTemplate.setDataSource(dataSource);
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate.setDataSource(dataSource);
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

        if (parameters.length > 0) {

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

        if (parameters.length > 0) {

            return jdbcTemplate.update(sql, parameters);

        } else {

            return jdbcTemplate.update(sql);
        }
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

        if (parameters.length > 0) {

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
