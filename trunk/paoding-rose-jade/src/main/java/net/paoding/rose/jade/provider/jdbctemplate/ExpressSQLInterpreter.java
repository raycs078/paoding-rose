package net.paoding.rose.jade.provider.jdbctemplate;

import java.sql.SQLSyntaxErrorException;
import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.exql.ExqlPattern;
import net.paoding.rose.jade.exql.impl.ExqlContextImpl;
import net.paoding.rose.jade.exql.impl.ExqlPatternImpl;
import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.SQLInterpreterResult;
import net.paoding.rose.jade.provider.SQLInterpreter;
import net.paoding.rose.jade.provider.Modifier;

import org.springframework.jdbc.BadSqlGrammarException;

/**
 * 提供动态: SQL 语句功能的 {@link DataAccess} 实现。
 * 
 * @author han.liao
 */
public class ExpressSQLInterpreter implements SQLInterpreter {

    @Override
    // 转换 JDQL 语句为正常的 SQL 语句
    public SQLInterpreterResult interpret(DataSource dataSource, String sql, Modifier modifier,
            Map<String, Object> parametersAsMap, Object[] parametersAsArray) {

        // 转换语句中的表达式
        ExqlPattern pattern = ExqlPatternImpl.compile(sql);
        ExqlContextImpl context = new ExqlContextImpl(sql.length() + 32);

        try {
            pattern.execute(context, parametersAsMap, modifier.getDefinition().getConstants());

        } catch (Exception e) {
            String daoInfo = modifier.toString();
            throw new BadSqlGrammarException(daoInfo, sql, new SQLSyntaxErrorException(daoInfo
                    + " @SQL('" + sql + "')", e));
        }

        return context;
    }

}
