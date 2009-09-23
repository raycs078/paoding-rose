/*
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.ar;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.ClassUtils;

/**
 * 
 * @author zhiliang.wang [qieqie.wang@paoding.net]
 * 
 */
public class ArEntity {

	/**
	 * 具体的实体对象Class类。比如User.class,Topic.class等
	 */
	private transient Class<?> entityClass;
	private transient String entityClassSimpleName;

	/**
	 * 仅被{@link Ar#$}使用的构造函数
	 */
	ArEntity(Class<?> entityClass) {
		this.entityClass = entityClass;
		this.entityClassSimpleName = this.entityClass.getSimpleName();
	}

	/**
	 * 用于子类扩展时使用，使得子类的对象本身就是一个{@link ArEntity}实例
	 */
	protected ArEntity() {
		this.entityClass = this.getClass();
		this.entityClassSimpleName = this.entityClass.getSimpleName();
	}

	// ---

	/**
	 * 当一个实体直接或间接扩展于{@link ArEntity}，调用本方法用于使他初始化
	 * 
	 * @see HibernateTemplate#initialize(Object)
	 */
	public ArEntity initialize() {
		Ar.getHibernateTemplate().initialize(this);
		return this;
	}

	/**
	 * 当一个实体直接或间接扩展于{@link ArEntity}，调用本方法可设置该实体在flush的时候不再检查dirty。
	 * 
	 * @see Session#setReadOnly(Object, boolean)
	 */
	public void setReadOnly() {
		this.setReadOnly(true);
	}

	/**
	 * 当一个实体直接或间接扩展于{@link ArEntity}，调用本方法可设置该实体在flush的时候是否检查dirty。
	 * 
	 * @param readOnly
	 * @see Session#setReadOnly(Object, boolean)
	 */
	public void setReadOnly(boolean readOnly) {
		if (ClassUtils.isAssignable(entityClass, this.getClass())) {
			throw new IllegalArgumentException("setReadOnly should be only"
					+ " invoked to an entity object");
		}
		Ar.setReadOnly(this, readOnly);
	}

	/**
	 * 返回本实体类的记录总个数
	 */
	public int count() {
		HibernateCallback callback = new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Criteria crit = session.createCriteria(entityClass);
				crit.setProjection(Projections.rowCount());
				Number number = (Number) crit.uniqueResult();
				return number.intValue();
			}
		};
		return (Integer) Ar.execute(callback);
	}

	/**
	 * 返回符合指定条件的本实体类的记录个数
	 * 
	 * @param smartString
	 *            "createTime>? and name=?" or "createTime, name"
	 * @param propertyValues
	 *            条件中参数的值，如createTime, name的值
	 * @return
	 */
	public int count(String smartString, Object... propertyValues) {
		StringBuilder sb = new StringBuilder("select count(*) from ");
		sb.append(entityClassSimpleName).append(" where ");
		if (smartString.indexOf('?') == -1) {
			String[] propertyNames = smartString.split(",");
			for (int i = 0; i < propertyNames.length; i++) {
				if (i != 0) {
					sb.append(" and ");
				}
				sb.append(propertyNames[i]).append("=?");
			}
		} else {
			sb.append(smartString);
		}
		return ((Number) Ar.unique(Ar.find(sb.toString(), propertyValues)))
				.intValue();
	}

	/**
	 * 返回指定标识值的实体对象，数据库不存在时则返回null。
	 * 
	 * @param id
	 *            对象的标识值
	 * @param lock
	 *            为true表示要求对该行进行数据库级别的锁定(悲观锁)，直到本事务提交，一般用于可能存在并发的更新数据获取
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Serializable id, boolean lock) {
		if (lock)
			return (T) get(id, LockMode.UPGRADE);
		else
			return (T) get(id);
	}

	/**
	 * 使用实体名映射实体时，用此方法返回指定实体的对象，数据库不存在时则返回null。<br>
	 * 此方法很少用。
	 * 
	 * @param id
	 *            对象的标识值
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Serializable id) {
		return (T) Ar.getHibernateTemplate().get(entityClass, id);
	}

	/**
	 * 返回指定标识值的实体对象，并应用给定的LockMode，数据库不存在时则返回null。
	 * 
	 * @param id
	 *            对象的标识值
	 * @param lockMode
	 *            请参考Hibernate LockMode 类中各种
	 *            LockMode(NONE,READ,UPGRADE,WRITE,FORCE) 的定义
	 * 
	 * @return
	 */

	@SuppressWarnings("unchecked")
	public <T> T get(Serializable id, LockMode lockMode) {
		return (T) Ar.getHibernateTemplate().get(entityClass, id, lockMode);
	}

	/**
	 * 返回数据库中存在的指定标识值的实体对象。一般情况下，本方法立即返回一个代理类，不会直接访问数据库获取该数据。 <br>
	 * 但当读取该对象的属性时，则该本实体会自动从数据库读出数据。如果此时数据库对象的纪录不存在将抛出异常。
	 * 
	 * @param id
	 *            对象的标识值
	 * @param lock
	 *            为true表示要求对该行进行数据库级别的锁定(悲观锁)，直到本事务提交，一般用于可能存在并发的更新数据获取
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T load(Serializable id, boolean lock) {
		if (lock)
			return (T) load(id, LockMode.UPGRADE);
		else
			return (T) load(id);
	}

	/**
	 * 返回数据库中存在的指定标识值的实体对象。一般情况下，本方法立即返回一个代理类，不会直接访问数据库获取该数据。 <br>
	 * 但当读取该对象的属性时，则该本实体会自动从数据库读出数据。如果此时数据库对象的纪录不存在将抛出异常。
	 * 
	 * @param id
	 *            对象的标识值
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T load(Serializable id) {
		return (T) Ar.getHibernateTemplate().load(entityClass, id);
	}

	/**
	 * 返回数据库中存在的指定标识值的实体对象。一般情况下，本方法立即返回一个代理类，不会直接访问数据库获取该数据。 <br>
	 * 但当读取该对象的属性时，则该本实体会自动从数据库读出数据。如果此时数据库对象的纪录不存在将抛出异常。
	 * 
	 * @param id
	 *            对象的标识值
	 * 
	 * 
	 * @param lockMode
	 *            请参考Hibernate LockMode 类中各种
	 *            LockMode(NONE,READ,UPGRADE,WRITE,FORCE) 的定义
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T load(Serializable id, LockMode lockMode) {
		return (T) Ar.getHibernateTemplate().load(entityClass, id, lockMode);
	}

	/**
	 * 从数据库读取本对象的数据值，填充到此类中，并把本类加入Hibernate Session管理，使得可以继续导航到本对象引用的其他对象。
	 * <p>
	 * 本方法区别于其他get/load系统的方法的是，本方法不返回一个另外创建的对象，而是将本对象加入Hibenate
	 * Session管理，而且在一个Session范围中中，一个对象只能调用本方法一次
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T load() {
		Ar.getHibernateTemplate().load(this, Ar.getId(this));
		return (T) this;
	}

	/**
	 * 返回符合指定条件的第一个对象
	 * 
	 * @param smartString
	 *            "logonName=? and password=?" 或者 "logonName,password"
	 *            后者用逗号分割表示参数列表，并且是“与”的关系
	 * @param propertyValues
	 *            条件中参数的值，如logonName和password的值
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T one(String smartString, Object... values) {
		return (T) Ar.unique(find(smartString, values));
	}

	/**
	 * 返回符合条件的所有对象
	 * 
	 * @param smartString
	 *            "(gender=? and age < ?) or (age < ? or gender=?)"
	 * @param values
	 *            条件中参数的值，例子中的总共需要4个参数，分别是：姓名，年龄，年龄，性别
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> find(String smartString, Object... values) {
		if (smartString.indexOf("from ") == -1) {
			StringBuilder sb = new StringBuilder();
			sb.append("from ").append(entityClassSimpleName).append(" where ");
			if (smartString.indexOf('?') == -1) {
				String[] propertyNames = smartString.split(",");
				for (int i = 0; i < propertyNames.length; i++) {
					if (i != 0) {
						sb.append(" and ");
					}
					sb.append(propertyNames[i]).append("=?");
				}
			} else {
				sb.append(smartString);
			}
			return Ar.getHibernateTemplate().find(sb.toString(), values);
		} else {
			return Ar.getHibernateTemplate().find(smartString, values);
		}
	}

	// ----------------------------------------

	/**
	 * 返回实体的所有对象
	 */
	public <T> List<T> find() {
		return find(new Criterion[0]);
	}

	/**
	 * 返回第一个记录
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T one() {
		return (T) Ar.unique(find(0, 1));
	}

	/**
	 * 返回从指定位置开始的若干个实体对象
	 * 
	 * @param firstResult
	 *            the first result to retrieve, numbered from <tt>0</tt>
	 * @param rowCount
	 *            the maximum number of results
	 * @return
	 */
	public <T> List<T> find(int firstResult, int rowCount) {
		return find(firstResult, rowCount, null);
	}

	/**
	 * 返回第firstResult个记录
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T one(int firstResult) {
		return (T) Ar.unique(find(firstResult, 1));
	}

	/**
	 * 返回符合条件的记录数目
	 * 
	 * @param criterions
	 * @return
	 */
	public int count(Criterion... criterions) {
		DetachedCriteria detachedCrit = DetachedCriteria.forClass(entityClass);
		detachedCrit.setProjection(Projections.rowCount());
		for (Criterion c : criterions) {
			if (c != null) {
				detachedCrit.add(c);
			}
		}
		return ((Number) Ar.one(detachedCrit)).intValue();
	}

	/**
	 * 使用不定个数的、组合条件查询实体对象，并返回。
	 * <p>
	 * 当简单的find方法无法满足时，本方法可构造复杂的查询条件。
	 * 
	 * @param criterions
	 *            由Restrictions类的静态方法创建，Restrictions包含了常见的关系判断、逻辑判断、like判断等，比如：<br>
	 *            Restrictions.eq("logonName", "wangzhiliang") 表示登录名为
	 *            "wangzhiliang" 的条件
	 * @return
	 */
	public <T> List<T> find(Criterion... criterions) {
		DetachedCriteria detachedCrit = DetachedCriteria.forClass(entityClass);
		for (Criterion c : criterions) {
			if (c != null) {
				detachedCrit.add(c);
			}
		}
		return Ar.find(detachedCrit);
	}

	/**
	 * 找到符合条件的第1个记录
	 * 
	 * @param criterions
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T one(Criterion... criterions) {
		return (T) Ar.unique(find(0, 1, null, criterions));
	}

	/**
	 * 返回满足条件的，按指定排序规则排序的实体对象
	 * 
	 * @param order
	 *            排序规则，由Order类提供的静态方法构建，null时表示不指定规则
	 * @param criterions
	 *            由Restrictions类的静态方法创建，Restrictions包含了常见的关系判断、逻辑判断、like判断等，比如：<br>
	 *            Restrictions.eq("logonName", "wangzhiliang") 表示登录名为
	 *            "wangzhiliang" 的条件
	 * @return
	 */
	public <T> List<T> find(Order order, Criterion... criterions) {
		return find(new Order[] { order }, criterions);
	}

	/**
	 * 找到符合条件的第1个记录
	 * 
	 * @param criterions
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T one(Order order, Criterion... criterions) {
		return (T) Ar.unique(find(0, 1, order, criterions));
	}

	/**
	 * 返回满足条件的，按指定排序规则排序的实体对象
	 * 
	 * @param order1
	 * @param order2
	 *            排序规则，由Order类提供的静态方法构建，null时表示不指定规则
	 * @param criterions
	 *            由Restrictions类的静态方法创建，Restrictions包含了常见的关系判断、逻辑判断、like判断等，比如：<br>
	 *            Restrictions.eq("logonName", "wangzhiliang") 表示登录名为
	 *            "wangzhiliang" 的条件
	 * @return
	 */
	public <T> List<T> find(Order order1, Order order2, Criterion... criterions) {
		return find(new Order[] { order1, order2 }, criterions);
	}

	/**
	 * 返回满足条件的，按指定排序规则排序的实体对象
	 * 
	 * @param orders
	 *            排序规则，由Order类提供的静态方法构建，null时表示不指定规则
	 * @param criterions
	 *            由Restrictions类的静态方法创建，Restrictions包含了常见的关系判断、逻辑判断、like判断等，比如：<br>
	 *            Restrictions.eq("logonName", "wangzhiliang") 表示登录名为
	 *            "wangzhiliang" 的条件
	 * @return
	 */
	public <T> List<T> find(Order[] orders, Criterion... criterions) {
		DetachedCriteria detachedCrit = DetachedCriteria.forClass(entityClass);
		for (Criterion c : criterions) {
			if (c != null) {
				detachedCrit.add(c);
			}
		}
		if (orders != null) {
			for (Order order : orders) {
				detachedCrit.addOrder(order);
			}
		}
		return Ar.find(detachedCrit);
	}

	@SuppressWarnings("unchecked")
	public <T> T one(Order[] orders, Criterion... criterions) {
		return (T) Ar.unique(find(orders, criterions));
	}

	/**
	 * 返回满足条件的，按指定排序规则排序的，从指定位置开始的若干个实体对象
	 * 
	 * @param firstResult
	 *            the first result to retrieve, numbered from <tt>0</tt>
	 * @param rowCount
	 *            the maximum number of results
	 * @param order
	 *            排序规则，由Order类提供的静态方法构建，null时表示不指定规则
	 * @param criterions
	 *            由Restrictions类的静态方法创建，Restrictions包含了常见的关系判断、逻辑判断、like判断等，比如：<br>
	 *            Restrictions.eq("logonName", "wangzhiliang") 表示登录名为
	 *            "wangzhiliang" 的条件
	 * @return
	 */
	public <T> List<T> find(int firstResult, int rowCount, Order order,
			Criterion... criterions) {
		DetachedCriteria detachedCrit = DetachedCriteria.forClass(entityClass);

		for (Criterion c : criterions) {
			if (c != null) {
				detachedCrit.add(c);
			}
		}
		if (order != null)
			detachedCrit.addOrder(order);
		return Ar.find(detachedCrit, firstResult, rowCount);
	}

	// -------------------------------------------------------------------------
	// Convenience methods for storing individual objects
	// -------------------------------------------------------------------------

	public void lock(LockMode lockMode) {
		Ar.lock(this, lockMode);
	}

	/**
	 * Persist this transient instance, first assigning a generated identifier.
	 * (Or using the current value of the identifier property if the
	 * <tt>assigned</tt> generator is used.) This operation cascades to
	 * associated instances if the association is mapped with
	 * <tt>cascade="save-update"</tt>.
	 * 
	 * @param object
	 *            a transient instance of a persistent class
	 * @return the generated identifier
	 * @throws HibernateException
	 */
	public Serializable save() {
		return Ar.save(this);
	}

	/**
	 * Make this transient instance persistent. This operation cascades to
	 * associated instances if the association is mapped with
	 * <tt>cascade="persist"</tt>.<br>
	 * <br>
	 * The semantics of this method are defined by JSR-220.
	 * 
	 * @param object
	 *            a transient instance to be made persistent
	 */
	public void persist() {
		Ar.persist(this);
	}

	/**
	 * Update this persistent instance with the identifier of the given detached
	 * instance. If there is a persistent instance with the same identifier, an
	 * exception is thrown. This operation cascades to associated instances if
	 * the association is mapped with <tt>cascade="save-update"</tt>.
	 * 
	 * @param object
	 *            a detached instance containing updated state
	 * @throws HibernateException
	 */
	public void update() {
		Ar.update(this);
	}

	public void update(LockMode lockMode) {
		Ar.update(this, lockMode);
	}

	/**
	 * 
	 * @see Session#saveOrUpdate(<T> T )
	 */
	public void saveOrUpdate() {
		Ar.saveOrUpdate(this);
	}

	/**
	 * 
	 * @param replicationMode
	 * @see Session#replicate(<T> T , ReplicationMode)
	 */
	public void replicate(ReplicationMode replicationMode) {
		Ar.replicate(this, replicationMode);
	}

	/**
	 * @see Session#merge(Object )
	 */
	@SuppressWarnings("unchecked")
	public <T> T merge() {
		return (T) Ar.merge(this);
	}

	/**
	 * @see Session#delete(Object )
	 */
	public void delete() {
		Ar.delete(this);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> findByValueBean(String queryString) {
		return Ar.getHibernateTemplate().findByValueBean(queryString, this);
	}

	public <T> List<T> findByExample() {
		return Ar.findByExample(this);
	}

	public <T> List<T> findByExample(int firstResult, int maxResults) {
		return Ar.findByExample(this, firstResult, maxResults);
	}

	public <T> List<T> findByNamedQueryAndValueBean(String queryName) {
		return Ar.findByNamedQueryAndValueBean(queryName, this);
	}

	/**
	 * @see Session#lock(<T> T , LockMode)
	 * @see Session#delete(Object )
	 */

	public void delete(LockMode lockMode) {
		Ar.delete(this, lockMode);
	}

	/**
	 * 
	 * @see Session#refresh(Object )
	 */
	public void refresh() {
		Ar.refresh(this);
	}

	/**
	 * @see Session#refresh(<T> T , LockMode))
	 * 
	 */
	public void refresh(LockMode lockMode) {
		Ar.refresh(this, lockMode);
	}

	/**
	 * 
	 * @return
	 * @see Session#contains(Object )
	 */
	public boolean contains() {
		return Ar.contains(this);
	}

	/**
	 * 
	 * @see Session#evict(Object )
	 */
	public void evict() {
		Ar.evict(this);
	}

	/**
	 * @see SessionFactory#evict(Class, Serializable)
	 */
	public void evictCache() {
		Ar.evictCache(this);
	}

	/**
	 * @see SessionFactory#evict(Class, Serializable)
	 */
	public void evictCache(String collectionProperty) {
		Ar.evictCache(this, collectionProperty);
	}

	// -------------------------------------------------------------------------
	// Convenience query methods for iteration and bulk updates/deletes
	// -------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public <T> Iterator<T> iterate(String smartString, Object... values) {
		if (smartString.indexOf("from ") == -1) {
			StringBuilder sb = new StringBuilder();
			sb.append("from ").append(entityClassSimpleName).append(" where ");
			if (smartString.indexOf('?') == -1) {
				String[] propertyNames = smartString.split(",");
				for (int i = 0; i < propertyNames.length; i++) {
					if (i != 0) {
						sb.append(" and ");
					}
					sb.append(propertyNames[i]).append("=?");
				}
			} else {
				sb.append(smartString);
			}
			return Ar.getHibernateTemplate().iterate(sb.toString(), values);
		} else {
			return Ar.getHibernateTemplate().iterate(smartString, values);
		}
	}

	// ----------------------------------------------------

	@SuppressWarnings("unchecked")
	public <T> List<T> sqlEntity(final String sql, final Object... values) {
		return (List<T>) Ar.sql(sql, entityClass, values);
	}

	public <T> List<T> sqlEntity(final String sql, final String alias,
			final Object... values) {
		return Ar.sql(sql, alias, entityClass, values);
	}

	// --------------------------

	public boolean equals(Object obj) {
		return Ar.equals(this, obj);
	}

	public int hashCode() {
		Serializable id = Ar.getId(this);
		return 31 * 17 + ((id == null) ? 0 : id.hashCode());
	}

	@Override
	public String toString() {
		return entityClass.getName() + "#" + Ar.getId(this);
	}

}
