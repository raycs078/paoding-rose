package net.paoding.rose.jade.jadeinterface.annotation;

/**
 * 定义 SQL 语句的返回值类型
 * 
 * @author han.liao
 */
public enum SQLReturnType {

    /**
     * 返回新插入对象的 ID.
     */
    ID,

    /**
     * 返回更新的数目。
     */
    UPDATE_COUNT
}
