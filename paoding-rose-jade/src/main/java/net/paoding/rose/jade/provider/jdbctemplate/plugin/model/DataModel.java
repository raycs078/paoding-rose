package net.paoding.rose.jade.provider.jdbctemplate.plugin.model;

import java.io.Serializable;

public class DataModel implements Serializable {

    private static final long serialVersionUID = 6286287422893255667L;

    /** 开始时间 */
    private long startTime;

    /** 消耗时间 */
    private long costTime;

    /** 数据源 */
    private String dataSource;

    /** SQL */
    private String sql;

    /** SQL 参数 */
    private String sqlParams;

    /** 客户端ip */
    private String clientIp;

    /** 类名 */
    private String className;

    /** 方法名 */
    private String methodName;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCostTime() {
        return costTime;
    }

    public void setCostTime(long costTime) {
        this.costTime = costTime;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSqlParams() {
        return sqlParams;
    }

    public void setSqlParams(String sqlParams) {
        this.sqlParams = sqlParams;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

}
