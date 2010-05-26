package net.paoding.rose.jade.plugin;

import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.provider.Modifier;

/**
 * IJadePlugin <br>
 *
 * @author tai.wang@opi-corp.com May 26, 2010 - 4:21:52 PM
 */
public interface IJadePlugin {

    /**
     * end<br>
     *
     *
     * @author tai.wang@opi-corp.com May 26, 2010 - 4:22:01 PM
     */
    public void end();

    /**
     * start<br>
     *
     * @param dataSource
     * @param sqlString
     * @param modifier
     * @param parameters
     *
     * @author tai.wang@opi-corp.com May 26, 2010 - 4:22:07 PM
     */
    public void start(DataSource dataSource, String sqlString, Modifier modifier,
            Map<String, Object> parameters);

}
