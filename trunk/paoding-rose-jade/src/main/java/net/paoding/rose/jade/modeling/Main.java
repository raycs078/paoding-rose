package net.paoding.rose.jade.modeling;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.dataaccess.DataAccessFactory;
import net.paoding.rose.jade.dataaccess.DataAccessFactoryImpl;
import net.paoding.rose.jade.dataaccess.DataSourceFactory;
import net.paoding.rose.jade.rowmapper.BeanPropertyRowMapper;
import net.paoding.rose.jade.statement.DAOMetaData;
import net.paoding.rose.jade.statement.Interpreter;
import net.paoding.rose.jade.statement.JdbcStatement;
import net.paoding.rose.jade.statement.Querier;
import net.paoding.rose.jade.statement.SQLInterpreter;
import net.paoding.rose.jade.statement.SelectQuerier;
import net.paoding.rose.jade.statement.Statement;
import net.paoding.rose.jade.statement.StatementMetaData;
import net.paoding.rose.jade.statement.cached.CacheProvider;
import net.paoding.rose.jade.statement.cached.CachedStatement;
import net.paoding.rose.jade.statement.cached.MockCacheProvider;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

// 此package只用于“意图演示”
public class Main {

    public static void main(String[] args) throws SecurityException, NoSuchMethodException {

        DAOMetaData classMetaData = new DAOMetaData(MyDAO.class);
        Method method = MyDAO.class.getMethod("get", int.class);
        StatementMetaData smd = new StatementMetaData(classMetaData, method);
        Interpreter[] interpreters = getInterpreters();
        Querier querier = getQuerier(smd);
        JdbcStatement jdbcStatement = new JdbcStatement(smd, SQLType.READ, interpreters, querier);
        CacheProvider cacheProvider = new MockCacheProvider();
        CachedStatement cachedStatement = new CachedStatement(cacheProvider, jdbcStatement);
        Statement statement = cachedStatement;
        //
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(":1", 1);
        parameters.put("id", 1);
        User user = (User) statement.execute(parameters);
        System.out.println("user.id=" + user.getId() + "; user.name=" + user.getName());
    }

    private static Interpreter[] getInterpreters() {
        return new Interpreter[]{new SQLInterpreter()};
    }

    private static Querier getQuerier(StatementMetaData metaData) {
        DataSourceFactory dataSourceFactory = new DataSourceFactory() {

            SingleConnectionDataSource dataSource;

            @Override
            public DataSource getDataSource(StatementMetaData metaData,
                    Map<String, Object> runtimeProperties) {
                if (dataSource == null) {
                    SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
                    dataSource.setUrl("jdbc:mysql://10.3.22.168:3306/zhiliang");
                    dataSource.setUsername("zhiliang");
                    dataSource.setPassword("zhiliang");
                    dataSource.setDriverClassName("com.mysql.jdbc.Driver");
                    this.dataSource = dataSource;
                }
                return dataSource;
            }
        };
        DataAccessFactory dataAccessFactory = new DataAccessFactoryImpl(dataSourceFactory);
        RowMapper rowMapper = new BeanPropertyRowMapper(User.class, true, false);
        return new SelectQuerier(dataAccessFactory, metaData, rowMapper);
    }
}
