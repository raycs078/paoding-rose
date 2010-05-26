package net.paoding.rose.jade.plugin;

import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.provider.Modifier;

/**
 * EmptyJadePlugin <br>
 *
 * @author tai.wang@opi-corp.com May 26, 2010 - 4:39:06 PM
 */
public class EmptyJadePlugin implements IJadePlugin {

    @Override
    public void end() {
        // do nothing
        
    }

    @Override
    public void start(DataSource dataSource, String sqlString, Modifier modifier,
            Map<String, Object> parameters) {
        // do nothing
        
    }


}
