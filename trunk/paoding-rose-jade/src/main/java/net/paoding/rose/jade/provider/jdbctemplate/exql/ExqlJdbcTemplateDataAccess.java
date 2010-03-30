package net.paoding.rose.jade.jadeinterface.provider.jdbctemplate.exql;

import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.jadeinterface.exql.ExqlContext;
import net.paoding.rose.jade.jadeinterface.exql.ExqlPattern;
import net.paoding.rose.jade.jadeinterface.exql.impl.ExqlContextImpl;
import net.paoding.rose.jade.jadeinterface.exql.impl.ExqlPatternImpl;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.Modifier;
import net.paoding.rose.jade.jadeinterface.provider.jdbctemplate.JdbcTemplateDataAccess;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.RowMapper;

/**
 * 提供动态: SQL 语句功能的 {@link DataAccess} 实现。
 * 
 * @author han.liao
 */
public class ExqlJdbcTemplateDataAccess extends JdbcTemplateDataAccess {

    public ExqlJdbcTemplateDataAccess() {
        super();
    }

    public ExqlJdbcTemplateDataAccess(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<?> select(String jdQL, Modifier modifier, Map<String, ?> parameters,
            RowMapper rowMapper) {

        ExqlContext context = processConversion(jdQL, modifier, parameters);
        return selectByJdbcTemplate(context.flushOut(), context.getParams(), rowMapper);
    }

    @Override
    public int update(String jdQL, Modifier modifier, Map<String, ?> parameters) {

        ExqlContext context = processConversion(jdQL, modifier, parameters);
        return updateByJdbcTemplate(context.flushOut(), context.getParams());
    }

    @Override
    public Number insertReturnId(String jdQL, Modifier modifier, Map<String, ?> parameters) {

        ExqlContext context = processConversion(jdQL, modifier, parameters);
        return insertReturnIdByJdbcTemplate(context.flushOut(), context.getParams());
    }

    // 转换   JDQL 语句为正常的  SQL 语句
    protected ExqlContext processConversion(String jdQL, Modifier modifier,
            Map<String, ?> parameters) throws DataAccessException {

        if (jdQL == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        // 转换语句中的表达式
        ExqlPattern pattern = ExqlPatternImpl.compile(jdQL);
        ExqlContext context = new ExqlContextImpl();

        try {
            pattern.execute(context, parameters, modifier.getDefinition().getConstants());

        } catch (Exception e) {
            String daoInfo = modifier.toString();
            throw new BadSqlGrammarException(daoInfo, jdQL, new SQLSyntaxErrorException(daoInfo
                    + " @SQL('" + jdQL + "')", e));
        }

        return context;
    }
}
