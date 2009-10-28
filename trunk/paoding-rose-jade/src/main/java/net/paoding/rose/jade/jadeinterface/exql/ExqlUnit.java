package net.paoding.rose.jade.jadeinterface.exql;

/**
 * 定义一个语句输出单元, 可以进行组装。
 * 
 * @author han.liao
 */
public interface ExqlUnit {

    /**
     * 输出单元的语句内容。
     * 
     * @param exqlContext - 输出上下文
     * @param exprResolver - 解析表达式
     * 
     * @throws Exception
     */
    void fill(ExqlContext exqlContext, ExprResolver exprResolver) throws Exception;
}
