package net.paoding.rose.jade.plugin;

import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.provider.Modifier;

/**
 * JadePluginWrapper <br>
 * 
 * @author tai.wang@opi-corp.com May 26, 2010 - 4:22:18 PM
 */
public class JadePluginWrapper implements IJadePlugin {

    IJadePlugin[] plugins = null;

    public JadePluginWrapper(IJadePlugin[] plugins) {
        this.plugins = plugins;
    }

    @Override
    public void end() {
        if (null == plugins) {
            return;
        }
        for (IJadePlugin plugin : plugins) {
            plugin.end();
        }
    }

    @Override
    public void start(DataSource dataSource, String sqlString, Modifier modifier,
            Map<String, Object> parameters) {
        if (null == plugins) {
            return;
        }
        for (IJadePlugin plugin : plugins) {
            plugin.start(dataSource, sqlString, modifier, parameters);
        }
    }

}
