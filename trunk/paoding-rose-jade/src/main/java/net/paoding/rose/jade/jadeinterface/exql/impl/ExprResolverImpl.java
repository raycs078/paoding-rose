package net.paoding.rose.jade.jadeinterface.exql.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.paoding.rose.jade.jadeinterface.exql.ExprResolver;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.lang.math.NumberUtils;

/**
 * 默认使用: Apache Common Jexl 引擎实现表达式处理。
 * 
 * @author han.liao
 */
public class ExprResolverImpl implements ExprResolver {

    // 表达式的缓存
    protected static final ConcurrentHashMap<String, Expression> cache = new ConcurrentHashMap<String, Expression>();

    // 正则表达式
    private static final Pattern PREFIX_PATTERN = Pattern.compile( // NL
            "(\\:|\\$)([a-zA-Z0-9_]+)(\\.[a-zA-Z0-9_]+)*");

    // 常量前缀
    private static final String CONST_PREFIX = "_mapConsts";

    // 参数前缀
    private static final String VAR_PREFIX = "_mapVars";

    // 带验证的  java.util.Map 实现
    public static class CheckingMap implements Map<String, Object> {

        protected Map<String, Object> map;

        public CheckingMap(Map<String, Object> map) {
            this.map = map;
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public boolean containsKey(Object key) {
            if (!map.containsKey(key)) {
                throw new Error("jdQL param not found: " + key);
            }
            return map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        @Override
        public Set<java.util.Map.Entry<String, Object>> entrySet() {
            return map.entrySet();
        }

        @Override
        public Object get(Object key) {
            if (!map.containsKey(key)) {
                throw new Error("jdQL param not found: " + key);
            }
            return map.get(key);
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public Set<String> keySet() {
            return map.keySet();
        }

        @Override
        public Object put(String key, Object value) {
            return map.put(key, value);
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> m) {
            map.putAll(m);
        }

        @Override
        public Object remove(Object key) {
            return map.remove(key);
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public Collection<Object> values() {
            return map.values();
        }
    }

    // 参数表
    protected final Map<String, Object> mapVars = new CheckingMap(new HashMap<String, Object>());

    // 常量表
    protected final Map<String, Object> mapConsts = new CheckingMap(new HashMap<String, Object>());

    // Common Jexl 上下文
    protected final JexlContext context = JexlHelper.createContext();

    /**
     * 构造表达式处理器。
     */
    @SuppressWarnings("unchecked")
    public ExprResolverImpl() {
        Map map = context.getVars();
        map.put(VAR_PREFIX, mapVars);
        map.put(CONST_PREFIX, mapConsts);
    }

    /**
     * 构造表达式处理器。
     * 
     * @param mapVars - 初始的参数表
     */
    public ExprResolverImpl(Map<String, ?> mapVars) {
        this();
        this.mapVars.putAll(mapVars);
    }

    /**
     * 构造表达式处理器。
     * 
     * @param mapVars - 初始的参数表
     * @param mapConsts - 初始的常量表
     */
    public ExprResolverImpl(Map<String, ?> mapVars, Map<String, ?> mapConsts) {
        this();
        this.mapVars.putAll(mapVars);
        this.mapConsts.putAll(mapConsts);
    }

    /**
     * 返回表达式处理器的参数表。
     * 
     * @return 处理器的参数表
     */
    public Map<String, ?> getVars() {
        return mapVars;
    }

    /**
     * 设置表达式处理器的参数表。
     * 
     * @param map - 处理器的参数表
     */
    public void setVars(Map<String, ?> map) {
        mapVars.putAll(map);
    }

    /**
     * 返回表达式处理器的常量表。
     * 
     * @return 处理器的常量表
     */
    public Map<String, ?> getConstants() {
        return mapConsts;
    }

    /**
     * 设置表达式处理器的常量表。
     * 
     * @param map - 处理器的常量表
     */
    public void setConstants(Map<String, ?> map) {
        mapConsts.putAll(map);
    }

    @Override
    public Object executeExpr(String expression) throws Exception {

        // 从缓存中获取解析的表达式
        Expression expr = cache.get(expression);

        if (expr == null) {

            // 转换表达式中的前缀字符, 保证可编译
            StringBuilder builder = new StringBuilder(expression.length());

            int index = 0;

            // 匹配正则表达式, 并替换内容
            Matcher matcher = PREFIX_PATTERN.matcher(expression);
            while (matcher.find()) {

                builder.append(expression.substring(index, matcher.start()));

                String prefix = matcher.group(1);
                String name = matcher.group(2);
                if (":".equals(prefix)) {
                    if (NumberUtils.isDigits(name)) {
                        // 按顺序访问变量
                        builder.append(VAR_PREFIX);
                        builder.append("[\':");
                        builder.append(name);
                        builder.append("\']");

                    } else {
                        // 按名称访问变量
                        builder.append(VAR_PREFIX);
                        builder.append("[\'");
                        builder.append(name);
                        builder.append("\']");
                    }

                } else if ("$".equals(prefix)) {
                    // 拼出常量访问语句
                    builder.append(CONST_PREFIX);
                    builder.append("[\'");
                    builder.append(name);
                    builder.append("\']");
                }

                index = matcher.end(2);
            }

            builder.append(expression.substring(index));

            // 编译表达式
            expr = ExpressionFactory.createExpression(builder.toString());
            cache.putIfAbsent(expression, expr);
        }

        // 进行表达式求值
        return expr.evaluate(context);
    }

    @Override
    public Object getVar(String variant) {
        return mapVars.get(variant);
    }

    @Override
    public void setVar(String variant, Object value) {
        mapVars.put(variant, value);
    }

    // 进行简单测试
    public static void main(String... args) throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        map.put("current", new Date());
        map.put("numbers", new Integer[] { 1, 2, 3, 5, 8, 13, 21, 34 });
        map.put("index", 5);
        map.put(":2", 2);

        ExprResolver exprResolver = new ExprResolverImpl(map, map);

        System.out.println( // NL
                exprResolver.executeExpr( // NL
                        "$numbers[:index] + :2"));
        System.out.println( // NL
                exprResolver.executeExpr( // NL
                        ":current.year - ($current.month + $current.day) - :2"));
        System.out.println( // NL
                exprResolver.executeExpr( // NL
                        ":current.year - ($current.month + $current.day) + $numbers[:index] + :2"));
    }
}
