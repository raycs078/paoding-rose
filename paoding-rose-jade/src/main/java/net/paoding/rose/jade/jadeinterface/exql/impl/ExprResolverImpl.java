package net.paoding.rose.jade.jadeinterface.exql.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.paoding.rose.jade.jadeinterface.exql.ExprResolver;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

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
    private static final String CONST_PREFIX = "_jadeconst";

    // 参数前缀
    private static final String VAR_PREFIX = "_jadevar";

    // 参数表
    protected final HashMap<String, Object> mapVars = new HashMap<String, Object>();

    // 常量表
    protected final HashMap<String, Object> mapConsts = new HashMap<String, Object>();

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
     * @param map - 初始的参数表
     */
    public ExprResolverImpl(Map<String, ?> map) {
        this();
        this.mapVars.putAll(map);
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
    public Map<String, Object> getVars() {
        return mapVars;
    }

    /**
     * 设置表达式处理器的参数表。
     * 
     * @param map - 处理器的参数表
     */
    public void setVars(Map<String, Object> map) {
        mapVars.putAll(map);
    }

    /**
     * 返回表达式处理器的常量表。
     * 
     * @return 处理器的常量表
     */
    public Map<String, Object> getConstants() {
        return mapConsts;
    }

    /**
     * 设置表达式处理器的常量表。
     * 
     * @param map - 处理器的常量表
     */
    public void setConstants(Map<String, Object> map) {
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
                if (":".equals(prefix)) {
                    // 拼出变量访问语句
                    builder.append(VAR_PREFIX);
                    builder.append("[\'");
                    builder.append(matcher.group(2));
                    builder.append("\']");

                } else if ("$".equals(prefix)) {
                    // 拼出常量访问语句
                    builder.append(CONST_PREFIX);
                    builder.append("[\'");
                    builder.append(matcher.group(2));
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

        ExprResolver exprResolver = new ExprResolverImpl(map, map);

        Object obj = exprResolver.executeExpr(":current.year - ($current.month + $current.day)");

        System.out.println(obj);
    }
}
