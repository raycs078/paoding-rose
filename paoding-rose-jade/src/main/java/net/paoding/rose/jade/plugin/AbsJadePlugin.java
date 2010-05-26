package net.paoding.rose.jade.plugin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.plugin.model.DataModel;
import net.paoding.rose.jade.provider.Modifier;

/**
 * AbsJadePlugin <br>
 * 
 * @author tai.wang@opi-corp.com May 26, 2010 - 3:56:13 PM
 */
public abstract class AbsJadePlugin implements IJadePlugin {

    private ThreadLocal<DataModel> data = new ThreadLocal<DataModel>();

    @Override
    public void start(DataSource dataSource, String sqlString, Modifier modifier,
            Map<String, Object> parameters) {
        this.data.set(new DataModel());
        DataModel d = data.get();

        d.setStartTime(System.currentTimeMillis());
        try {
            d.setClientIp(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            d.setClientIp("");
            e.printStackTrace();
        }
        startPlugin(d, dataSource, sqlString, modifier, parameters);
    }

    @Override
    public void end() {
        if (null == data.get()) {
            return;
        }
        data.get().setCostTime((int) (System.currentTimeMillis() - data.get().getStartTime()));
        try {
            endPlugin(data.get());
        } finally {
            data.remove();
        }
    }

    final protected DataModel getDataModel() {
        if (null == data.get()) {
            return new DataModel();
        }
        return this.data.get();
    }

    /**
     * startPlugin<br>
     * 
     * @param dataSource
     * @param sqlString
     * @param modifier
     * @param parameters
     * 
     * @author tai.wang@opi-corp.com May 26, 2010 - 3:56:38 PM
     * @param data
     */
    protected abstract void startPlugin(DataModel data, DataSource dataSource, String sqlString,
            Modifier modifier, Map<String, Object> parameters);

    /**
     * endPlugin<br>
     * 
     * 
     * @author tai.wang@opi-corp.com May 26, 2010 - 3:56:45 PM
     * @param dataModel
     */
    protected abstract void endPlugin(DataModel dataModel);
}
