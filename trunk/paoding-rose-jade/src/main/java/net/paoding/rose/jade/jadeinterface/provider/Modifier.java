package net.paoding.rose.jade.jadeinterface.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.paoding.rose.jade.jadeinterface.impl.GenericUtils;

/**
 * 提供 Modifier 包装对 DAO 方法的访问。
 * 
 * @author han.liao[in355hz@gmail.com]
 */
public class Modifier {

    private final Definition definition;

    private final Method method;

    private final Class<?>[] genericReturnType;

    private final Map<Class<? extends Annotation>, Annotation[]> parameterAnnotations = new HashMap<Class<? extends Annotation>, Annotation[]>(
            8, 1.0f);

    public Modifier(Definition definition, Method method) {
        this.definition = definition;
        this.method = method;

        genericReturnType = GenericUtils.getActualClass(method.getGenericReturnType());

        Annotation[][] annotations = method.getParameterAnnotations();
        for (int index = 0; index < annotations.length; index++) {
            for (Annotation annotation : annotations[index]) {

                Class<? extends Annotation> annotationType = annotation.annotationType();
                Annotation[] annotationArray = parameterAnnotations.get(annotationType);
                if (annotationArray == null) {
                    annotationArray = (Annotation[]) Array.newInstance( // NL
                            annotationType, annotations.length);
                    parameterAnnotations.put(annotationType, annotationArray);
                }

                annotationArray[index] = annotation;
            }
        }
    }

    public String getName() {
        return method.getName();
    }

    public Definition getDefinition() {
        return definition;
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public Class<?>[] getGenericReturnType() {
        return genericReturnType;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> T[] getParameterAnnotations(Class<T> annotationClass) {
        T[] annotations = (T[]) parameterAnnotations.get(annotationClass);
        if (annotations == null) {
            annotations = (T[]) Array.newInstance(annotationClass, 0);
        }
        return annotations;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Modifier) {
            Modifier modifier = (Modifier) obj;
            return definition.equals(modifier.definition) && method.equals(modifier.method);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return definition.hashCode() ^ method.hashCode();
    }

    @Override
    public String toString() {
        return definition.getName() + '#' + method.getName();
    }
}
