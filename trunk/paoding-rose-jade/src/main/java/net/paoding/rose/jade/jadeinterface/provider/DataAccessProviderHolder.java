package net.paoding.rose.jade.jadeinterface.provider;

import javax.sql.DataSource;

public class DataAccessProviderHolder implements DataAccessProvider {

    private static DataAccessProvider provider;

    public static void setProvider(DataAccessProvider provider) {
        DataAccessProviderHolder.provider = provider;
    }

    public static DataAccessProvider getProvider() {
        return provider;
    }

    @Override
    public DataAccess createDataAccess(DataSource dataSource) {
        return provider.createDataAccess(dataSource);
    }
}
