/*
 * OPI, all rights reserved.
 */
package com.xiaonei.commons.jade.jadeinterface.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 把 {@link SQLParam} 标注在 SQL 查询的方法参数上，说明该参数是 SQL 语句中某参数的值。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
@Target( { ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SQLParam {

    /**
     * 指出这个值是 SQL 语句中哪个参数的值
     * 
     * @return 对应 SQL 语句中哪个参数
     */
    String value();
}
