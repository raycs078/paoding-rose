package net.paoding.rose.jade.jadeinterface.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用 {#link Cache} 标注需要缓存的 Dao 接口方法，如果没有标注 Key, 则所有方法 参数都作为缓存关键字。默认的
 * expireTime 为 0 表示没有过期限制。
 * 
 * @author han.liao
 */
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    /**
     * 标注 DAO 方法的缓存关键字。
     * 
     * @return 缓存关键字
     */
    String[] key() default "*";

    /**
     * 标注 DAO 缓存的过期时间。
     * 
     * @return 缓存过期时间
     */
    int expireTime() default 0;
}
