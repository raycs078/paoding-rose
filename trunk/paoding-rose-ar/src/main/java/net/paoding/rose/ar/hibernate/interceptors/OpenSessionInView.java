package net.paoding.rose.ar.hibernate.interceptors;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在控制器或其父类中标注 {@link OpenSessionInView}
 * 表示使用OpenSessionInView的模式访问Hibernate.
 * <p>
 * 
 * @author 王志亮 zhiliang.wang@opi-corp.com
 */
@Inherited
@Target( { ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpenSessionInView {

    boolean enabled() default true;
}
