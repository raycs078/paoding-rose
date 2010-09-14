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
package net.paoding.rose.jade.provider.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import net.paoding.rose.jade.core.Identity;
import net.paoding.rose.jade.provider.Modifier;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.util.Assert;

/**
 * 
 * @author 王泰 [tai.wang@opi-corp.com]
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class JdbcImpl implements Jdbc {

    private final JdbcTemplate spring;

    public JdbcImpl(DataSource dataSource) {
        spring = new JdbcTemplate(dataSource);
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        spring.setDataSource(dataSource);
    }

    @Override
    public List<?> query(Modifier modifier, String sql, Object[] args, RowMapper rowMapper)
            throws DataAccessException {
        if (args != null && args.length > 0) {
            return spring.query(sql, args, rowMapper);
        } else {
            return spring.query(sql, rowMapper);
        }
    }

    @Override
    public int update(Modifier modifier, String sql, Object[] args) throws DataAccessException {
        if (args != null && args.length > 0) {
            return spring.update(sql, args);
        } else {
            return spring.update(sql);
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
    @Override
    public Number insertAndReturnId(Modifier modifier, String sql, Object[] parameters) {
        ArgPreparedStatementSetter setter = null;
        if (parameters != null && parameters.length > 0) {
            setter = new ArgPreparedStatementSetter(parameters);
        }
        Class<?> returnType = modifier.getReturnType();
        if (returnType == Identity.class) {
            returnType = Long.class;
        }
        PreparedStatementCallbackReturnId callbackReturnId = new PreparedStatementCallbackReturnId(
                setter, returnType);
        return (Number) spring.execute(new GenerateKeysPreparedStatementCreator(sql),
                callbackReturnId);
    }

    //-----------------------------------------------------------------------------

    // 创建 PreparedStatement 时指定 Statement.RETURN_GENERATED_KEYS 属性
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
