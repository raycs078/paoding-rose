package net.paoding.rose.jade.provider.jdbctemplate.plugin;

import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.provider.Modifier;

public class EmptyDBMonitorPlugin implements IDBMonitorPlugin {

    @Override
    public void initData(DataSource dataSource, String sqlString, Modifier modifier,
            Map<String, Object> parameters) {

    }

    @Override
    public void listen() {

    }

}
