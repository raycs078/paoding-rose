package net.paoding.rose.jade.jadeinterface.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class Modifier {

    private Method method;

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
