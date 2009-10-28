package net.paoding.rose.jade.jadeinterface.exql.impl;

import net.paoding.rose.jade.jadeinterface.exql.ExprResolver;
import net.paoding.rose.jade.jadeinterface.exql.ExqlContext;
import net.paoding.rose.jade.jadeinterface.exql.ExqlUnit;
import net.paoding.rose.jade.jadeinterface.exql.util.ExqlUtils;

/**
 * 按条件选择输出子单元内容的语句单元, 例如: '#if (:expr) {...} #else {...}' 形式的语句。
 * 
 * @author han.liao
 */
public class ChoiceUnit implements ExqlUnit {

    private final String expr;

    private final ExqlUnit unitIfTrue, unitIfFalse;

    /**
     * 构造一元的条件语句单元。
     * 
     * @param expr - 条件表达式
     * @param unit - <code>true</code> 的输出
     */
    public ChoiceUnit(String expr, ExqlUnit unit) {
        this.expr = expr;
        this.unitIfTrue = unit;
        this.unitIfFalse = null;
    }

    /**
     * 构造二元的条件语句单元。
     * 
     * @param expr - 条件表达式
     * @param unitIfTrue - <code>true</code> 的输出
     * @param unitIfFalse - <code>false</code> 的输出
     */
    public ChoiceUnit(String expr, ExqlUnit unitIfTrue, ExqlUnit unitIfFalse) {
        this.expr = expr;
        this.unitIfTrue = unitIfTrue;
        this.unitIfFalse = unitIfFalse;
    }

    @Override
    public void fill(ExqlContext exqlContext, ExprResolver exprResolver) throws Exception {

        // 解释表达式内容
        Object obj = exprResolver.executeExpr(expr);

        if (ExqlUtils.asBoolean(obj)) {

            // 输出第一个单元
            unitIfTrue.fill(exqlContext, exprResolver);

        } else if (unitIfFalse != null) {

            // 输出第二个单元
            unitIfFalse.fill(exqlContext, exprResolver);
        }
    }
}
