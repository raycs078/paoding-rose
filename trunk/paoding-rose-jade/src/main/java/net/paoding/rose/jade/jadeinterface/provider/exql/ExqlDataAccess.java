package net.paoding.rose.jade.jadeinterface.provider.exql;

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
import net.paoding.rose.jade.jadeinterface.provider.springjdbctemplte.SpringJdbcTemplateDataAccess;

import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.RowMapper;

/**
 * 提供动态: SQL 语句功能的 {@link DataAccess} 实现。
 * 
 * @author han.liao
 */
public class ExqlDataAccess extends SpringJdbcTemplateDataAccess {

    public ExqlDataAccess(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<?> select(String sql, Modifier modifier, Map<String, ?> parameters,
            RowMapper rowMapper) {

        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        // 转换语句中的表达式
        ExqlPattern pattern = ExqlPatternImpl.compile(sql);
        ExqlContext context = new ExqlContextImpl();

        try {
            sql = pattern.execute(context, parameters);
        } catch (Exception e) {
            throw new BadSqlGrammarException("ExqlPattern.execute", sql,
                    new SQLSyntaxErrorException("Error executing pattern", e));
        }

        return select(sql, context.getParams(), rowMapper);
    }

    @Override
    public int update(String sql, Modifier modifier, Map<String, ?> parameters) {

        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        // 转换语句中的表达式
        ExqlPattern pattern = ExqlPatternImpl.compile(sql);
        ExqlContext context = new ExqlContextImpl();

        try {
            sql = pattern.execute(context, parameters);
        } catch (Exception e) {
            throw new BadSqlGrammarException("ExqlPattern.execute", sql,
                    new SQLSyntaxErrorException("Error executing pattern", e));
        }

        return update(sql, context.getParams());
    }

    @Override
    public Number insertReturnId(String sql, Modifier modifier, Map<String, ?> parameters) {

        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        // 转换语句中的表达式
        ExqlPattern pattern = ExqlPatternImpl.compile(sql);
        ExqlContext context = new ExqlContextImpl();

        try {
            sql = pattern.execute(context, parameters);
        } catch (Exception e) {
            throw new BadSqlGrammarException("ExqlPattern.execute", sql,
                    new SQLSyntaxErrorException("Error executing pattern", e));
        }

        return insertReturnId(sql, context.getParams());
    }
}
