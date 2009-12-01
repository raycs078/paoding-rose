package net.paoding.rose.jade.jadeinterface.app;

import java.util.HashMap;
import java.util.Map;

/**
 * 实现 Jade 配置参数。
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public class JadeConfigImpl extends JadeConfig {

    private final HashMap<String, Object> map = new HashMap<String, Object>();

    public JadeConfigImpl() {
    }

    public JadeConfigImpl(Map<String, Object> map) {
        this.map.putAll(map);
    }

    /**
     * 设置 Jade 配置参数。
     * 
     * @param name - 参数名称
     * @param value - 参数内容
     */
    public void setConfig(String name, Object value) {
        map.put(name, value);
    }

    @Override
    public Object getConfig(String name) {
        return map.get(name);
    }
}
