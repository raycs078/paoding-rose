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

    // 正则表达式
    private static final Pattern PREFIX = Pattern.compile("\\:([a-zA-Z0-9_\\.]*)");

    // 表达式的缓存
    protected static final ConcurrentHashMap<String, Expression> cache = new ConcurrentHashMap<String, Expression>();

    // Jexl 上下文
    protected final JexlContext context;

    /**
     * 构造表达式处理器。
     */
    public ExprResolverImpl() {
        context = JexlHelper.createContext();
    }

    /**
     * 构造表达式处理器。
     * 
     * @param map - 初始的参数表
     */
    public ExprResolverImpl(Map<String, ?> map) {
        context = JexlHelper.createContext();
        context.setVars(map);
    }

    /**
     * 返回表达式处理器的参数表。
     * 
     * @return 处理器的参数表
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getVars() {
        return context.getVars();
    }

    /**
     * 设置表达式处理器的参数表。
     * 
     * @param map - 处理器的参数表
     */
    public void setVars(Map<String, Object> map) {
        context.setVars(map);
    }

    @Override
    public Object executeExpr(String expression) throws Exception {

        // 从缓存中获取解析的表达式
        Expression expr = cache.get(expression);

        if (expr == null) {

            // 删除表达式中的  ':' 字符, 保证可编译
            StringBuilder builder = new StringBuilder(expression.length());

            int index = 0;

            // 匹配正则表达式, 并替换内容
            Matcher matcher = PREFIX.matcher(expression);
            while (matcher.find()) {
                builder.append(expression.substring(index, matcher.start()));
                builder.append(matcher.group(1));
                index = matcher.end();
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
        return context.getVars().get(variant);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setVar(String variant, Object value) {
        context.getVars().put(variant, value);
    }

    // 进行简单测试
    public static void main(String... args) throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        map.put("current", new Date());

        ExprResolver exprResolver = new ExprResolverImpl(map);

        Object obj = exprResolver.executeExpr(":current.year - (:current.month + :current.day)");

        System.out.println(obj);
    }
}
