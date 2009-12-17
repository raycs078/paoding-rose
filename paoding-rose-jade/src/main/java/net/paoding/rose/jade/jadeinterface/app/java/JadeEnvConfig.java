package net.paoding.rose.jade.jadeinterface.app.java;

import net.paoding.rose.jade.jadeinterface.app.JadeConfig;

/**
 * 实现用系统环境变量配置 Jade 启动参数。
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public class JadeEnvConfig extends JadeConfig {

    @Override
    public Object getConfig(String param) {

        String config = null;

        try {
            // 首先尝试环境变量
            config = System.getenv(param);
        } catch (SecurityException e) {
            // Do nothing
        }

        // 然后尝试系统属性
        if (config == null) {

            try {
                config = System.getProperty(param);
            } catch (SecurityException e) {
                // Do nothing
            }
        }

        return config;
    }
}
