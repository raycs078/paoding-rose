/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.springcontext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;
import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.dataaccess.DataAccessFactory;
import net.paoding.rose.jade.rowmapper.RowMapperFactory;
import net.paoding.rose.jade.statement.DAOMetaData;
import net.paoding.rose.jade.statement.Interpreter;
import net.paoding.rose.jade.statement.Querier;
import net.paoding.rose.jade.statement.SelectQuerier;
import net.paoding.rose.jade.statement.Statement;
import net.paoding.rose.jade.statement.JdbcStatement;
import net.paoding.rose.jade.statement.StatementMetaData;
import net.paoding.rose.jade.statement.UpdateQuerier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class JadeDaoInvocationHandler implements InvocationHandler {

    private static final Log logger = LogFactory.getLog(JadeDaoInvocationHandler.class);

    private HashMap<Method, Statement> statements = new HashMap<Method, Statement>();

    private final DAOMetaData classMetaData;

    private final DataAccessFactory dataAccessProvider;

    private final RowMapperFactory rowMapperFactory;

    public JadeDaoInvocationHandler(//
            DAOMetaData classMetaData,//
            RowMapperFactory rowMapperFactory,//
            DataAccessFactory dataAccessProvider) {
        this.rowMapperFactory = rowMapperFactory;
        this.dataAccessProvider = dataAccessProvider;
        this.classMetaData = classMetaData;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (logger.isDebugEnabled()) {
            logger.debug("invoking  " + classMetaData.getDAOClass().getName() + "#"
                    + method.getName());
        }

        if (Object.class == method.getDeclaringClass()) {
            String methodName = method.getName();
            if (methodName.equals("toString")) {
                return JadeDaoInvocationHandler.this.toString();
            }
            if (methodName.equals("hashCode")) {
                return classMetaData.getDAOClass().hashCode() * 13 + this.hashCode();
            }
            if (methodName.equals("equals")) {
                return args[0] == proxy;
            }
            if (methodName.equals("clone")) {
                throw new CloneNotSupportedException("clone is not supported for jade dao.");
            }
            throw new UnsupportedOperationException(classMetaData.getDAOClass().getName() + "#"
                    + method.getName());
        }

        Statement statement = statements.get(method);
        if (statement == null) {
            synchronized (statements) {
                statement = statements.get(method);
                if (statement == null) {
                    StatementMetaData statementMetaData = new StatementMetaData(//
                            classMetaData, method);
                    // SQLType是不是应该提前在这里计算好，而非在StatementImpl
                    Interpreter[] interpreters = getInterpreters(statementMetaData);
                    SQLType sqlType = statementMetaData.getSQLType();
                    Querier querier;
                    if (sqlType == SQLType.READ) {
                        RowMapper rowMapper = rowMapperFactory.getRowMapper(statementMetaData);
                        querier = new SelectQuerier(dataAccessProvider, statementMetaData,
                                rowMapper);
                    } else {
                        querier = new UpdateQuerier(dataAccessProvider, statementMetaData);
                    }
                    statement = new JdbcStatement(statementMetaData, sqlType, interpreters, querier);
                    statements.put(method, statement);
                }
            }
        }
        //
        // 将参数放入  Map
        Map<String, Object> parameters;
        StatementMetaData statemenetMetaData = statement.getMetaData();
        if (args == null || args.length == 0) {
            parameters = new HashMap<String, Object>(4);
        } else {
            parameters = new HashMap<String, Object>(args.length * 2 + 4);
            for (int i = 0; i < args.length; i++) {
                parameters.put(":" + (i + 1), args[i]);
                SQLParam sqlParam = statemenetMetaData.getSQLParamAt(i);
                if (sqlParam != null) {
                    parameters.put(sqlParam.value(), args[i]);
                }
            }
        }
        //
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("invoking ").append(classMetaData.getDAOClass().getName()).append("#")
                    .append(method.getName()).append("\n");
            sb.append("\toperation: ").append(statement.getClass().getSimpleName()).append("\n");
            sb.append("\tsql: ").append(statemenetMetaData.getAnnotation(SQL.class).value())
                    .append("\n");
            sb.append("\tparams: ");
            ArrayList<String> keys = new ArrayList<String>(parameters.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                sb.append(key).append("='").append(parameters.get(key)).append("'  ");
            }
            logger.debug(sb.toString());
        }

        return statement.execute(parameters);
    }

    private Interpreter[] getInterpreters(StatementMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        DAO dao = classMetaData.getDAOClass().getAnnotation(DAO.class);
        String toString = classMetaData.getDAOClass().getName()//
                + "[catalog=" + dao.catalog() + "]";
        return toString;
    }

}
