package net.paoding.rose.jade.jadeinterface.provider.jdbctemplate.exql;

import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.jdbctemplate.JdbcTemplateDataAccess;
import net.paoding.rose.jade.jadeinterface.provider.jdbctemplate.JdbcTemplateDataAccessProvider;

/**
 * 提供动态: SQL 语句功能的 {@link DataAccessProvider} 实现。
 * 
 * @author han.liao
 */
public class ExqlJdbcTemplateDataAccessProvider extends JdbcTemplateDataAccessProvider {

    @Override
    protected JdbcTemplateDataAccess createEmptyJdbcTemplateDataAccess() {
        return new ExqlJdbcTemplateDataAccess();
    }
}
