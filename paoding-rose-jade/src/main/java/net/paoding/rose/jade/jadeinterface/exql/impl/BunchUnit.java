package net.paoding.rose.jade.jadeinterface.exql.impl;

import java.util.List;

import net.paoding.rose.jade.jadeinterface.exql.ExprResolver;
import net.paoding.rose.jade.jadeinterface.exql.ExqlContext;
import net.paoding.rose.jade.jadeinterface.exql.ExqlUnit;

/**
 * 顺序输出子单元的语句单元, 例如一个语句段。
 * 
 * @author han.liao
 */
public class BunchUnit implements ExqlUnit {

    private final List<ExqlUnit> units;

    public BunchUnit(List<ExqlUnit> units) {
        this.units = units;
    }

    @Override
    public void fill(ExqlContext exqlContext, ExprResolver exprResolver) throws Exception {

        // 顺序输出子单元
        for (ExqlUnit unit : units) {
            unit.fill(exqlContext, exprResolver);
        }
    }
}
