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
import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.core.SQLThreadLocal;
import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.Modifier;
import net.paoding.rose.jade.provider.SQLInterpreter;
import net.paoding.rose.jade.provider.SQLInterpreterResult;
import net.paoding.rose.jade.provider.jdbctemplate.plugin.EmptyDBMonitorPlugin;
import net.paoding.rose.jade.provider.jdbctemplate.plugin.IDBMonitorPlugin;

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

	private IDBMonitorPlugin plugin = new EmptyDBMonitorPlugin();

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

	public void setDBMonitorPlugin(IDBMonitorPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<?> select(final String sql, Modifier modifier,
			Map<String, Object> parameters, RowMapper rowMapper) {
		String sqlString = sql;
		Object[] arrayParameters = null;
		SQLInterpreterResult ir = null;
		for (SQLInterpreter interpreter : interpreters) {
			ir = interpreter.interpret(jdbcTemplate.getDataSource(), sqlString,
					modifier, parameters, arrayParameters);
			if (ir != null) {
				sqlString = ir.getSQL();
				arrayParameters = ir.getParameters();
			}
		}
		plugin.initData(jdbcTemplate.getDataSource(),
				sqlString, modifier, parameters);
		List<?> rt = selectByJdbcTemplate(sqlString, arrayParameters, rowMapper);
		plugin.listen();
		return rt;
	}

	@Override
	public int update(final String sql, Modifier modifier,
			Map<String, Object> parameters) {
		String sqlString = sql;
		Object[] arrayParameters = null;
		SQLInterpreterResult ir = null;
		for (SQLInterpreter interpreter : interpreters) {
			ir = interpreter.interpret(jdbcTemplate.getDataSource(), sqlString,
					modifier, parameters, arrayParameters);
			if (ir != null) {
				sqlString = ir.getSQL();
				arrayParameters = ir.getParameters();
			}
		}
		plugin.initData(jdbcTemplate.getDataSource(),
				sqlString, modifier, parameters);
		int rt = updateByJdbcTemplate(sqlString, arrayParameters);
		plugin.listen();
		return rt;
	}

	@Override
	public Number insertReturnId(final String sql, Modifier modifier,
			Map<String, Object> parameters) {
		String sqlString = sql;
		Object[] arrayParameters = null;
		SQLInterpreterResult ir = null;
		for (SQLInterpreter interpreter : interpreters) {
			ir = interpreter.interpret(jdbcTemplate.getDataSource(), sqlString,
					modifier, parameters, arrayParameters);
			if (ir != null) {
				sqlString = ir.getSQL();
				arrayParameters = ir.getParameters();
			}
		}
		plugin.initData(jdbcTemplate.getDataSource(),
				sqlString, modifier, parameters);
		Number rt = insertReturnIdByJdbcTemplate(sqlString, arrayParameters);
		plugin.listen();
		return rt;
	}

	/**
	 * 执行 SELECT 语句。
	 * 
	 * @param sql
	 *            - 执行的语句
	 * @param parameters
	 *            - 参数
	 * @param rowMapper
	 *            - 对象映射方式
	 * 
	 * @return 返回的对象列表
	 */
	protected List<?> selectByJdbcTemplate(String sql, Object[] parameters,
			RowMapper rowMapper) {

		if (parameters != null && parameters.length > 0) {

			return jdbcTemplate.query(sql, parameters, rowMapper);

		} else {

			return jdbcTemplate.query(sql, rowMapper);
		}
	}

	/**
	 * 执行 UPDATE / DELETE 语句。
	 * 
	 * @param sql
	 *            - 执行的语句
	 * @param parameters
	 *            - 参数
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

	public int[] batchUpdate(String sql, Modifier modifier,
			List<Map<String, Object>> parametersList) {
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

	/**
	 * 执行 UPDATE / DELETE 语句。
	 * 
	 * @param sql
	 *            - 执行的语句
	 * @param parameters
	 *            - 参数
	 * 
	 * @return 更新的记录数目
	 */
	protected void batchUpdateByJdbcTemplate(Map<String, List<Object[]>> ps,
			int[] updated, Map<String, int[]> positions) {
		for (Map.Entry<String, List<Object[]>> batch : ps.entrySet()) {
			String sql = batch.getKey();
			final List<Object[]> parametersList = batch.getValue();
			int[] subUpdated = jdbcTemplate.batchUpdate(sql,
					new BatchPreparedStatementSetter() {

						@Override
						public int getBatchSize() {
							return parametersList.size();
						}

						@Override
						public void setValues(PreparedStatement ps, int i)
								throws SQLException {
							Object[] args = parametersList.get(i);
							for (int j = 0; j < args.length; j++) {
								Object arg = args[j];
								if (arg instanceof SqlParameterValue) {
									SqlParameterValue paramValue = (SqlParameterValue) arg;
									StatementCreatorUtils.setParameterValue(ps,
											j + 1, paramValue, paramValue
													.getValue());
								} else {
									StatementCreatorUtils.setParameterValue(ps,
											j + 1, SqlTypeValue.TYPE_UNKNOWN,
											arg);
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

	/**
	 * 执行 INSERT 语句，并返回插入对象的 ID.
	 * 
	 * @param sql
	 *            - 执行的语句
	 * @param parameters
	 *            - 参数
	 * 
	 * @return 插入对象的 ID
	 */
	protected Number insertReturnIdByJdbcTemplate(String sql,
			Object[] parameters) {

		if (parameters != null && parameters.length > 0) {

			PreparedStatementCallbackReturnId callbackReturnId = new PreparedStatementCallbackReturnId(
					new ArgPreparedStatementSetter(parameters));

			return (Number) jdbcTemplate.execute(
					new GenerateKeysPreparedStatementCreator(sql),
					callbackReturnId);

		} else {

			PreparedStatementCallbackReturnId callbackReturnId = new PreparedStatementCallbackReturnId();

			return (Number) jdbcTemplate.execute(
					new GenerateKeysPreparedStatementCreator(sql),
					callbackReturnId);
		}
	}

	// 创建 PreparedStatement 时指定 Statement.RETURN_GENERATED_KEYS 属性
	private static class GenerateKeysPreparedStatementCreator implements
			PreparedStatementCreator, SqlProvider {

		private final String sql;

		public GenerateKeysPreparedStatementCreator(String sql) {
			Assert.notNull(sql, "SQL must not be null");
			this.sql = sql;
		}

		public PreparedStatement createPreparedStatement(Connection con)
				throws SQLException {
			return con.prepareStatement(this.sql,
					Statement.RETURN_GENERATED_KEYS);
		}

		public String getSql() {
			return this.sql;
		}
	}
}
