package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    public RowMapperDelegate(RowMapperFactory rowMapperFactory, Class<?> daoClass, Method method) {
        this.rowMapperFactory = rowMapperFactory;
        this.daoClass = daoClass;
        this.method = method;
    }

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (rowMapper == null) {
            rowMapper = rowMapperFactory.getRowMapper(daoClass, method, rs, getRowType(method, rs));
        }
        return rowMapper.mapRow(rs, rowNum);
    }

    private static Class<?> getRowType(Method method, ResultSet rs) throws SQLException {
        Class<?> returnClassType = method.getReturnType();
        Class<?> rowType = returnClassType;
        if (Collection.class.isAssignableFrom(returnClassType)) {
            if (returnClassType != List.class && returnClassType != Collection.class
                    && returnClassType != Set.class) {
                throw new IllegalArgumentException("error collection type "
                        + returnClassType.getName() + "; only support List, Set, Collection");
            }
            Class<?>[] genericTypes = GenericUtils.getActualClass(method.getGenericReturnType());
            if (genericTypes.length < 1) {
                throw new IllegalArgumentException("Collection generic");
            }
            rowType = genericTypes[0];
        } else if (Map.class == returnClassType) {
            Class<?>[] genericTypes = GenericUtils.getActualClass(method.getGenericReturnType());
            if (genericTypes.length != 2) {
                throw new IllegalArgumentException("Map generic");
            }
            rowType = genericTypes[1]; // 取  V 类型
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
        return rowType;
    }
}
