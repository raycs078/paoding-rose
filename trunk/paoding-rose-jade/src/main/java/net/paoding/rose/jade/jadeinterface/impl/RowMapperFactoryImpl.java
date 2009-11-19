package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.paoding.rose.jade.jadeinterface.annotation.MapKey;

import org.apache.commons.lang.ClassUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class RowMapperFactoryImpl implements RowMapperFactory {

    @Override
    public RowMapper getRowMapper(Class<?> daoClass, Method method, Class<?> rowType) {

        // BUGFIX: SingleColumnRowMapper 处理  Primitive Type 抛异常
        if (rowType.isPrimitive()) {
            rowType = ClassUtils.primitiveToWrapper(rowType);
        }

        // 根据类型创建  RowMapper
        RowMapper rowMapper;
        if (ClassUtils.wrapperToPrimitive(rowType) != null) {
            SingleColumnRowMapper mapper = new SingleColumnRowMapper();
            mapper.setRequiredType(rowType);
            rowMapper = mapper;
        } else if (rowType == Map.class) {
            ColumnMapRowMapper mapper = new ColumnMapRowMapper();
            rowMapper = mapper;
        } else if (rowType.isArray()) {
            rowMapper = new ArrayRowMapper(rowType);
        } else if ((rowType == List.class) || (rowType == Collection.class)) {
            rowMapper = new ListRowMapper(method);
        } else if (rowType == Set.class) {
            rowMapper = new ListRowMapper(method);
        } else {
            rowMapper = new BeanPropertyRowMapper(rowType);
        }

        // 处理返回值是  Map 的情况
        if (method.getReturnType() == Map.class) {
            rowMapper = new KeyValuePairMapper(method, rowMapper);
        }

        return rowMapper;
    }

    // 用数组返回每一列
    protected static class ArrayRowMapper implements RowMapper {

        private Class<?> componentType;

        public ArrayRowMapper(Class<?> returnType) {
            this.componentType = returnType.getComponentType();
        }

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            int columnSize = rs.getMetaData().getColumnCount();
            Object array = Array.newInstance(componentType, columnSize);
            for (int i = 0; i < columnSize; i++) {
                Array.set(array, i, JdbcUtils.getResultSetValue(rs, i, componentType));
            }
            return array;
        }
    }

    // 用  Map<K, V> 返回每一列
    protected static class KeyValuePairMapper implements RowMapper {

        private final RowMapper mapper;

        private String keyColumn;

        private Class<?> keyType;

        public KeyValuePairMapper(Method method, RowMapper mapper) {
            MapKey mapKey = method.getAnnotation(MapKey.class);
            if (mapKey != null) {
                keyColumn = mapKey.value();
            } else {
                keyColumn = "id";
            }
            Class<?>[] genericTypes = GenericUtils.getActualClass(method.getGenericReturnType());
            if (genericTypes.length < 1) {
                throw new IllegalArgumentException("Map generic");
            }
            keyType = genericTypes[0];
            this.mapper = mapper;
        }

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            Object key = JdbcUtils.getResultSetValue(rs, rs.findColumn(keyColumn), keyType);
            Object value = mapper.mapRow(rs, rowNum);
            return new KeyValuePair(key, value);
        }
    }

    // 用  List<T> 返回每一列
    protected static class ListRowMapper extends CollectionRowMapper {

        public ListRowMapper(Method method) {
            super(method);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Collection createCollection(int columnSize) {
            return new ArrayList(columnSize);
        }
    }

    // 用  Set<T> 返回每一列
    protected static class SetRowMapper extends CollectionRowMapper {

        public SetRowMapper(Method method) {
            super(method);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Collection createCollection(int columnSize) {
            return new HashSet(columnSize);
        }
    }

    // 用  Collection<T> 返回每一列, 这是基类
    protected abstract static class CollectionRowMapper implements RowMapper {

        private Class<?> elementType;

        public CollectionRowMapper(Method method) {
            Class<?>[] genericTypes = GenericUtils.getActualClass(method.getGenericReturnType());
            if (genericTypes.length < 1) {
                throw new IllegalArgumentException("Collection generic");
            }
            elementType = genericTypes[0];
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
