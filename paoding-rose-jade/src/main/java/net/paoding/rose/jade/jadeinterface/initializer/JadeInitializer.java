package net.paoding.rose.jade.jadeinterface.initializer;

import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProviderHolder;

public class JadeInitializer {

    public static void initialize(DataAccessProvider provider) {
        DataAccessProviderHolder.setProvider(provider);
    }

    public static void initialize(String providerClassName) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {

        if (DataAccessProviderHolder.getProvider() != null) {
            throw new IllegalStateException("DataAccessProviderHolder");
        }

        Class<?> providerClass = Class.forName(providerClassName);
        DataAccessProvider provider = (DataAccessProvider) providerClass.newInstance();
        DataAccessProviderHolder.setProvider(provider);
    }
}
