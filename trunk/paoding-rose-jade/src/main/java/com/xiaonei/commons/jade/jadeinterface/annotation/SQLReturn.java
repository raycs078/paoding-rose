package com.xiaonei.commons.jade.jadeinterface.annotation;

/**
 * 定义 SQL 操作的返回值。
 * 
 * @author han.liao
 */
public @interface SQLReturn {

    /**
     * 返回值的类型。
     * 
     * @return 返回值的类型
     */
    SQLReturnType value();
}
