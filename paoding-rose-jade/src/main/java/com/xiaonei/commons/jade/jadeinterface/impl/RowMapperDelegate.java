package com.xiaonei.commons.jade.jadeinterface.impl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.ClassUtils;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
class RowMapperDelegate implements RowMapper {

    private RowMapperFactory rowMapperFactory;

    private Class<?> daoClass;

    private Method method;

    private RowMapper rowMapper;

    private Class<?> rowType;

    public RowMapperDelegate(RowMapperFactory rowMapperFactory, Class<?> daoClass, Method method) {
        this.rowMapperFactory = rowMapperFactory;
        this.daoClass = daoClass;
        this.method = method;
    }

    public Class<?> getRowType() {
        if (rowMapper == null) {
            throw new IllegalStateException("");
        }
        return rowType;
    }

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (rowMapper == null) {
            rowMapper = rowMapperFactory.getRowMapper(daoClass, method, rs, getRowType(method
                    .getReturnType(), rs));
        }
        return rowMapper.mapRow(rs, rowNum);
    }

    private Class<?> getRowType(Class<?> returnClassType, ResultSet rs) throws SQLException {
        Class<?> rowType = returnClassType;
        if (Collection.class.isAssignableFrom(returnClassType)) {
            if (returnClassType != List.class && returnClassType != Collection.class
                    && returnClassType != Set.class) {
                throw new IllegalArgumentException("error collection type "
                        + returnClassType.getName() + "; only support List, Set, Collection");
            }
            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) returnType;
                Type[] typeArguments = type.getActualTypeArguments();
                for (Type typeArgument : typeArguments) {
                    rowType = (Class<?>) typeArgument;
                }
            }
        } else if (returnClassType.isArray()) {
            Class<?> componentType = returnClassType.getComponentType();
            if (componentType == String.class || ClassUtils.isPrimitiveOrWrapper(componentType)) {
                if (rs.getMetaData().getColumnCount() == 1) {
                    rowType = returnClassType;
                } else {
                    rowType = componentType;
                }
            } else {
                rowType = componentType;
            }
        }
        return this.rowType = rowType;
    }
}
