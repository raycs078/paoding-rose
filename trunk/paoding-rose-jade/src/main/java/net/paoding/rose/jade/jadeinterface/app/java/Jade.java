package net.paoding.rose.jade.jadeinterface.app.java;

import java.util.concurrent.ConcurrentHashMap;

import net.paoding.rose.jade.jadeinterface.annotation.Dao;
import net.paoding.rose.jade.jadeinterface.cache.CacheProvider;
import net.paoding.rose.jade.jadeinterface.impl.DaoFactoryBean;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;
import net.paoding.rose.jade.jadeinterface.provider.cache.CacheDataAccess;

public class Jade {

    @SuppressWarnings("unchecked")
    protected static ConcurrentHashMap<Class, DaoFactoryBean> mapDao = new ConcurrentHashMap<Class, DaoFactoryBean>();

    protected DataAccessProvider dataAccessProvider;

    protected CacheProvider cacheProvider;

    public Jade() {
        super();
    }

    public Jade(DataAccessProvider dataAccessProvider) {
        this.dataAccessProvider = dataAccessProvider;
    }

    public Jade(DataAccessProvider dataAccessProvider, CacheProvider cacheProvider) {
        this.dataAccessProvider = dataAccessProvider;
        this.cacheProvider = cacheProvider;
    }

    public DataAccessProvider getDataAccessProvider() {
        return dataAccessProvider;
    }

    public void setDataAccessProvider(DataAccessProvider dataAccessProvider) {
        this.dataAccessProvider = dataAccessProvider;
    }

    public CacheProvider getCacheProvider() {
        return cacheProvider;
    }

    public void setCacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    /**
     * 获取指定类型的 DAO 对象。
     * 
     * @param <T> - DAO 对象的类型
     * 
     * @param daoClass - DAO 对象的类
     * 
     * @return DAO 对象
     */
    public <T> T getDao(Class<T> daoClass) {

        // 获取缓存的  DaoFactoryBean<T>
        @SuppressWarnings("unchecked")
        DaoFactoryBean<T> factoryBean = mapDao.get(daoClass);
        if (factoryBean == null) {

            // 检查是否符合规则
            if (!daoClass.isInterface()) {
                throw new IllegalArgumentException("daoClass should be a interface");
            }

            Dao annotation = daoClass.getAnnotation(Dao.class);
            if (annotation == null) {
                throw new IllegalArgumentException("not @Dao annotated ");
            }

            // 创建  DaoFactoryBean<T>
            factoryBean = new DaoFactoryBean<T>();
            factoryBean.setDaoClass(daoClass);
            factoryBean.setDataAccessProvider(new DataAccessProvider() {

                @Override
                public DataAccess createDataAccess(String dataSourceName) {

                    if (cacheProvider != null) {
                        // 含缓存逻辑的  DataAccess
                        return new CacheDataAccess( // NL
                                dataAccessProvider.createDataAccess(dataSourceName), // NL
                                cacheProvider);
                    } else {
                        // 返回原始的  DataAccess
                        return dataAccessProvider.createDataAccess(dataSourceName);
                    }
                }
            });
            factoryBean.afterPropertiesSet();

            // 缓存创建的  DaoFactoryBean<T>
            mapDao.putIfAbsent(daoClass, factoryBean);
        }

        // 获取  DAO 对象
        return factoryBean.getObject();
    }
}
