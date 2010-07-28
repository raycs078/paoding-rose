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

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.core.SQLThreadLocal;
import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.Modifier;
import net.paoding.rose.jade.provider.SQLInterpreter;
import net.paoding.rose.jade.provider.SQLInterpreterResult;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public class JdbcDataAccess implements DataAccess {

    private SQLInterpreter[] interpreters;

    private final Jdbc sysjdbc;

    private Jdbc jdbc;

    private final DataSource dataSource;

    protected JdbcDataAccess(Jdbc jdbc, DataSource dataSource) {
        this.sysjdbc = jdbc;
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    public void setInterpreters(SQLInterpreter[] interpreters) {
        Assert.isTrue(interpreters.length > 0);
        this.interpreters = interpreters;
    }

    public void setJdbcWrappers(JdbcWrapper[] jdbcWrappers) {
        for (int i = 1; i < jdbcWrappers.length; i++) {
            JdbcWrapper pre = jdbcWrappers[i - 1];
            pre.setJdbc(jdbcWrappers[i]);
        }
        if (jdbcWrappers.length > 0) {
            jdbcWrappers[jdbcWrappers.length - 1].setJdbc(sysjdbc);
            this.jdbc = jdbcWrappers[0];
        }
    }

    // ------------------------------------------------

    @Override
    public List<?> select(String jadeSQL, Modifier modifier, Map<String, Object> parametersAsMap,
            RowMapper rowMapper) {
        SQLInterpreterResult result = interpret(jadeSQL, modifier, parametersAsMap);
        return jdbc.query(modifier, result.getSQL(), result.getParameters(), rowMapper);
    }

    @Override
    public int update(String jadeSQL, Modifier modifier, Map<String, Object> parametersAsMap) {
        SQLInterpreterResult result = interpret(jadeSQL, modifier, parametersAsMap);
        return jdbc.update(modifier, result.getSQL(), result.getParameters());
    }

    @Override
    public Number insertReturnId(String jadeSQL, Modifier modifier,
            Map<String, Object> parametersAsMap) {
        SQLInterpreterResult result = interpret(jadeSQL, modifier, parametersAsMap);
        return jdbc.insertAndReturnId(modifier, result.getSQL(), result.getParameters());
    }

    public int[] batchUpdate(String sql, Modifier modifier, List<Map<String, Object>> parametersList) {
        int[] updated = new int[parametersList.size()];
        for (int i = 0; i < updated.length; i++) {
            Map<String, Object> parameters = parametersList.get(i);
            SQLThreadLocal.set(SQLType.WRITE, sql, modifier, parameters);
            updated[i] = update(sql, modifier, parameters);
            SQLThreadLocal.remove();
        }
        return updated;

        /*
         * if (parametersList.size() == 0) { return new int[0]; }
         * HashMap<String, List<Object[]>> batches = new HashMap<String,
         * List<Object[]>>(); Map<String, int[]> positions = new HashMap<String,
         * int[]>(); for (int i = 0; i < parametersList.size(); i++) { Object[]
         * statemenetParameters = null; String sqlString = sql;
         * SQLInterpreterResult ir = null; for (SQLInterpreter interpreter :
         * interpreters) { ir =
         * interpreter.interpret(jdbcTemplate.getDataSource(), sql, modifier,
         * parametersList.get(i), statemenetParameters); if (ir != null) { // if
         * (sqlString != null && !sqlString.equals(ir.getSQL())) { // throw new
         * IllegalArgumentException("batchUpdate"); // } sqlString =
         * ir.getSQL(); statemenetParameters = ir.getParameters(); } } // if
         * (sqlString == null) { // sqlString = sql; // } List<Object[]>
         * batchParameters = batches.get(sqlString); if (batchParameters ==
         * null) { batchParameters = new
         * ArrayList<Object[]>(parametersList.size()); batches.put(sqlString,
         * batchParameters); } int[] subPositions = positions.get(sqlString); if
         * (subPositions == null) { subPositions = new int[parametersList.size()
         * + 1]; positions.put(sqlString, subPositions); }
         * subPositions[subPositions[parametersList.size()]] = i;
         * subPositions[parametersList.size()] =
         * subPositions[parametersList.size()] + 1;
         * batchParameters.add(statemenetParameters); } int[] updated = new
         * int[parametersList.size()]; batchUpdateByJdbcTemplate(batches,
         * updated, positions); return updated;
         */
    }

    // ------------------------------------------------

    /**
     * 执行 UPDATE / DELETE 语句。
     * 
     * @param sql - 执行的语句
     * @param parameters - 参数
     * 
     * @return 更新的记录数目
     */
    /*-   protected void xxxxbatchUpdateByJdbcTemplate(Map<String, List<Object[]>> ps, int[] updated,
               Map<String, int[]> positions) {
           for (Map.Entry<String, List<Object[]>> batch : ps.entrySet()) {
               String sql = batch.getKey();
               final List<Object[]> parametersList = batch.getValue();
               int[] subUpdated = jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {

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
                               StatementCreatorUtils.setParameterValue(ps, j + 1, paramValue,
                                       paramValue.getValue());
                           } else {
                               StatementCreatorUtils.setParameterValue(ps, j + 1,
                                       SqlTypeValue.TYPE_UNKNOWN, arg);
                           }
                       }
                   }
               });

               int[] subPositions = positions.get(sql);
               for (int i = 0; i < subUpdated.length; i++) {
                   updated[subPositions[i]] = subUpdated[i];
               }
           }

       }
    */
    protected SQLInterpreterResult interpret(String jadeSQL, Modifier modifier,
            Map<String, Object> parametersAsMap) {
        SQLInterpreterResult result = null;
        // 
        for (SQLInterpreter interpreter : interpreters) {
            String sql = (result == null) ? jadeSQL : result.getSQL();
            Object[] parameters = (result == null) ? null : result.getParameters();
            SQLInterpreterResult t = interpreter.interpret(dataSource, sql, modifier,
                    parametersAsMap, parameters);
            if (t != null) {
                result = t;
            }
        }
        Assert.notNull(result);
        //
        return result;
    }

}
