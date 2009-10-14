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
 * 把 {@link SQL} 标注在 Dao 接口的方法上，填入 SQL 语句，使能够调用该方法访问数据。
 * 
 * <p>
 * SQL 语句的动态参数必须以冒号开始并紧跟一个名字字符串表示，如：<br>
 * UPDATE user SET password=:password WHERE id=:id
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
@Target( { ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SQL {

    /**
     * 标注 Dao 方法的类型。
     * 
     * @return 方法的类型
     */
    SQLType type() default SQLType.AUTO_DETECT;

    /**
     * 标注与 Dao 方法绑定的 SQL 语句。
     * 
     * @return SQL 语句
     */
    String value();
}
