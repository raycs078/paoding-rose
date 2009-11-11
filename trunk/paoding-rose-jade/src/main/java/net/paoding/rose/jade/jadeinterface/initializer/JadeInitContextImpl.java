package net.paoding.rose.jade.jadeinterface.initializer;

import java.util.HashMap;
import java.util.Map;

/**
 * 实现 Jade 启动参数的上下文。
 * 
 * @author han.liao
 */
public class JadeInitContextImpl extends JadeInitContext {

    private final Map<String, Object> map;

    public JadeInitContextImpl() {
        this.map = new HashMap<String, Object>();
    }

    public JadeInitContextImpl(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public Object get(String name) {
        return map.get(name);
    }

    @Override
    public void put(String name, Object value) {
        map.put(name, value);
    }
}
