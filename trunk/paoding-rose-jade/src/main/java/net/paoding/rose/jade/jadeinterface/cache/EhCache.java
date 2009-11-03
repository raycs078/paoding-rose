package net.paoding.rose.jade.jadeinterface.cache;

import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 提供 Ehcache 缓存池的 {@link Cache} 实现。
 * 
 * @author han.liao
 */
public class EhCache implements Cache {

    private static Log logger = LogFactory.getLog(EhCache.class);

    private net.sf.ehcache.Cache cache;

    public EhCache(net.sf.ehcache.Cache cache) {
        this.cache = cache;
    }

    @Override
    public Object get(String key) {

        Object value = null;

        Element element = cache.get(key);
        if (element != null) {
            value = element.getObjectValue();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Get cache \'" + key + "\' from pool \'" + cache.getName() + "\': "
                    + value);
        }

        return value;
    }

    @Override
    public boolean set(String key, Object value, int expiry) {

        Element element = new Element(key, value, expiry);

        cache.put(element);

        if (logger.isDebugEnabled()) {
            logger.debug("Set cache \'" + key + "\' from pool \'" + cache.getName() + "\': "
                    + value);
        }

        return true;
    }

    @Override
    public boolean delete(String key) {

        boolean deleted = cache.remove(key);

        if (logger.isDebugEnabled()) {
            logger.debug("[" + Boolean.toString(deleted) + "] Delete cache \'" + key
                    + "\' from pool \'" + cache.getName() + "\'.");
        }

        return deleted;
    }
}
