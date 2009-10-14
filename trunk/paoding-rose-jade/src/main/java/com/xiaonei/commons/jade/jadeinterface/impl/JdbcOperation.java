package com.xiaonei.commons.jade.jadeinterface.impl;

import java.lang.reflect.Method;

import com.xiaonei.commons.jade.jadeinterface.provider.DataAccess;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public interface JdbcOperation {

    public Object execute(DataAccess dataAccess, Class<?> daoClass, Method method, Object[] args);

}
