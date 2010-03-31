/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.core;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.paoding.rose.jade.annotation.SQLParam;
import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.Modifier;

import org.apache.commons.lang.ClassUtils;
import org.springframework.util.NumberUtils;

/**
 * 实现 INSERT / UPDATE / DELETE / REPLACE等更新类型语句。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public class UpdateOperation implements JdbcOperation {

    private final String jdQL;

    private final SQLParam[] sqlParamAnnotations;

    private final Class<?> returnType;

    private final Modifier modifier;

    public UpdateOperation(String jdQL, Modifier modifier) {

        this.jdQL = jdQL;
        this.modifier = modifier;
        this.returnType = modifier.getReturnType();
        this.sqlParamAnnotations = modifier.getParameterAnnotations(SQLParam.class);
    }

    @Override
    public Modifier getModifier() {
        return modifier;
    }

    @Override
    public Object execute(DataAccess dataAccess, Map<String, Object> parameters) {
        if (parameters.get(":1") instanceof Collection<?>) {
            // 批量执行查询
            return executeBatch(dataAccess, parameters);
        } else {
            // 单个执行查询
            return executeSignle(dataAccess, parameters, returnType);
        }
    }

    private Object executeBatch(DataAccess dataAccess, Map<String, Object> parameters) {

        Class<?> batchReturnClazz = returnType;
        Class<?> returnClazz = batchReturnClazz;

        Collection<?> collection = (Collection<?>) parameters.get(":1");

        Object returnArray = null;
        boolean successful = true;
        int updated = 0;

        // 转换基本类型
        if (batchReturnClazz.isPrimitive()) {
            batchReturnClazz = ClassUtils.primitiveToWrapper(batchReturnClazz);
        }

        if (batchReturnClazz.isArray()) {
            // 返回数组
            returnClazz = batchReturnClazz.getComponentType();
            returnArray = Array.newInstance(batchReturnClazz.getComponentType(), collection.size());
        } else if (batchReturnClazz == Boolean.class) {
            // 返回成功与否
            returnClazz = Boolean.class;
        } else if ((batchReturnClazz == Integer.class) || (batchReturnClazz == Long.class)
                || Number.class.isAssignableFrom(batchReturnClazz)) {
            // 返回更新纪录数
            returnClazz = Integer.class;
        }

        int index = 0;

        // 批量执行查询
        for (Object arg : collection) {

            HashMap<String, Object> clone = new HashMap<String, Object>(parameters);

            // 更新执行参数
            clone.put(":1", arg);
            if (this.sqlParamAnnotations[0] != null) {
                clone.put(this.sqlParamAnnotations[0].value(), arg);
            }

            Object value = executeSignle(dataAccess, clone, returnClazz);

            if (batchReturnClazz.isArray()) {
                Array.set(returnArray, index, value);
            } else if (returnClazz == Boolean.class) {
                successful = successful && ((Boolean) value).booleanValue();
            } else if (returnClazz == Integer.class) {
                updated += ((Number) value).intValue();
            }

            index++;
        }

        // 转换返回值
        if (batchReturnClazz.isArray()) {
            return returnArray;
        } else if (batchReturnClazz == Boolean.class) {
            return Boolean.valueOf(successful);
        } else if (Number.class.isAssignableFrom(batchReturnClazz)) {
            return NumberUtils.convertNumberToTargetClass(Integer.valueOf(updated),
                    batchReturnClazz);
        }

        return null;
    }

    private Object executeSignle(DataAccess dataAccess, Map<String, Object> parameters,
            Class<?> returnType) {

        if (returnType == Identity.class) {

            // 执行 INSERT 查询
            Number number = dataAccess.insertReturnId(jdQL, modifier, parameters);

            // 将结果转成方法的返回类型
            return new Identity(number);

        } else {

            // 执行 UPDATE / DELETE 查询
            int updated = dataAccess.update(jdQL, modifier, parameters);

            // 转换基本类型
            if (returnType.isPrimitive()) {
                returnType = ClassUtils.primitiveToWrapper(returnType);
            }

            // 将结果转成方法的返回类型
            if (returnType == Boolean.class) {
                return Boolean.valueOf(updated > 0);
            } else if (returnType == Long.class) {
                return Long.valueOf(updated);
            } else if (returnType == Integer.class) {
                return Integer.valueOf(updated);
            } else if (Number.class.isAssignableFrom(returnType)) {
                return NumberUtils.convertNumberToTargetClass( // NL
                        Integer.valueOf(updated), returnType);
            }
        }

        return null; // 没有返回值
    }
}
