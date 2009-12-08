package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * 实现工具类，检查参数化类型的参数类型。
 * 
 * @author han.liao
 */
public class GenericUtils {

    private static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];

    /**
     * 从参数, 返回值, 基类的: Generic 类型信息获取传入的实际类信息。
     * 
     * @param genericType - Generic 类型信息
     * 
     * @return 实际类信息
     */
    public static Class<?>[] getActualClass(Type genericType) {

        if (genericType instanceof ParameterizedType) {

            Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
            Class<?>[] actualClasses = new Class<?>[actualTypes.length];

            for (int i = 0; i < actualTypes.length; i++) {
                Type actualType = actualTypes[i];
                if (actualType instanceof Class<?>) {
                    actualClasses[i] = (Class<?>) actualType;
                } else if (actualType instanceof GenericArrayType) {
                    Type componentType = ((GenericArrayType) actualType).getGenericComponentType();
                    actualClasses[i] = Array.newInstance((Class<?>) componentType, 0).getClass();
                }
            }

            return actualClasses;
        }

        return EMPTY_CLASSES;
    }

    // 测试代码
    public static void main(String... args) {

        Class<?> clazz = ClassLoader.class;

        for (Method method : clazz.getMethods()) {
            Class<?>[] classes = getActualClass(method.getGenericReturnType());
            System.out.print(method.getName() + " = ");
            System.out.println(Arrays.toString(classes));
        }

        for (Type genericInterfaceType : clazz.getGenericInterfaces()) {
            Class<?>[] classes = getActualClass(genericInterfaceType);
            System.out.print(genericInterfaceType + " = ");
            System.out.println(Arrays.toString(classes));
        }
    }
}
