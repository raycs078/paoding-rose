/*
 * OPI, all rights reserved.
 */
package net.paoding.rose.jade.jadeinterface.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用 xiaonei-commons-jade 的 DAO 接口需要明确标注 {@link Dao} 注解，并指定所使用的数据源
 * (catalog)。
 * 
 * <p>
 * xiaonei-commons-jade 要求数据访问对象接口必须在 xxx.yyy.dao 包或其子 package 包下 (xxx.yyy
 * 命名可任意)， 并且以 "Dao" 为结尾。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Dao {

    /**
     * 指定此所标注的 Dao 类所要使用的数据源。
     * 
     * @return 使用的数据源
     */
    String catalog();
}
