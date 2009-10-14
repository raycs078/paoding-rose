package com.xiaonei.commons.jade.jadeinterface.provider.springjdbctemplte;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.xiaonei.commons.jade.jadeinterface.provider.DataAccess;

public class SpringJdbcTemplateDataAccess implements DataAccess {

    private static final Pattern PATTERN = Pattern.compile("\\:([a-zA-Z0-9_]*)");

    private JdbcTemplate jdbcTemplate = new JdbcTemplate();

    public SpringJdbcTemplateDataAccess(DataSource dataSource) {
        jdbcTemplate.setDataSource(dataSource);
    }

    @Override
    public List<?> select(String sql, Map<String, ?> parameters, RowMapper rowMapper) {

        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        Matcher matcher = PATTERN.matcher(sql);

        if (matcher.find()) {

            final List<Object> values = new ArrayList<Object>();

            do {
                final String name = matcher.group(1).trim();

                values.add(parameters.get(name));

            } while (matcher.find());

            return jdbcTemplate.query(matcher.replaceAll("?"), values.toArray(), rowMapper);

        } else {

            return jdbcTemplate.query(sql, rowMapper);
        }
    }

    @Override
    public int update(String sql, Map<String, ?> parameters) {

        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        Matcher matcher = PATTERN.matcher(sql);

        if (matcher.find()) {

            final List<Object> values = new ArrayList<Object>();

            do {
                final String name = matcher.group(1).trim();

                values.add(parameters.get(name));

            } while (matcher.find());

            return jdbcTemplate.update(matcher.replaceAll("?"), values.toArray());

        } else {

            return jdbcTemplate.update(sql);
        }
    }

    @Override
    public Number insertReturnId(final String sql, final Map<String, ?> parameters) {

        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        Matcher matcher = PATTERN.matcher(sql);

        if (matcher.find()) {

            final List<Object> values = new ArrayList<Object>();

            do {
                final String name = matcher.group(1).trim();

                values.add(parameters.get(name));

            } while (matcher.find());

            PreparedStatementCallbackReturnId callbackReturnId = new PreparedStatementCallbackReturnId(
                    new ArgPreparedStatementSetter(values.toArray()));
            return (Number) jdbcTemplate.execute(matcher.replaceAll("?"), callbackReturnId);

        } else {

            PreparedStatementCallbackReturnId callbackReturnId = new PreparedStatementCallbackReturnId();
            return (Number) jdbcTemplate.execute(sql, callbackReturnId);
        }
    }
}
