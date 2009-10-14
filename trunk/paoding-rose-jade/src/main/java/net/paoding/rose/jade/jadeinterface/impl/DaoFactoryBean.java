package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.regex.Pattern;

import net.paoding.rose.jade.jadeinterface.annotation.Dao;
import net.paoding.rose.jade.jadeinterface.annotation.SQL;
import net.paoding.rose.jade.jadeinterface.annotation.SQLType;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class DaoFactoryBean<T> implements FactoryBean, InitializingBean {

    private DataAccessProvider dataAccessProvider;

    private DataSourceFactory dataSourceFactory;

    private static RowMapperFactory rowMapperFactory = new RowMapperFactoryImpl();

    private T dao;

    private Class<T> daoClass;

    public void setDaoClass(Class<T> daoClass) {
        this.daoClass = daoClass;
    }

    public void setDataAccessProvider(DataAccessProvider dataAccessProvider) {
        this.dataAccessProvider = dataAccessProvider;
    }

    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public T getObject() {
        return dao;
    }

    @Override
    public Class<T> getObjectType() {
        return daoClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() {
        this.dao = createDao(daoClass);
    }

    @SuppressWarnings("unchecked")
    private T createDao(final Class<T> daoClass) {
        if (!daoClass.isInterface()) {
            throw new IllegalArgumentException("daoClass should be a interface");
        }

        Dao annotation = daoClass.getAnnotation(Dao.class);
        if (annotation == null) {
            throw new IllegalArgumentException("not @Dao annotated ");
        }

        String dataSourceName = annotation.catalog();
        javax.sql.DataSource dataSource = dataSourceFactory.getDataSource(dataSourceName);
        final DataAccess dataAccess = dataAccessProvider.createDataAccess(dataSource);
        return (T) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(),
                new Class[] { daoClass }, new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {
                        SQL sql = method.getAnnotation(SQL.class);
                        if (sql == null) {
                            // toString
                            if (method.getName().equals("toString")
                                    && method.getReturnType() == String.class
                                    && method.getParameterTypes().length == 0) {
                                return daoClass.getName() + "@"
                                        + Integer.toHexString(System.identityHashCode(this));
                            }
                            if (method.getDeclaringClass() == Object.class) {
                                return method.invoke(this, args);
                            } else {
                                throw new UnsupportedOperationException(
                                        "not a sql command method: " + method.getName());
                            }
                        }
                        JdbcOperation operation = getJdbcOperation(sql);
                        return operation.execute(dataAccess, daoClass, method, args);
                    }
                });
    }

    protected static Pattern SELECT = Pattern.compile("^\\s*SELECT", Pattern.CASE_INSENSITIVE);

    protected JdbcOperation getJdbcOperation(SQL sqlCommand) {
        SQLType command = sqlCommand.type();
        if (command == SQLType.AUTO_DETECT) {
            String sql = sqlCommand.value();
            // 用正则表达式匹配  SELECT 语句
            if (SELECT.matcher(sql).find()) {
                command = SQLType.SELECT;
            } else {
                command = SQLType.UPDATE;
            }
        }
        if (SQLType.SELECT == command) {
            SelectOperation select = new SelectOperation();
            select.setRowMapperFactory(rowMapperFactory);
            return select;
        } else if (SQLType.UPDATE == command) {
            UpdateOperation update = new UpdateOperation();
            update.setRowMapperFactory(rowMapperFactory);
            return update;
        }
        // 抛出检查异常
        throw new AssertionError("unknown command");
    }
}
