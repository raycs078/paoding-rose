package net.paoding.rose.jade.jadeinterface.provider.springjdbctemplte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.Modifier;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.util.Assert;

public class SpringJdbcTemplateDataAccess implements DataAccess {

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

    private static final Pattern PATTERN = Pattern.compile("\\:([a-zA-Z0-9_\\.]*)");

    private JdbcTemplate jdbcTemplate = new JdbcTemplate();

    public SpringJdbcTemplateDataAccess(DataSource dataSource) {
        jdbcTemplate.setDataSource(dataSource);
    }

    @Override
    public List<?> select(String sql, Modifier modifier, Map<String, ?> parameters,
            RowMapper rowMapper) {

        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        // 用 :name 参数的值填充列表
        final List<Object> values = new ArrayList<Object>();

        sql = processParameters(sql, parameters, values);

        if (!values.isEmpty()) {

            return jdbcTemplate.query(sql, values.toArray(), rowMapper);

        } else {

            return jdbcTemplate.query(sql, rowMapper);
        }
    }

    @Override
    public int update(String sql, Modifier modifier, Map<String, ?> parameters) {

        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        // 用 :name 参数的值填充列表
        final List<Object> values = new ArrayList<Object>();

        sql = processParameters(sql, parameters, values);

        if (!values.isEmpty()) {

            return jdbcTemplate.update(sql, values.toArray());

        } else {

            return jdbcTemplate.update(sql);
        }
    }

    @Override
    public Number insertReturnId(String sql, Modifier modifier, Map<String, ?> parameters) {

        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        // 用 :name 参数的值填充列表
        final List<Object> values = new ArrayList<Object>();

        sql = processParameters(sql, parameters, values);

        if (!values.isEmpty()) {

            PreparedStatementCallbackReturnId callbackReturnId = new PreparedStatementCallbackReturnId(
                    new ArgPreparedStatementSetter(values.toArray()));
            return (Number) jdbcTemplate.execute(new GenerateKeysPreparedStatementCreator(sql),
                    callbackReturnId);

        } else {

            PreparedStatementCallbackReturnId callbackReturnId = new PreparedStatementCallbackReturnId();
            return (Number) jdbcTemplate.execute(new GenerateKeysPreparedStatementCreator(sql),
                    callbackReturnId);
        }
    }

    /**
     * 查找 SQL 语句中所有的 :name, :name.property 参数, 将值写入列表, 并且返回包含 '?' 的 SQL 语句。
     * 
     * @param sql - SQL 语句
     * @param parameters - [in] 传入的参数
     * @param values - [out] 填充的参数列表
     * 
     * @return 包含 '?' 的 SQL 语句
     */
    protected String processParameters(String sql, Map<String, ?> parameters,
            final List<Object> values) {

        // 匹配符合  :name 格式的参数
        Matcher matcher = PATTERN.matcher(sql);
        if (matcher.find()) {

            do {
                // 提取参数名称
                final String name = matcher.group(1).trim();

                Object value = null;

                // 解析  a.b.c 类型的名称 
                int index = name.indexOf('.');
                if (index >= 0) {

                    // 用  BeanWrapper 获取属性值
                    Object bean = parameters.get(name.substring(0, index));
                    if (bean != null) {
                        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
                        value = beanWrapper.getPropertyValue(name.substring(index + 1));
                    }

                } else {
                    // 直接获取值
                    value = parameters.get(name);
                }

                values.add(value);

            } while (matcher.find());

            return matcher.replaceAll("?");
        }

        return sql;
    }
}
