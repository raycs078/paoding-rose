package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public interface RowMapperFactory {

    public RowMapper getRowMapper(Class<?> daoClass, Method method, ResultSet resultSet,
            Class<?> rowType) throws SQLException;
}
