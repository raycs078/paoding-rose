package net.paoding.rose.jade.jadeinterface.app;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.paoding.rose.jade.jadeinterface.cache.MockCacheProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.cache.CacheDataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.exql.ExqlDataAccessProvider;

public class JadeConfigResolverTests extends TestCase {

    public void testResolve() {

        JadeConfigImpl config = new JadeConfigImpl();

        config.setConfig(JadeConfig.DATA_ACCESS_PROVIDER, // NL
                new ExqlDataAccessProvider());

        DataAccessProvider dataAccessProvider = JadeConfigResolver.configResolve(config);

        Assert.assertNotNull(dataAccessProvider);
        Assert.assertEquals(ExqlDataAccessProvider.class, // NL
                dataAccessProvider.getClass());
    }

    public void testResolveWithCache() {

        JadeConfigImpl config = new JadeConfigImpl();

        config.setConfig(JadeConfig.DATA_ACCESS_PROVIDER, // NL
                new ExqlDataAccessProvider());
        config.setConfig(JadeConfig.CACHE_PROVIDER, // NL
                new MockCacheProvider());

        DataAccessProvider dataAccessProvider = JadeConfigResolver.configResolve(config);

        Assert.assertNotNull(dataAccessProvider);
        Assert.assertEquals(CacheDataAccessProvider.class, // NL
                dataAccessProvider.getClass());
    }

    public void testResolveWithClass() {

        JadeConfigImpl config = new JadeConfigImpl();

        config.setConfig(JadeConfig.DATA_ACCESS_PROVIDER_CLASS, // NL
                ExqlDataAccessProvider.class);

        DataAccessProvider dataAccessProvider = JadeConfigResolver.configResolve(config);

        Assert.assertNotNull(dataAccessProvider);
        Assert.assertEquals(ExqlDataAccessProvider.class, // NL
                dataAccessProvider.getClass());
    }

    public void testResolveWithCacheAndClass() {

        JadeConfigImpl config = new JadeConfigImpl();

        config.setConfig(JadeConfig.DATA_ACCESS_PROVIDER_CLASS, // NL
                ExqlDataAccessProvider.class);
        config.setConfig(JadeConfig.CACHE_PROVIDER_CLASS, // NL
                MockCacheProvider.class);

        DataAccessProvider dataAccessProvider = JadeConfigResolver.configResolve(config);

        Assert.assertNotNull(dataAccessProvider);
        Assert.assertEquals(CacheDataAccessProvider.class, // NL
                dataAccessProvider.getClass());
    }

    public void testResolveWithName() {

        JadeConfigImpl config = new JadeConfigImpl();

        config.setConfig(JadeConfig.DATA_ACCESS_PROVIDER_CLASS, // NL
                "net.paoding.rose.jade.jadeinterface.provider.exql.ExqlDataAccessProvider");

        DataAccessProvider dataAccessProvider = JadeConfigResolver.configResolve(config);

        Assert.assertNotNull(dataAccessProvider);
        Assert.assertEquals(ExqlDataAccessProvider.class, // NL
                dataAccessProvider.getClass());
    }

    public void testResolveWithCacheAndName() {

        JadeConfigImpl config = new JadeConfigImpl();

        config.setConfig(JadeConfig.DATA_ACCESS_PROVIDER_CLASS, // NL
                "net.paoding.rose.jade.jadeinterface.provider.exql.ExqlDataAccessProvider");
        config.setConfig(JadeConfig.CACHE_PROVIDER_CLASS, // NL
                "net.paoding.rose.jade.jadeinterface.cache.MockCacheProvider");

        DataAccessProvider dataAccessProvider = JadeConfigResolver.configResolve(config);

        Assert.assertNotNull(dataAccessProvider);
        Assert.assertEquals(CacheDataAccessProvider.class, // NL
                dataAccessProvider.getClass());
    }
}
