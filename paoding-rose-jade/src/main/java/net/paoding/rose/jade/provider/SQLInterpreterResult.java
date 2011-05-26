package net.paoding.rose.jade.provider;

import java.util.Properties;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 * 
 */
public interface SQLInterpreterResult {

    /**
     * 
     * @return
     */
    String getSQL();

    /**
     * 
     * @return
     */
    Object[] getParameters();

    /**
     * 散库库名，如果没有散库返回null
     * 
     * @return
     */
    Properties getClientInfo();
}
