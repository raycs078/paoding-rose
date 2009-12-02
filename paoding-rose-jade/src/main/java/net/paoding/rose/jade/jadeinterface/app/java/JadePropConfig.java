package net.paoding.rose.jade.jadeinterface.app.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.paoding.rose.jade.jadeinterface.app.JadeConfig;

import org.springframework.core.io.Resource;

/**
 * 实现用 *.properties 配置 Jade 启动参数。
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public class JadePropConfig extends JadeConfig {

    private final Properties props = new Properties();

    public JadePropConfig(Resource prop) throws IOException {
        props.load(prop.getInputStream());
    }

    public JadePropConfig(InputStream in) throws IOException {
        props.load(in);
    }

    @Override
    public Object getConfig(String param) {
        return props.getProperty(param);
    }
}
