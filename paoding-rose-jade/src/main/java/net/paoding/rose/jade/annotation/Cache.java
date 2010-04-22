package net.paoding.rose.jade.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用：{#link Cache} 标注需要缓存的 DAO 接口方法。默认的 expiry 为 0 表示没有过期限制。
 * 
 * @author han.liao
 */
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    /**
     * 标注 DAO 方法的缓存 Pool
     * 
     * @return 缓存 Pool
     */
    String pool();

    /**
     * 标注 DAO 方法的缓存 Key.
     * 
     * @return 缓存 Key
     */
    String key();

    /**
     * 标注 DAO 缓存的过期时间。
     * 
     * @return 缓存过期时间
     */
    int expiry() default 0;
}
