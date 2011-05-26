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

import net.paoding.rose.jade.provider.Modifier;
import net.paoding.rose.jade.provider.SQLInterpreterResult;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
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
    public List<?> query(Modifier modifier, SQLInterpreterResult interpreted, RowMapper rowMapper) {
        PreparedStatementCreator csc = getPreparedStatementCreator(interpreted);
        ArgPreparedStatementSetter pss = getArgPreparedStatementSetter(interpreted);
        return (List<?>) spring.query(csc, pss, new RowMapperResultSetExtractor(rowMapper));
    }

    private ArgPreparedStatementSetter getArgPreparedStatementSetter(
            final SQLInterpreterResult interpreted) {
        ArgPreparedStatementSetter pss = null;
        if (interpreted.getParameters() != null && interpreted.getParameters().length > 0) {
            pss = new ArgPreparedStatementSetter(interpreted.getParameters());
        }
        return pss;
    }

    private PreparedStatementCreator getPreparedStatementCreator(
            final SQLInterpreterResult interpreted) {
        PreparedStatementCreator creator = new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                if (interpreted.getClientInfo() != null) {
                    con.setClientInfo(interpreted.getClientInfo());
                }
                return con.prepareStatement(interpreted.getSQL());
            }
        };
        return creator;
    }

    private PreparedStatementCreator getPreparedStatementCreatorReturnId(
            final SQLInterpreterResult interpreted) {
        PreparedStatementCreator creator = new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                if (interpreted.getClientInfo() != null) {
                    con.setClientInfo(interpreted.getClientInfo());
                }
                return con.prepareStatement(interpreted.getSQL(), Statement.RETURN_GENERATED_KEYS);
            }
        };
        return creator;
    }

    @Override
    public int update(Modifier modifier, SQLInterpreterResult interpreted) {
        PreparedStatementCreator psc = getPreparedStatementCreator(interpreted);
        final ArgPreparedStatementSetter pss = getArgPreparedStatementSetter(interpreted);
        // :-( 这个方法spring末有public：spring.update(psc, pss);
        return (Integer) spring.execute(psc, new PreparedStatementCallback() {

            public Object doInPreparedStatement(PreparedStatement ps) throws SQLException {
                try {
                    if (pss != null) {
                        pss.setValues(ps);
                    }
                    int rows = ps.executeUpdate();
                    return new Integer(rows);
                } finally {
                    if (pss instanceof ParameterDisposer) {
                        ((ParameterDisposer) pss).cleanupParameters();
                    }
                }
            }
        });

    }

    /**
     * 执行 INSERT 语句，并返回插入对象的 ID.
     * 
     * @param sql - 执行的语句
     * @param args - 参数
     * 
     * @return 插入对象的 ID
     */
    @SuppressWarnings("deprecation")
    @Override
    public Object insertAndReturnId(Modifier modifier, SQLInterpreterResult interpreted) {
        PreparedStatementCreator psc = getPreparedStatementCreatorReturnId(interpreted);
        ArgPreparedStatementSetter pss = getArgPreparedStatementSetter(interpreted);
        PreparedStatementCallback callbackReturnId = new PreparedStatementCallbackReturnId(pss,
                modifier.getReturnType());
        return spring.execute(psc, callbackReturnId);
    }

    @Override
    public int[] batchUpdate(Modifier modifier, List<SQLInterpreterResult> interpreteds)
            throws DataAccessException {
        throw new UnsupportedOperationException("unsupported temporary");
//        return spring.batchUpdate(sql, new BatchPreparedStatementSetter() {
//
//            @Override
//            public void setValues(PreparedStatement ps, int i) throws SQLException {
//                Object[] values = args.get(i);
//                for (int j = 0; j < values.length; j++) {
//                    Object arg = values[j];
//                    if (arg instanceof SqlParameterValue) {
//                        SqlParameterValue paramValue = (SqlParameterValue) arg;
//                        StatementCreatorUtils.setParameterValue(ps, j + 1, paramValue,
//                                paramValue.getValue());
//                    } else {
//                        StatementCreatorUtils.setParameterValue(ps, j + 1,
//                                SqlTypeValue.TYPE_UNKNOWN, arg);
//                    }
//                }
//            }
//
//            @Override
//            public int getBatchSize() {
//                return args.size();
//            }
//        });
    }

    //-----------------------------------------------------------------------------

}
