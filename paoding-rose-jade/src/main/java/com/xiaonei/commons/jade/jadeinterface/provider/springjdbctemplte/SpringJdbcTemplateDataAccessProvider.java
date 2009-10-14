package com.xiaonei.commons.jade.jadeinterface.provider.springjdbctemplte;

import javax.sql.DataSource;

import com.xiaonei.commons.jade.jadeinterface.provider.DataAccess;
import com.xiaonei.commons.jade.jadeinterface.provider.DataAccessProvider;

public class SpringJdbcTemplateDataAccessProvider implements DataAccessProvider {

    @Override
    public DataAccess createDataAccess(DataSource dataSource) {
        return new SpringJdbcTemplateDataAccess(dataSource);
    }
}
