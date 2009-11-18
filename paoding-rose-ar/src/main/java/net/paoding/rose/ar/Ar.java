/*
 * Copyright 2007-2009 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.ar;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import net.paoding.rose.ar.init.SessionFactorySetterCallback;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ClassUtils;

/**
 * {@link Ar}底层使用了{@link HibernateTemplate}
 * ，提供对实体对象进行持久化等相关操作的方便的API，他的主要风格就是可以静态地使用。
 * <p>
 * 当使用{@link Ar}的API时，{@link Ar}能够自动从web的根ApplicationContext读取
 * {@link SessionFactory}对象使用。如果要禁止这种自动行为，则应在使用{@link Ar}前，自行调用
 * {@link #setSessionFactory(SessionFactory)}设置{@link SessionFactory}。
 * 
 * @author zhiliang.wang [qieqie.wang@paoding.net]
 */
public class Ar {

    // -------------------------------------------------------

    private static HibernateTemplate hibernateTemplate;

    public static void setSessionFactory(SessionFactory sessionFactory) {
        if (sessionFactory == null) {
            throw new NullPointerException("SessionFactory.");
        }
        hibernateTemplate = new HibernateTemplate(sessionFactory) {

            @Override
            public boolean isCacheQueries() {
                return Ar.isQueryCachable();
            }

            @Override
            public String getQueryCacheRegion() {
                return Ar.CurrentState.getQueryCacheRegion();
            }
        };
    }

    /**
     * @see SessionFactorySetterCallback
     * @return
     */
    public static HibernateTemplate getHibernateTemplate() {
        if (hibernateTemplate == null) {
            throw new NullPointerException("coun't found sessionFactory in applicationContext");
        }
        return hibernateTemplate;
    }

    // -----------------------------------------------

    /**
     * 返回一个和指定实体类相关的一个{@link Ar}上下文，用于在这个上下文环境下进行实体操作。
     * <p>
     * 比如，要取得id为"123"的User实体的一个记录时，使用如下方法:<br>
     * User user = (User) Ar.$(User.class).get("123");
     * 
     * @param entityClass 实体的class类
     * @return
     */
    public static ArEntity of(Class<?> entityClass) {
        ArEntity ar = cactedArEntities.get(entityClass);
        if (ar == null) {
            ar = new ArEntity(entityClass);
            cactedArEntities.put(entityClass, ar);
        }
        return ar;
    }

    // -------------------------------------------------------

    /**
     * @param entity
     * @see Session#lock(Object, LockMode)
     */

    public static void lock(Object entity, LockMode mode) {
        getHibernateTemplate().lock(entity, mode);
    }

    /**
     * @param entity
     * @see Session#save(Object)
     */
    public static Serializable save(Object entity) {
        return getHibernateTemplate().save(entity);
    }

    /**
     * @param entity
     * @see Session#persist(Object)
     */
    public static void persist(Object entity) {
        getHibernateTemplate().persist(entity);
    }

    /**
     * @param entity
     * @see Session#update(Object)
     */
    public static void update(Object entity) {
        getHibernateTemplate().update(entity);
    }

    /**
     * @param entity
     * @see Session#update(Object)
     * @see Session#lock(Object, LockMode)
     */
    public static void update(Object entity, LockMode lockMode) {
        getHibernateTemplate().update(entity, lockMode);
    }

    /**
     * @param entity
     * @see Session#saveOrUpdate(Object)
     */
    public static void saveOrUpdate(Object entity) {
        getHibernateTemplate().saveOrUpdate(entity);
    }

    /**
     * @param entity
     * @see Session#saveOrUpdate(Object)
     */
    public static void saveOrUpdateAll(Collection<?> entities) {
        getHibernateTemplate().saveOrUpdateAll(entities);
    }

    /**
     * @param entity
     * @see Session#merge(Object)
     */
    public static Object merge(Object entity) {
        return getHibernateTemplate().merge(entity);
    }

    /**
     * @param entity
     * @param replicationMode
     * @see Session#replicate(Object, ReplicationMode)
     */
    public static void replicate(Object entity, ReplicationMode replicationMode) {
        getHibernateTemplate().replicate(entity, replicationMode);
    }

    /**
     * @param entity
     * @see Session#delete(Object)
     */
    public static void delete(Object entity) {
        getHibernateTemplate().delete(entity);
    }

    /**
     * @param entity
     * @see Session#lock(Object, LockMode)
     * @see Session#delete(Object)
     */
    public static void delete(Object entity, LockMode lockMode) {
        getHibernateTemplate().delete(entity, lockMode);
    }

    /**
     * @param entities
     * @see Session#delete(Object)
     */
    public static void deleteAll(Collection<?> entities) {
        getHibernateTemplate().deleteAll(entities);
    }

    /**
     * @param entity
     * @see Session#refresh(Object)
     */
    public static void refresh(Object entity) {
        getHibernateTemplate().update(entity);
    }

    /**
     * @param entity
     * @see Session#refresh(Object, LockMode)
     */
    public static void refresh(Object entity, LockMode lockMode) {
        getHibernateTemplate().refresh(entity, lockMode);
    }

    /**
     * @param entity
     * @return
     * @see Session#contains(Object)
     */
    public static boolean contains(Object entity) {
        return getHibernateTemplate().contains(entity);
    }

    /**
     * @see Session#evict(Object)
     */
    public static void evict(Object entity) {
        getHibernateTemplate().evict(entity);
    }

    /**
     * @param entity
     * @see SessionFactory#evict(Class, Serializable)
     */
    public static void evictCache(Object entity) {
        getHibernateTemplate().getSessionFactory().evict(entity.getClass(), getId(entity));
    }

    /**
     * @param entity
     * @param collectionProperty
     * @see SessionFactory#evictCollection(String, Serializable)
     */
    public static void evictCache(Object entity, String collectionProperty) {
        getHibernateTemplate().getSessionFactory().evictCollection(
                entity.getClass().getName() + "." + collectionProperty, getId(entity));
    }

    /**
     * @param entityClass
     * @param collectionProperty
     * @see SessionFactory#evictCollection(String)
     */

    public static void evictCache(Class<?> entityClass, String collectionProperty) {
        getHibernateTemplate().getSessionFactory().evictCollection(
                entityClass.getName() + "." + collectionProperty);
    }

    /**
     * @param entityClass
     * @see SessionFactory#evict(Class)
     */
    public static void evictCache(Class<?> entityClass) {
        getHibernateTemplate().getSessionFactory().evict(entityClass);
    }

    /**
     * @see Session#clear()
     */
    public static void clear() throws DataAccessException {
        getHibernateTemplate().clear();
    }

    /**
     * @see Session#flush()
     */
    public static void flush() {
        getHibernateTemplate().flush();
    }

    // --------------------------------------

    @SuppressWarnings("unchecked")
    public static <T> List<T> find(String queryString, Object... propertyValues) {
        return getHibernateTemplate().find(queryString, propertyValues);
    }

    /**
     * @param action
     * @return
     * @see HibernateTemplate#execute(HibernateCallback)
     */
    public static Object execute(HibernateCallback action) {
        return getHibernateTemplate().execute(action);
    }

    /**
     * @param action
     * @return
     * @see HibernateTemplate#executeFind(HibernateCallback)
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> executeFind(HibernateCallback action) {
        return getHibernateTemplate().executeFind(action);
    }

    // -------------------------------------------------------------------------
    // Convenience finder methods for HQL strings
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public static <T> List<T> findByNamedParam(String queryString, String paramName, Object value) {
        return Ar.getHibernateTemplate().findByNamedParam(queryString, paramName, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> findByNamedParam(String queryString, String[] paramNames,
            Object[] values) {
        return Ar.getHibernateTemplate().findByNamedParam(queryString, paramNames, values);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> findByValueBean(String queryString, Object valueBean) {
        return Ar.getHibernateTemplate().findByValueBean(queryString, valueBean);
    }

    // -------------------------------------------------------------------------
    // Convenience finder methods for named queries
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public static <T> List<T> findByNamedQuery(String queryName, Object... values) {
        return Ar.getHibernateTemplate().findByNamedQuery(queryName, values);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> findByNamedQueryAndNamedParam(String queryName, String paramName,
            Object value) {
        return Ar.getHibernateTemplate().findByNamedQueryAndNamedParam(queryName, paramName, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> findByNamedQueryAndNamedParam(String queryName, String[] paramNames,
            Object[] values) {
        return Ar.getHibernateTemplate().findByNamedQueryAndNamedParam(queryName, paramNames,
                values);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> findByNamedQueryAndValueBean(String queryName, Object valueBean) {
        return Ar.getHibernateTemplate().findByNamedQueryAndValueBean(queryName, valueBean);
    }

    // -------------------------------------------------------------------------
    // Convenience finder methods for detached criteria
    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static <T> List<T> find(DetachedCriteria criteria) {
        return Ar.getHibernateTemplate().findByCriteria(criteria);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> find(DetachedCriteria criteria, int firstResult, int maxResults) {
        return Ar.getHibernateTemplate().findByCriteria(criteria, firstResult, maxResults);
    }

    @SuppressWarnings("unchecked")
    public static <T> T one(DetachedCriteria criteria) {
        return (T) Ar.unique(Ar.getHibernateTemplate().findByCriteria(criteria));
    }

    @SuppressWarnings("unchecked")
    public static <T> T one(DetachedCriteria criteria, int firstResult, int maxResults) {
        return (T) Ar.unique(Ar.getHibernateTemplate().findByCriteria(criteria, firstResult,
                maxResults));
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> findByExample(Object exampleEntity) {
        return Ar.getHibernateTemplate().findByExample(exampleEntity);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> findByExample(Object exampleEntity, int firstResult, int maxResults) {
        return Ar.getHibernateTemplate().findByExample(exampleEntity, firstResult, maxResults);
    }

    public static void closeIterator(Iterator<?> it) {
        Ar.getHibernateTemplate().closeIterator(it);
    }

    public static int bulkUpdate(String queryString, Object... values) {
        return Ar.getHibernateTemplate().bulkUpdate(queryString, values);
    }

    // -------------------------------------------------------

    /**
     * @see Session#setFlushMode(FlushMode)
     */
    public static void setFlushMode(final FlushMode fulshMode) {
        getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.setFlushMode(fulshMode);
                return null;
            }
        });
    }

    /**
     * @see Session#setCacheMode(CacheMode)
     * @param cacheMode
     */
    public static void setCacheMode(final CacheMode cacheMode) {
        getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.setCacheMode(cacheMode);
                return null;
            }
        });
    }

    /**
     * 设置当前线程中，对接下来的查询进行缓存
     * 
     * @param cachable
     */
    public static void enableQueryCachable() {
        CurrentState.enableQueryCachable(true);
    }

    /**
     * 设置当前线程中，对接下来的查询进行缓存
     * 
     * @param cachable
     */
    public static void disableQueryCachable(String queryCacheRegion) {
        CurrentState.enableQueryCachable(queryCacheRegion);
    }

    /**
     * 设置当前线程中，是否对接下来的查询进行缓存
     * 
     * @param cachable
     */
    public static void enableQueryCachable(boolean cachable) {
        CurrentState.enableQueryCachable(cachable);
    }

    /**
     * 设置当前线程中，是否对接下来的查询进行缓存
     * 
     * @param cachable
     * @param queryCacheRegion
     */
    public static void enableQueryCachable(boolean cachable, String queryCacheRegion) {
        CurrentState.enableQueryCachable(cachable, queryCacheRegion);
    }

    /**
     * 取消对当前线程接下来的查询进行缓存
     */
    public static void disableQueryCachable() {
        CurrentState.disableQueryCachable();
    }

    /**
     * 当前线程接下来的查询是否进行缓存
     * 
     * @return
     */
    public static boolean isQueryCachable() {
        return CurrentState.isQueryCachable();
    }

    /**
     * readOnly为true用于设置给定的持久化对象为只读，即它的变更等不会被同步到数据库 Ar.setReadOnly(user,
     * true);
     * 
     * @param entity
     * @param readOnly
     * @see Session#setReadOnly(Object, boolean)
     */
    public static void setReadOnly(final Object entity, final boolean readOnly) {
        getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.setReadOnly(entity, readOnly);
                return null;
            }
        });
    }

    /**
     * 初始化代理对象
     * 
     * @param entity
     * @return
     * @throws DataAccessException
     */
    public static Object initialize(Object proxy) throws DataAccessException {
        Hibernate.initialize(proxy);
        return proxy;
    }

    /**
     * 初始化集合以及集合中的元素
     * 
     * @param collection
     * @return
     */
    public static Collection<?> initializeCollection(Collection<?> collection) {
        for (Iterator<?> it = collection.iterator(); it.hasNext();) {
            Hibernate.initialize(it.next());
        }
        return collection;
    }

    // -------

    @SuppressWarnings("unchecked")
    public static List<Object[]> sql(final String sql, final Object... values) {
        final HibernateTemplate ht = Ar.getHibernateTemplate();
        return (List<Object[]>) ht.execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                SQLQuery queryObject = session.createSQLQuery(sql);
                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        queryObject.setParameter(i, values[i]);
                    }
                }
                return queryObject.list();
            }
        });
    }

    public static int exesql(final String sql, final Object... values) {
        final HibernateTemplate ht = Ar.getHibernateTemplate();
        return (Integer) ht.execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                SQLQuery queryObject = session.createSQLQuery(sql);
                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        queryObject.setParameter(i, values[i]);
                    }
                }
                return queryObject.executeUpdate();
            }
        });
    }

    public static <T> List<T> sql(final String sql, Class<T> entityClass, final Object... values) {
        return sql(sql, (String) null, entityClass, values);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> sql(final String sql, final String alias, final Class<?> entityClass,
            final Object... values) {
        final HibernateTemplate ht = Ar.getHibernateTemplate();
        return (List<T>) ht.execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                SQLQuery queryObject = session.createSQLQuery(sql);
                if (alias == null) {
                    queryObject.addEntity(entityClass);
                } else {
                    queryObject.addEntity(alias, entityClass);
                }
                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        queryObject.setParameter(i, values[i]);
                    }
                }
                return queryObject.list();
            }
        });
    }

    // -------------------------------------------------------

    /**
     * 返回第一个对象，list长度==0时，返回null
     * 
     * @param list
     * @return
     */
    static <T> T unique(List<T> list) {
        return list.size() == 0 ? null : list.get(0);
    }

    /**
     * 返回list的第一个对象，如果list长度为0或大于1，
     * 则抛出IncorrectResultSizeDataAccessException异常
     * 
     * @param list
     * @return
     */
    static <T> T requiredUnique(List<T> list) {
        if (list.size() != 1) {
            throw new IncorrectResultSizeDataAccessException(1, list.size());
        }
        return list.get(0);
    }

    // -----------------------

    static Method idMethod(Class<?> entityClass) {
        Method m = idMethods.get(entityClass);
        if (m == null) {
            Method[] ms = entityClass.getMethods();
            for (int i = 0; i < ms.length; i++) {
                if (ms[i].getAnnotation(Id.class) != null) {
                    m = ms[i];
                    idMethods.put(entityClass, m);
                    break;
                }
            }
            if (m == null) {
                throw new InvalidDataAccessApiUsageException(
                        "not found the id annotation getter method.");
            }
        }
        return m;
    }

    public static Serializable getId(Object entity) {
        if (entity instanceof HibernateProxy) {
            return ((HibernateProxy) entity).getHibernateLazyInitializer().getIdentifier();
        }
        Method m = idMethod(entity.getClass());
        Serializable id;
        try {
            id = (Serializable) m.invoke(entity);
        } catch (IllegalArgumentException e1) {
            throw new IllegalArgumentException("fail to get the id of object: " + entity, e1);
        } catch (IllegalAccessException e1) {
            throw new IllegalArgumentException("fail to get the id of object: " + entity, e1);
        } catch (InvocationTargetException e1) {
            throw new IllegalArgumentException("fail to get the id of object: " + entity, e1);
        }
        return id;
    }

    public static void setId(Object entity, Serializable s) {
        Method gm = idMethod(entity.getClass());
        Method sm = null;
        try {
            sm = entity.getClass().getMethod(gm.getName().replaceFirst("get", "set"), s.getClass());
            sm.invoke(entity, s);
        } catch (SecurityException e1) {
            throw new IllegalArgumentException("fail to set the id of object: " + entity, e1);
        } catch (NoSuchMethodException e1) {
            throw new IllegalArgumentException("fail to set the id of object: " + entity, e1);
        } catch (IllegalArgumentException e1) {
            throw new IllegalArgumentException("fail to set the id of object: " + entity, e1);
        } catch (IllegalAccessException e1) {
            throw new IllegalArgumentException("fail to set the id of object: " + entity, e1);
        } catch (InvocationTargetException e1) {
            throw new IllegalArgumentException("fail to set the id of object: " + entity, e1);
        }
    }

    /**
     * 比较两个实体实例是否是相同的
     * 
     * @param thisObj
     * @param thatObj
     * @return
     */
    public static boolean equals(Object thisObj, Object thatObj) {
        if (thisObj == null || thatObj == null) return false;
        if (thisObj == thatObj) {
            return true;
        }
        if (thisObj.getClass() != thatObj.getClass()) {
            String thisClassName = thisObj.getClass().getName();
            String thatClassName = thatObj.getClass().getName();
            int thisObjectIndex = thisClassName.indexOf("$");
            if (thisObjectIndex > 0) {
                thisClassName = thisClassName.substring(0, thisObjectIndex);
            }
            int thatIndex = thatClassName.indexOf("$");
            if (thatIndex > 0) {
                thatClassName = thatClassName.substring(0, thatIndex);
            }
            if (!thisClassName.equals(thatClassName)) {
                try {
                    Class<?> thisClass = Class.forName(thisClassName);
                    Class<?> thatClass = Class.forName(thatClassName);
                    if (!ClassUtils.isAssignable(thisClass, thatClass)
                            && !ClassUtils.isAssignable(thatClass, thisClass)) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }
        Serializable id = getId(thisObj);
        if (id == null) return false;
        return id.equals(getId(thatObj));
    }

    /**
     * 可被复用的{@link ArEntity}实例
     */
    private static transient Map<Class<?>, ArEntity> cactedArEntities = new HashMap<Class<?>, ArEntity>();

    /**
     * Cache找到的实体主键get方法
     */
    private static transient Map<Class<?>, Method> idMethods = new HashMap<Class<?>, Method>();

    public static class CurrentState {

        public final static String QUERY_CACHE_REGION = "paoding.ar.queryCacheRegion";

        public static void disableQueryCachable() {
            enableQueryCachable(false, null);
        }

        public static void enableQueryCachable() {
            enableQueryCachable(true, null);
        }

        public static void enableQueryCachable(String queryCacheRegion) {
            enableQueryCachable(true, queryCacheRegion);
        }

        public static void enableQueryCachable(boolean cachable) {
            enableQueryCachable(cachable, null);
        }

        /**
         * @param cachable
         * @param queryCacheRegion
         */
        public static void enableQueryCachable(boolean cachable, String queryCacheRegion) {
            if (cachable) {
                if (queryCacheRegion == null) {
                    queryCacheRegion = "";
                }
                if (!TransactionSynchronizationManager.hasResource(QUERY_CACHE_REGION)) {
                    TransactionSynchronizationManager.bindResource(QUERY_CACHE_REGION,
                            queryCacheRegion);
                }
            } else {
                TransactionSynchronizationManager.unbindResource(QUERY_CACHE_REGION);
            }
        }

        public static boolean isQueryCachable() {
            return getQueryCacheRegion() != null;
        }

        public static String getQueryCacheRegion() {
            return (String) TransactionSynchronizationManager.getResource(QUERY_CACHE_REGION);
        }
    }

}
