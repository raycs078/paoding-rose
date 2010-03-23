package net.paoding.rose.jade.jadeinterface.provider.exql;

import javax.sql.DataSource;

import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.springjdbctemplte.JdbcTemplateDataAccessProvider;

/**
 * 提供动态: SQL 语句功能的 {@link DataAccessProvider} 实现。
 * 
 * @author han.liao
 */
public class ExqlDataAccessProvider extends JdbcTemplateDataAccessProvider {

    @Override
    protected DataAccess createDataAccess(DataSource dataSource) {
        return new ExqlDataAccess(dataSource);
    }
}
