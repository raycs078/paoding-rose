package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.paoding.rose.jade.jadeinterface.annotation.SQL;
import net.paoding.rose.jade.jadeinterface.annotation.SQLParam;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.Modifier;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class SelectOperation implements JdbcOperation {

    private static final SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter();

    private RowMapperFactory rowMapperFactory;

    public void setRowMapperFactory(RowMapperFactory rowMapperFactory) {
        this.rowMapperFactory = rowMapperFactory;
    }

    public RowMapperFactory getRowMapperFactory() {
        return rowMapperFactory;
    }

    @Override
    public Object execute(DataAccess dataAccess, Class<?> daoClass, Method method, Object[] args) {
        SQL sqlCommand = method.getAnnotation(SQL.class);
        // 将参数放入map中
        // Class<?>[] methodParameterTypes = method.getParameterTypes();
        Map<String, Object> parameters = new HashMap<String, Object>();
        // ArrayList<Object> sqlArgs = new ArrayList<Object>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            // Class<?> methodParameterType = methodParameterTypes[i];
            // boolean beanParam = true;
            // if (ClassUtils.isPrimitiveOrWrapper(methodParameterType)
            //     || java.util.Date.class.isAssignableFrom(methodParameterType)) {
            //     beanParam = false;
            // }
            Annotation[] annotations = parameterAnnotations[i];
            for (Annotation annotation : annotations) {
                if (annotation instanceof SQLParam) {
                    parameters.put(((SQLParam) annotation).value(), args[i]);
                    continue;
                }
            }
        }

        // 
        RowMapperDelegate rowMapper = new RowMapperDelegate(this.rowMapperFactory, daoClass, method);
        // 执行查询
        List<?> result = dataAccess.select(sqlCommand.value(), new Modifier(method), parameters,
                rowMapper);

        // 将result转成方法的返回类型
        Class<?> returnClassType = method.getReturnType();
        // list
        if (List.class == returnClassType || Collection.class == returnClassType) {
            return result;
        }
        // array
        else if (returnClassType.isArray()) {
            Object array = Array.newInstance(returnClassType, result.size());
            int index = 0;
            for (Object value : result) {
                Array.set(array, index++, value);
            }
            return array;
        }
        // set
        else if (Set.class == returnClassType) {
            @SuppressWarnings("unchecked")
            HashSet set = new HashSet(result);
            return set;
        }
        // element
        else {
            if (result.size() == 0) {
                if (returnClassType.isPrimitive()) {
                    return simpleTypeConverter.convertIfNecessary("0", returnClassType);
                } else {
                    return null;
                }
            }
            if (result.size() > 1) {
                throw new IncorrectResultSizeDataAccessException(daoClass.getName() + "#"
                        + method.getName(), 1, result.size());
            }
            return result.get(0);
        }
    }

}
