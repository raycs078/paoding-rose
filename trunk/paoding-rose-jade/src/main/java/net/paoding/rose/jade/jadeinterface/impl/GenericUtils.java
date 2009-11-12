package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 实现工具类，检查参数化类型的参数类型。
 * 
 * @author han.liao
 */
public class GenericUtils {

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

        return new Class<?>[0];
    }

    public interface Test extends List<Boolean> {

        public List<Integer> toList();

        public Set<Integer> toSet();

        public Collection<Integer> toCollection();

        public Map<Long, String> toMap();
    };

    // 测试代码。
    public static void main(String... args) {

        Class<?> clazz = Test.class;

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
