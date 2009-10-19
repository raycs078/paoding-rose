package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.paoding.rose.jade.jadeinterface.Identity;
import net.paoding.rose.jade.jadeinterface.annotation.SQL;
import net.paoding.rose.jade.jadeinterface.annotation.SQLParam;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;

import org.springframework.util.NumberUtils;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class UpdateOperation implements JdbcOperation {

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

        // 将参数放入 map 中
        Map<String, Object> parameters = new HashMap<String, Object>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            for (Annotation annotation : annotations) {
                if (annotation instanceof SQLParam) {
                    parameters.put(((SQLParam) annotation).value(), args[i]);
                    continue;
                }
            }
        }

        // 检查返回值类型
        Class<?> returnClassType = method.getReturnType();

        if (returnClassType == Identity.class) {

            // 执行 INSERT 查询
            Number number = dataAccess.insertReturnId(sqlCommand.value(), parameters);

            // 将结果转成方法的返回类型
            return new Identity(number);

        } else {

            // 执行 UPDATE / DELETE 查询
            int updated = dataAccess.update(sqlCommand.value(), parameters);

            // 将结果转成方法的返回类型
            if (returnClassType == boolean.class || returnClassType == Boolean.class) {
                return updated > 0;
            }

            if (returnClassType == int.class || returnClassType == Integer.class
                    || returnClassType == long.class || returnClassType == Long.class) {
                return updated;
            }

            if (Number.class.isAssignableFrom(returnClassType)) {
                return NumberUtils.convertNumberToTargetClass(updated, returnClassType);
            }
        }

        return null; // 没有返回值
    }
}
