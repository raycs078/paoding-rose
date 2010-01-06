package net.paoding.rose.jade.jadeinterface.impl;

import net.paoding.rose.jade.jadeinterface.provider.Modifier;

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
     * @return {@link RowMapper} 对象
     */
    public RowMapper getRowMapper(Modifier modifier);
}
