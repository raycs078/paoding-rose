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

        try {
            return System.getenv(param);

        } catch (SecurityException e) {

            // 忽略权限异常
            return null;
        }
    }
}
