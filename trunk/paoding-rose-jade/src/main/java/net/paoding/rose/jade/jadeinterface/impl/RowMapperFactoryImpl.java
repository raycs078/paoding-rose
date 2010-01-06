package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.paoding.rose.jade.jadeinterface.annotation.MapKey;
import net.paoding.rose.jade.jadeinterface.provider.Modifier;

import org.apache.commons.lang.ClassUtils;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.NumberUtils;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class RowMapperFactoryImpl implements RowMapperFactory {

    // 获得返回的集合元素类型
    private static Class<?> getRowType(Modifier modifier) {

        Class<?> returnClassType = modifier.getReturnType();
        Class<?> rowType = returnClassType;

        if (Collection.class.isAssignableFrom(returnClassType)) {

            // 仅支持  List / Collection / Set
            if ((returnClassType != List.class) && (returnClassType != Collection.class)
                    && (returnClassType != Set.class)) {
                throw new IllegalArgumentException("error collection type "
                        + returnClassType.getName() + "; only support List, Set, Collection");
            }
            // 获取集合元素类型
            Class<?>[] genericTypes = modifier.getGenericReturnType();
            if (genericTypes.length < 1) {
                throw new IllegalArgumentException(modifier + ": Collection<T> must be generic");
            }
            rowType = genericTypes[0];

        } else if (Map.class == returnClassType) {

            // 获取  Map<K, V> 值元素类型
            Class<?>[] genericTypes = modifier.getGenericReturnType();
            if (genericTypes.length != 2) {
                throw new IllegalArgumentException(modifier + ": Map<K, V> must be generic");
            }
            rowType = genericTypes[1]; // 取  V 类型

        } else if (returnClassType.isArray()) {

            // 数组类型, 支持多重数组
            rowType = returnClassType.getComponentType();
        }

        return rowType;
    }

    @Override
    public RowMapper getRowMapper(Modifier modifier) {

        Class<?> returnClassType = modifier.getReturnType();
        Class<?> rowType = getRowType(modifier);

        // BUGFIX: SingleColumnRowMapper 处理  Primitive Type 抛异常
        if (rowType.isPrimitive()) {
            rowType = ClassUtils.primitiveToWrapper(rowType);
        }

        // 根据类型创建  RowMapper
        RowMapper rowMapper;

        if ((String.class == rowType) // NL
                || Date.class.isAssignableFrom(rowType)
                || (ClassUtils.wrapperToPrimitive(rowType) != null)) {
            // 目前只考虑  java.lang.String, java.util.Date(java.sql.Date, 
            // java.sql.Time, java.sql.Timestamp) 及基本类型
            if (returnClassType == Map.class) {
                rowMapper = new KeyValuePairColumnRowMapper(modifier, rowType);
            } else {
                rowMapper = new SingleColumnRowMapper(rowType);
            }

        } else {
            // 处理组合的类型
            if (rowType == Map.class) {
                rowMapper = new ColumnMapRowMapper();
            } else if (rowType.isArray()) {
                rowMapper = new ArrayRowMapper(rowType);
            } else if ((rowType == List.class) || (rowType == Collection.class)) {
                rowMapper = new ListRowMapper(modifier);
            } else if (rowType == Set.class) {
                rowMapper = new ListRowMapper(modifier);
            } else {
                rowMapper = new BeanPropertyRowMapper(rowType);
            }

            if (returnClassType == Map.class) {
                rowMapper = new KeyValuePairRowMapper(modifier, rowMapper);
            }
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
                Array.set(array, i, JdbcUtils.getResultSetValue(rs, // NL
                        (i + 1), componentType));
            }
            return array;
        }
    }

    // 以  Map<K, V> 返回每一列
    public static class KeyValuePairColumnRowMapper implements RowMapper {

        private String keyColumn;

        private int keyColumnIndex = 0, valueColumnIndex = 0;

        private Class<?> keyType, valueType;

        public KeyValuePairColumnRowMapper(Modifier modifier, Class<?> requiredType) {

            // 获取 Key 类型与列
            MapKey mapKey = modifier.getAnnotation(MapKey.class);
            Class<?>[] genericTypes = modifier.getGenericReturnType();
            if (genericTypes.length < 1) {
                throw new IllegalArgumentException("Map generic");
            }

            // 设置 Key 类型与列
            this.keyColumn = (mapKey != null) ? mapKey.value() : MapKey.DEFAULT_KEY;
            this.keyType = genericTypes[0];
            this.valueType = genericTypes[1];
        }

        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

            // 验证列的数目
            ResultSetMetaData rsmd = rs.getMetaData();
            int nrOfColumns = rsmd.getColumnCount();
            if (nrOfColumns != 2) {
                throw new IncorrectResultSetColumnCountException(2, nrOfColumns);
            }

            if (keyColumnIndex == 0) {
                keyColumnIndex = rs.findColumn(keyColumn);
                valueColumnIndex = (keyColumnIndex == 1) ? 2 : 1;
            }

            // 从  JDBC ResultSet 获取  Key
            Object key = JdbcUtils.getResultSetValue(rs, keyColumnIndex, keyType);
            if (key != null && !keyType.isInstance(key)) {
                try {
                    key = convertValueToRequiredType(key, keyType);
                } catch (IllegalArgumentException ex) {
                    throw new TypeMismatchDataAccessException( // NL
                            "Type mismatch affecting row number " + rowNum + " and column type '"
                                    + rsmd.getColumnTypeName(keyColumnIndex) + "': "
                                    + ex.getMessage());
                }
            }

            // 从  JDBC ResultSet 获取  Value
            Object value = JdbcUtils.getResultSetValue(rs, valueColumnIndex, valueType);
            if (value != null && !valueType.isInstance(value)) {
                try {
                    value = convertValueToRequiredType(value, valueType);
                } catch (IllegalArgumentException ex) {
                    throw new TypeMismatchDataAccessException( // NL
                            "Type mismatch affecting row number " + rowNum + " and column type '"
                                    + rsmd.getColumnTypeName(valueColumnIndex) + "': "
                                    + ex.getMessage());
                }
            }

            return new KeyValuePair(key, value);
        }
    }

    // 用  Map<K, V> 返回每一列
    protected static class KeyValuePairRowMapper implements RowMapper {

        private final RowMapper mapper;

        private String keyColumn;

        private int keyColumnIndex = 0;

        private Class<?> keyType;

        public KeyValuePairRowMapper(Modifier modifier, RowMapper mapper) {

            MapKey mapKey = modifier.getAnnotation(MapKey.class);
            Class<?>[] genericTypes = modifier.getGenericReturnType();
            if (genericTypes.length < 1) {
                throw new IllegalArgumentException("Map generic");
            }

            this.keyColumn = (mapKey != null) ? mapKey.value() : MapKey.DEFAULT_KEY;
            this.keyType = genericTypes[0];
            this.mapper = mapper;
        }

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

            if (keyColumnIndex == 0) {
                keyColumnIndex = rs.findColumn(keyColumn);
            }

            // 从  JDBC ResultSet 获取 Key
            Object key = JdbcUtils.getResultSetValue(rs, keyColumnIndex, keyType);
            if (key != null && !keyType.isInstance(key)) {
                try {
                    key = convertValueToRequiredType(key, keyType);
                } catch (IllegalArgumentException ex) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    throw new TypeMismatchDataAccessException( // NL
                            "Type mismatch affecting row number " + rowNum + " and column type '"
                                    + rsmd.getColumnTypeName(keyColumnIndex) + "': "
                                    + ex.getMessage());
                }
            }

            Object value = mapper.mapRow(rs, rowNum);
            return new KeyValuePair(key, value);
        }
    }

    // 用  List<T> 返回每一列
    protected static class ListRowMapper extends CollectionRowMapper {

        public ListRowMapper(Modifier modifier) {
            super(modifier);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Collection createCollection(int columnSize) {
            return new ArrayList(columnSize);
        }
    }

    // 用  Set<T> 返回每一列
    protected static class SetRowMapper extends CollectionRowMapper {

        public SetRowMapper(Modifier modifier) {
            super(modifier);
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

        public CollectionRowMapper(Modifier modifier) {
            Class<?>[] genericTypes = modifier.getGenericReturnType();
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

    // 转换对象到指定的类型, 参考 org.springframework.jdbc.core.SingleColumnRowMapper
    protected static Object convertValueToRequiredType(Object value, Class<?> requiredType) {

        if (String.class.equals(requiredType)) {
            return value.toString();

        } else if (Number.class.isAssignableFrom(requiredType)) {
            if (value instanceof Number) {
                return NumberUtils.convertNumberToTargetClass(((Number) value), requiredType);
            } else {
                return NumberUtils.parseNumber(value.toString(), requiredType);
            }

        } else {
            throw new IllegalArgumentException("Value [" + value + "] is of type ["
                    + value.getClass().getName() + "] and cannot be converted to required type ["
                    + requiredType.getName() + "]");
        }
    }
}
