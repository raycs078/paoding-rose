package net.paoding.rose.jade.jadeinterface.provider.springjdbctemplte;

import javax.sql.DataSource;

import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;

public class SpringJdbcTemplateDataAccessProvider implements DataAccessProvider {

    @Override
    public DataAccess createDataAccess(DataSource dataSource) {
        return new SpringJdbcTemplateDataAccess(dataSource);
    }
}
