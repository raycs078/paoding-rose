package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.paoding.rose.jade.jadeinterface.annotation.SQL;
import net.paoding.rose.jade.jadeinterface.annotation.SQLType;

import org.springframework.jdbc.core.RowMapper;

/**
 * 实现创建: {@link JdbcOperation} 的工厂。
 * 
 * @author han.liao
 */
public class JdbcOperationFactoryImpl implements JdbcOperationFactory {

    private static Pattern SELECT_PATTERN = Pattern
            .compile("^\\s*SELECT", Pattern.CASE_INSENSITIVE);

    private RowMapperFactory rowMapperFactory = new RowMapperFactoryImpl();

    @Override
    public JdbcOperation getJdbcOperation(Class<?> daoClass, Method method) {

        // 检查方法的  Annotation
        SQL sqlAnnotation = method.getAnnotation(SQL.class);
        if (sqlAnnotation == null) {
            throw new UnsupportedOperationException( // NL
                    "DAO method without @SQL annotated: " + // NL
                            daoClass.getName() + '#' + method.getName());
        }

        String jdQL = sqlAnnotation.value();
        SQLType sqlType = sqlAnnotation.type();
        if (sqlType == SQLType.AUTO_DETECT) {
            // 用正则表达式匹配  SELECT 语句
            if (SELECT_PATTERN.matcher(jdQL).find()) {
                sqlType = SQLType.SELECT;
            } else {
                sqlType = SQLType.UPDATE;
            }
        }

        if (SQLType.SELECT == sqlType) {
            // 获得  RowMapper
            RowMapper rowMapper = rowMapperFactory.getRowMapper(daoClass, method,
                    getRowType(method));
            // SELECT 查询
            return new SelectOperation(jdQL, daoClass, method, rowMapper);

        } else if (SQLType.UPDATE == sqlType) {
            // INSERT / UPDATE / DELETE 查询
            return new UpdateOperation(jdQL, daoClass, method);
        }
        // 抛出检查异常
        throw new AssertionError("Unknown SQL type: " + sqlType);
    }

    // 获得返回的集合元素类型
    private static Class<?> getRowType(Method method) {

        Class<?> returnClassType = method.getReturnType();
        Class<?> rowType = returnClassType;

        if (Collection.class.isAssignableFrom(returnClassType)) {

            // 仅支持  List / Collection / Set
            if ((returnClassType != List.class) && (returnClassType != Collection.class)
                    && (returnClassType != Set.class)) {
                throw new IllegalArgumentException("error collection type "
                        + returnClassType.getName() + "; only support List, Set, Collection");
            }
            // 获取集合元素类型
            Class<?>[] genericTypes = GenericUtils.getActualClass(method.getGenericReturnType());
            if (genericTypes.length < 1) {
                throw new IllegalArgumentException(method.getClass().getName() + '#'
                        + method.getName() + ": Collection<T> must be generic");
            }
            rowType = genericTypes[0];

        } else if (Map.class == returnClassType) {

            // 获取  Map<K, V> 值元素类型
            Class<?>[] genericTypes = GenericUtils.getActualClass(method.getGenericReturnType());
            if (genericTypes.length != 2) {
                throw new IllegalArgumentException(method.getClass().getName() + '#'
                        + method.getName() + ": Map<K, V> must be generic");
            }
            rowType = genericTypes[1]; // 取  V 类型

        } else if (returnClassType.isArray()) {

            // 数组类型, 支持多重数组
            rowType = returnClassType.getComponentType();
        }

        return rowType;
    }
}
