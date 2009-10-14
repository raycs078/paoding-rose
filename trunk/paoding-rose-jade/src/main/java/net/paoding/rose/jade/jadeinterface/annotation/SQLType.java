/*
 * OPI, all rights reserved.
 */
package net.paoding.rose.jade.jadeinterface.annotation;

/**
 * 定义 Dao 方法的类型。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public enum SQLType {

    /**
     * 自动检测
     */
    AUTO_DETECT,

    /**
     * SELECT 查询
     */
    SELECT,

    /**
     * 除 SELECT 查询的 INSERT / UPDATE / DELETE 语句。
     */
    UPDATE
}
