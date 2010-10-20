package net.paoding.rose.jade.provider.jdbc;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.exql.ExqlPattern;
import net.paoding.rose.jade.exql.impl.ExqlContextImpl;
import net.paoding.rose.jade.exql.impl.ExqlPatternImpl;
import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.Modifier;
import net.paoding.rose.jade.provider.SQLInterpreter;
import net.paoding.rose.jade.provider.SQLInterpreterResult;

import org.springframework.jdbc.BadSqlGrammarException;

/**
 * 提供动态: SQL 语句功能的 {@link DataAccess} 实现。
 * 
 * @author 廖涵 [in355hz@gmail.com]
 */
public class SimpleSQLInterpreter implements SQLInterpreter {

    @Override
    // 转换 JadeSQL 语句为正常的 SQL 语句
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

    public static void main(String[] args) throws Exception {
        // 转换语句中的表达式
        String sql = "insert ignore into table_name "
                + "(`id`,`uid`,`favable_id`,`addtime`,`ranking`) "
                + "values (:1.id,:1.favableId,now(),0)";
        ExqlPattern pattern = ExqlPatternImpl.compile(sql);
        ExqlContextImpl context = new ExqlContextImpl(sql.length() + 32);

        Map parametersAsMap = new HashMap();
        parametersAsMap.put(":1", new ArrayList());
//        parametersAsMap.put(":2", 123);

        String result = pattern.execute(context, parametersAsMap);
        System.out.println(result);
    }

}
