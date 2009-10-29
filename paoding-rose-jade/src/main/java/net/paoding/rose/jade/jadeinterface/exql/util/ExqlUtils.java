package net.paoding.rose.jade.jadeinterface.exql.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 实现表达式求值时的常用方法。
 * 
 * @author han.liao
 */
public class ExqlUtils {

    /**
     * 检查对象是否有效。
     * 
     * @param obj - 检查的对象
     * 
     * @return true / false
     */
    public static boolean isValid(Object obj) {

        return (obj != null);
    }

    /**
     * 将对象转换成: Boolean 值。
     * 
     * @param obj - 用于转换的对象
     * 
     * @return true / false
     */
    public static boolean asBoolean(Object obj) {

        if (obj == null) {

            return false; // 空值永远表示无效

        } else if (obj instanceof CharSequence) {

            return ((CharSequence) obj).length() > 0; // 字符串类型, 长度 > 0 表示有效

        } else if (obj instanceof Number) {

            return ((Number) obj).doubleValue() > 0; // 数字类型, > 0 表示有效

        } else if (obj instanceof Boolean) {

            return ((Boolean) obj).booleanValue(); // Boolean 类型, true 表示有效

        } else if (obj instanceof Collection<?>) {

            return ((Collection<?>) obj).size() > 0; // 容器类型, 对象数量 > 0 表示有效
        }

        return true; // 任意对象，引用不为空就判定有效
    }

    /**
     * 将对象转换成: java.lang.Object 数组。
     * 
     * @param obj 用于转换的对象
     * 
     * @return java.lang.Object 数组
     */
    public static Object[] asArray(Object obj) {

        if ((obj != null) && obj.getClass().isArray()) {

            Class<?> componentType = obj.getClass().getComponentType();

            if (componentType.isPrimitive()) {

                final int length = Array.getLength(obj);

                Object[] array = new Object[length];

                for (int index = 0; index < length; index++) {
                    array[index] = Array.get(obj, index);
                }

                return array;
            }

            return (Object[]) obj;
        }

        return new Object[] { obj };
    }

    /**
     * 将对象转换成: Collection 集合。
     * 
     * @param obj - 用于转换的对象
     * 
     * @return Collection 集合
     */
    public static Collection<?> asCollection(Object obj) {

        if (obj == null) {

            return Collections.EMPTY_SET; // 返回空集合

        } else if (obj.getClass().isArray()) {

            return Arrays.asList(asArray(obj));

        } else if (obj instanceof Collection<?>) {

            return (Collection<?>) obj; // List, Set, Collection 直接返回

        } else if (obj instanceof Map<?, ?>) {

            return ((Map<?, ?>) obj).entrySet(); // 映射表， 返回条目的集合

        } else {

            return Arrays.asList(obj); // 其他类型, 返回包含单个对象的集合
        }
    }
}
