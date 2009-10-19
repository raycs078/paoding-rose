package net.paoding.rose.jade.jadeinterface.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 提供 Modifier 包装对 Dao 方法的访问。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class Modifier {

    private Method method;

    public Modifier(Method method) {
        this.method = method;
    }

    public String getMethodName() {
        return method.getName();
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return (T) method.getAnnotation(annotationClass);
    }
}
