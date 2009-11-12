package net.paoding.rose.jade.jadeinterface.impl;

/**
 * 封装一对 Key-Value 值。
 * 
 * @author han.liao
 */
public class KeyValuePair {

    private final Object key, value;

    public KeyValuePair(Object key, Object value) {
        this.value = value;
        this.key = key;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "KeyValuePair [key=" + key + ", value=" + value + "]";
    }
}
