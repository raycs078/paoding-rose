package net.paoding.rose.jade.jadeinterface.initializer;

import java.util.HashMap;

/**
 * 实现 Jade 启动参数的上下文。
 * 
 * @author han.liao
 */
public class JadeInitContextImpl extends JadeInitContext {

    private final HashMap<String, Object> map;

    public JadeInitContextImpl() {
        this.map = new HashMap<String, Object>();
    }

    public JadeInitContextImpl(HashMap<String, Object> map) {
        this.map = map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) map.get(name);
    }

    @Override
    public void put(String name, Object value) {
        map.put(name, value);
    }
}
