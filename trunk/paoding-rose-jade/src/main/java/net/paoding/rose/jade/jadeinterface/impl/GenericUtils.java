package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
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

            Type[] argTypes = ((ParameterizedType) genericType).getActualTypeArguments();

            Class<?>[] argClasses = new Class<?>[argTypes.length];

            for (int index = 0; index < argTypes.length; index++) {

                Type argType = argTypes[index];

                if (argType instanceof Class<?>) {
                    argClasses[index] = (Class<?>) argType;
                }
            }

            return argClasses;
        }

        return EMPTY_CLASSES;
    }

    // 测试代码。
    public static void main(String... args) {

        Class<?> clazz = ArrayList.class;

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
