package net.paoding.rose.jade.jadeinterface.impl;

import javax.sql.DataSource;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public interface DataSourceFactory {

    public DataSource getDataSource(String name);
}
