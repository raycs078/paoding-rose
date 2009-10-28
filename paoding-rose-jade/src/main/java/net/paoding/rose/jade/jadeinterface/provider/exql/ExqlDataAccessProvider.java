package net.paoding.rose.jade.jadeinterface.provider.exql;

import javax.sql.DataSource;

import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;

public class ExqlDataAccessProvider implements DataAccessProvider {

    @Override
    public DataAccess createDataAccess(DataSource dataSource) {
        return new ExqlDataAccess(dataSource);
    }
}
