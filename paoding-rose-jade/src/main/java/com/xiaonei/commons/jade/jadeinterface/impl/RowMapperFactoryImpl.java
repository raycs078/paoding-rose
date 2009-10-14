package com.xiaonei.commons.jade.jadeinterface.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.ClassUtils;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class RowMapperFactoryImpl implements RowMapperFactory {

    public RowMapper getRowMapper(Class<?> daoClass, Method method, ResultSet resultSet,
            Class<?> rowType) throws SQLException {
        if (ClassUtils.isPrimitiveOrWrapper(rowType)) {
            SingleColumnRowMapper mapper = new SingleColumnRowMapper();
            mapper.setRequiredType(rowType);
            return mapper;
        }
        if (rowType == Map.class) {
            ColumnMapRowMapper mapper = new ColumnMapRowMapper();
            return mapper;
        }
        if (rowType.isArray()) {
            return new ArrayRowMapper(rowType.getComponentType());
        }
        if (rowType == List.class || rowType == Collection.class) {
            return new ListRowMapper(method);
        }
        if (rowType == Set.class) {
            return new ListRowMapper(method);
        }
        return new BeanPropertyRowMapper(rowType);
    }

    static class ArrayRowMapper implements RowMapper {

        private Class<?> componentType;

        public ArrayRowMapper(Class<?> returnType) {
            this.componentType = returnType.getComponentType();
        }

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            int columnSize = rs.getMetaData().getColumnCount();
            Object array = Array.newInstance(componentType, columnSize);
            for (int i = 0; i < columnSize; i++) {
                Array.set(array, i, JdbcUtils.getResultSetValue(rs, i));
            }
            return array;
        }
    }

    class ListRowMapper extends CollectionRowMapper {

        public ListRowMapper(Method method) {
            super(method);
        }

        @SuppressWarnings("unchecked")
        protected Collection createCollection(int columnSize) {
            return new ArrayList(columnSize);
        }
    }

    class SetRowMapper extends CollectionRowMapper {

        public SetRowMapper(Method method) {
            super(method);
        }

        @SuppressWarnings("unchecked")
        protected Collection createCollection(int columnSize) {
            return new HashSet(columnSize);
        }
    }

    abstract static class CollectionRowMapper implements RowMapper {

        private Class<?> elementType;

        public CollectionRowMapper(Method method) {
            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) returnType;
                Type[] typeArguments = type.getActualTypeArguments();
                for (Type typeArgument : typeArguments) {
                    elementType = (Class<?>) typeArgument;
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            int columnSize = rs.getMetaData().getColumnCount();
            Collection list = createCollection(columnSize);
            for (int i = 0; i < columnSize; i++) {
                list.add(JdbcUtils.getResultSetValue(rs, i, elementType));
            }
            return list;
        }

        @SuppressWarnings("unchecked")
        protected abstract Collection createCollection(int columnSize);
    }
}
