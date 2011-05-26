package net.paoding.rose.jade.provider.jdbc;

import java.util.Properties;

import net.paoding.rose.jade.provider.SQLInterpreterResult;

public class SQLInterpreterResultImpl implements SQLInterpreterResult {

    private String sql;

    private Object[] parameters;

    private Properties clientInfo;

    public SQLInterpreterResultImpl() {
    }

    public SQLInterpreterResultImpl(String sql, Object[] parameters) {
        setSQL(sql);
        setParameters(parameters);
    }

    @Override
    public String getSQL() {
        return sql;
    }

    public void setSQL(String sql) {
        this.sql = sql;
    }

    @Override
    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public Properties getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(Properties clientInfo) {
        if (this.clientInfo == null) {
            this.clientInfo = clientInfo;
        } else {
            this.clientInfo.putAll(clientInfo);
        }
    }

    public void setClientInfo(String name, String value) {
        if (this.clientInfo == null) {
            this.clientInfo = new Properties();
        }
        this.clientInfo.setProperty(name, value);
    }

    public String getClientInfo(String name) {
        return this.clientInfo == null ? null : this.clientInfo.getProperty(name);
    }

}
