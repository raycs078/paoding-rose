package net.paoding.rose.jade.jadeinterface.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用：{#link MapKey} 标注需要指定作为 {@link java.util.Map}#Key 的字段名。
 * 
 * @author han.liao
 */
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MapKey {

    String DEFAULT_KEY = "id";

    /**
     * 指出用查询结果的哪个字段作为 Key
     * 
     * @return 查询结果的字段名
     */
    String value() default DEFAULT_KEY;
}
