package net.paoding.rose.jade.jadeinterface.provider;

import java.util.Collections;
import java.util.Map;

import net.paoding.rose.jade.jadeinterface.impl.GenericUtils;

/**
 * 提供 Definition 包装对 DAO 的定义。
 * 
 * @author han.liao[in355hz@gmail.com]
 */
public class Definition {

    private final Class<?> clazz;

    private final Map<String, ?> constants;

    public Definition(Class<?> clazz) {
        this.clazz = clazz;
        this.constants = Collections.unmodifiableMap( // NL
                GenericUtils.getConstantFrom(clazz, true, true));
    }

    public String getName() {
        return clazz.getName();
    }

    public Map<String, ?> getConstants() {
        return constants;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Definition) {
            Definition definition = (Definition) obj;
            return clazz.equals(definition.clazz);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return clazz.hashCode() * 13;
    }

    @Override
    public String toString() {
        return clazz.getName();
    }
}
