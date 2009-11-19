package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.Method;

import org.springframework.jdbc.core.RowMapper;

/**
 * 定义创建: {@link RowMapper} 的工厂。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public interface RowMapperFactory {

    /**
     * 创建合适的: {@link RowMapper} 对象。
     * 
     * @return
     */
    public RowMapper getRowMapper(Class<?> daoClass, Method method, Class<?> rowType);
}
