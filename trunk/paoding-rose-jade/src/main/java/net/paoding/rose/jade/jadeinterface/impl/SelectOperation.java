package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.paoding.rose.jade.jadeinterface.annotation.SQL;
import net.paoding.rose.jade.jadeinterface.annotation.SQLParam;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.Modifier;

import org.apache.commons.lang.ClassUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.util.NumberUtils;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class SelectOperation implements JdbcOperation {

    private RowMapperFactory mapperFactory;

    public void setRowMapperFactory(RowMapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
    }

    public RowMapperFactory getRowMapperFactory() {
        return mapperFactory;
    }

    @Override
    public Object execute(DataAccess dataAccess, Class<?> daoClass, Method method, Object[] args) {

        SQL sqlCommand = method.getAnnotation(SQL.class);

        // 将参数放入  Map
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Map<String, Object> parameters = new HashMap<String, Object>();

        for (int i = 0; i < parameterAnnotations.length; i++) {

            Annotation[] annotations = parameterAnnotations[i];
            for (Annotation annotation : annotations) {
                if (annotation instanceof SQLParam) {
                    parameters.put(((SQLParam) annotation).value(), args[i]);
                    continue;
                }
            }
        }

        // 获得  RowMapper 封装
        RowMapperDelegate mapperDelegate = new RowMapperDelegate(mapperFactory, // NL
                daoClass, method);

        // 执行查询
        List<?> listResult = dataAccess.select(sqlCommand.value(), new Modifier(method), // NL
                parameters, mapperDelegate);
        final int sizeResult = listResult.size();

        // 将 Result 转成方法的返回类型
        Class<?> returnClazz = method.getReturnType();

        if (returnClazz.isAssignableFrom(List.class)) {

            // 返回  List 集合
            return listResult;

        } else if (returnClazz.isArray()) {

            // 返回数组
            Class<?> componentClazz = returnClazz.getComponentType();

            if (componentClazz.isPrimitive()) {

                // 返回 Primitive 类型数组
                Object array = Array.newInstance(returnClazz.getComponentType(), sizeResult);

                int index = 0;
                for (Object value : listResult) {
                    Array.set(array, index++, value);
                }

                return array;

            } else {
                // 非  Primitive 类型数组直接返回
                return listResult.toArray();
            }

        } else if (returnClazz == Map.class) {

            HashMap<Object, Object> map = new HashMap<Object, Object>();
            for (Object value : listResult) {
                KeyValuePair pair = (KeyValuePair) value;
                map.put(pair.getKey(), pair.getValue());
            }
            return map;

        } else if (returnClazz.isAssignableFrom(HashSet.class)) {

            // 返回  Set 集合
            return new HashSet<Object>(listResult);

        } else {

            if (sizeResult == 1) {
                // 返回单个  Bean 对象
                return listResult.get(0);

            } else if (sizeResult == 0) {

                // 返回  0 (Primitive Type) 或者  null.
                if (returnClazz.isPrimitive()) {
                    if (returnClazz == boolean.class) {
                        return Boolean.FALSE;
                    }
                    return NumberUtils.convertNumberToTargetClass( // NL
                            Integer.valueOf(0), ClassUtils.primitiveToWrapper(returnClazz));
                }

                return null;

            } else {
                // IncorrectResultSizeDataAccessException
                throw new IncorrectResultSizeDataAccessException(daoClass.getName() + "#"
                        + method.getName(), 1, sizeResult);
            }
        }
    }
}
