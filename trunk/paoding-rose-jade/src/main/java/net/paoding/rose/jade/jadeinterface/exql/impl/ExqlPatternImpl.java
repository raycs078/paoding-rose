package net.paoding.rose.jade.jadeinterface.exql.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.paoding.rose.jade.jadeinterface.exql.ExqlContext;
import net.paoding.rose.jade.jadeinterface.exql.ExqlPattern;
import net.paoding.rose.jade.jadeinterface.exql.ExqlUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 实现语句的执行接口。
 * 
 * @author han.liao
 */
public class ExqlPatternImpl implements ExqlPattern {

    // 输出日志
    private static final Log logger = LogFactory.getLog(ExqlPattern.class);

    // 语句的缓存
    private static final ConcurrentHashMap<String, ExqlPattern> cache = new ConcurrentHashMap<String, ExqlPattern>();

    // 编译的语句
    protected final String pattern;

    // 输出的单元
    protected final ExqlUnit unit;

    /**
     * 构造语句的执行接口。
     * 
     * @param pattern - 编译的语句
     * @param unit - 输出的单元
     */
    protected ExqlPatternImpl(String pattern, ExqlUnit unit) {
        this.pattern = pattern;
        this.unit = unit;
    }

    /**
     * 从语句编译: ExqlPattern 对象。
     * 
     * @param pattern - 待编译的语句
     * 
     * @return ExqlPattern 对象
     */
    public static ExqlPattern compile(String pattern) {

        // 输出日志
        if (logger.isDebugEnabled()) {
            logger.debug("EXQL pattern compiling:\n    pattern: " + pattern);
        }

        // 从缓存中获取编译好的语句
        ExqlPattern compiledPattern = cache.get(pattern);
        if (compiledPattern == null) {

            // 重新编译语句
            ExqlCompiler compiler = new ExqlCompiler(pattern);
            compiledPattern = compiler.compile();

            // 语句的缓存
            cache.putIfAbsent(pattern, compiledPattern);
        }

        return compiledPattern;
    }

    @Override
    public String execute(ExqlContext context, Map<String, ?> map) throws Exception {

        // 转换语句内容
        unit.fill(context, new ExprResolverImpl(map));

        String flushOut = context.flushOut();

        // 输出日志
        if (logger.isDebugEnabled()) {
            logger.debug("EXQL pattern executing:\n    origin: " + pattern + "\n    result: "
                    + flushOut + "\n    params: " + Arrays.toString(context.getParams()));
        }

        return flushOut;
    }

    // 进行简单测试
    public static void main(String... args) throws Exception {

        // 编译下列语句
        ExqlPattern pattern = ExqlPatternImpl.compile("SELECT :expr1, #(:expr2.class),"
                + " ##(:expr3) WHERE #if(:expr4) {e = :expr4} #else {e IS NULL}"
                + "#for(variant in :expr5.bytes) { AND c = :variant}" // NL
                + " GROUP BY ##(:expr1) ASC");

        ExqlContext context = new ExqlContextImpl();

        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("expr1", "expr1");
        map.put("expr2", "expr2");
        map.put("expr3", "expr3");
        map.put("expr4", "expr4");
        map.put("expr5", "expr5");

        System.out.println(pattern.execute(context, map));
        System.out.println(Arrays.toString(context.getParams()));
    }
}
