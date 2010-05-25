package net.paoding.rose.jade.provider.jdbctemplate.plugin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.util.Assert;

import net.paoding.rose.jade.provider.Modifier;
import net.paoding.rose.jade.provider.jdbctemplate.plugin.model.DataModel;

public abstract class AbsDBMonitorPlugin implements IDBMonitorPlugin {

    private ThreadLocal<DataModel> data = new ThreadLocal<DataModel>();

    @Override
    public void initData(DataSource dataSource, String sqlString, Modifier modifier,
            Map<String, Object> parameters) {
        this.data.set(new DataModel());
        start();
        init(dataSource, sqlString, modifier, parameters);
    }

    @Override
    public void listen() {
        Assert.isTrue(null != data.get());
        data.get().setCostTime(System.currentTimeMillis() - data.get().getCostTime());
        listenDB();
        data.remove();
    }

    private void start() {
        DataModel d = data.get();

        d.setStartTime(System.currentTimeMillis());
        d.setCostTime(System.currentTimeMillis());
        try {
            d.setClientIp(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            d.setClientIp("");
            e.printStackTrace();
        }
    }

    final protected DataModel getDataModel() {
        Assert.isTrue(null != data.get());
        return this.data.get();
    }

    protected abstract void init(DataSource dataSource, String sqlString, Modifier modifier,
            Map<String, Object> parameters);

    protected abstract void listenDB();
}
