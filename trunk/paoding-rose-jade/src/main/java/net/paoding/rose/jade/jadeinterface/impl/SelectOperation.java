package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.paoding.rose.jade.jadeinterface.annotation.SQLParam;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.Modifier;

import org.apache.commons.lang.ClassUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.NumberUtils;

/**
 * 实现 SELECT 查询。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author han.liao
 */
public class SelectOperation implements JdbcOperation {

    private final String jdQL;

    private final SQLParam[] annotations;

    private final RowMapper rowMapper;

    private final Class<?> returnType;

    private final Modifier modifier;

    public SelectOperation(String jdQL, Modifier modifier, RowMapper rowMapper) {

        this.jdQL = jdQL;
        this.modifier = modifier;
        this.returnType = modifier.getReturnType();
        this.annotations = modifier.getParameterAnnotations(SQLParam.class);
        this.rowMapper = rowMapper;
    }

    @Override
    public Object execute(DataAccess dataAccess, Object[] args) {

        // 将参数放入  Map
        HashMap<String, Object> parameters = new HashMap<String, Object>(annotations.length);
        for (int i = 0; i < annotations.length; i++) {
            SQLParam annotation = annotations[i];
            if (annotation != null) {
                parameters.put(annotation.value(), args[i]);
            }
        }

        // 执行查询
        List<?> listResult = dataAccess.select(jdQL, modifier, // NL
                parameters, rowMapper);
        final int sizeResult = listResult.size();

        // 将 Result 转成方法的返回类型
        if (returnType.isAssignableFrom(List.class)) {

            // 返回  List 集合
            return listResult;

        } else if (returnType.isArray()) {

            // 返回数组
            Class<?> componentClazz = returnType.getComponentType();

            if (componentClazz.isPrimitive()) {

                // 返回 Primitive 类型数组
                Object array = Array.newInstance(returnType.getComponentType(), sizeResult);

                int index = 0;
                for (Object value : listResult) {
                    Array.set(array, index++, value);
                }

                return array;

            } else {
                // 非  Primitive 类型数组直接返回
                return listResult.toArray();
            }

        } else if (returnType == Map.class) {

            // 将返回的  KeyValuePair 转换成  Map 对象
            Map<Object, Object> map = new HashMap<Object, Object>();
            for (Object obj : listResult) {
                KeyValuePair pair = (KeyValuePair) obj;
                map.put(pair.getKey(), pair.getValue());
            }

            return map;

        } else if (returnType.isAssignableFrom(HashSet.class)) {

            // 返回  Set 集合
            return new HashSet<Object>(listResult);

        } else {

            if (sizeResult == 1) {
                // 返回单个  Bean 对象
                return listResult.get(0);

            } else if (sizeResult == 0) {

                // 返回  0 (Primitive Type) 或者  null.
                if (returnType.isPrimitive()) {
                    Class<?> wrapperType = ClassUtils.primitiveToWrapper(returnType);
                    if (wrapperType == Boolean.class) {
                        return Boolean.FALSE;
                    } else {
                        return NumberUtils.convertNumberToTargetClass( // NL
                                Integer.valueOf(0), wrapperType);
                    }
                }

                return null;

            } else {
                // IncorrectResultSizeDataAccessException
                throw new IncorrectResultSizeDataAccessException(modifier.toString(), 1, sizeResult);
            }
        }
    }
}
