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
package net.paoding.rose.jade.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQLParam;
import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.Definition;
import net.paoding.rose.jade.provider.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class JadeDaoInvocationHandler implements InvocationHandler {

    private static final Log logger = LogFactory.getLog(JadeDaoFactoryBean.class);

    private static JadeOperationFactory jdbcOperationFactory = new JadeOperationFactoryImpl();

    private HashMap<Method, JadeOperation> jdbcOperations = new HashMap<Method, JadeOperation>();

    private final Definition definition;

    private final DataAccess dataAccess;

    public JadeDaoInvocationHandler(DataAccess dataAccess, Definition definition) {
        this.definition = definition;
        this.dataAccess = dataAccess;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (logger.isDebugEnabled()) {
            logger.debug("invoke: " + definition.getDAOClazz().getName() + "#" + method.getName());
        }

        if (Object.class == method.getDeclaringClass()) {
            String methodName = method.getName();
            if (methodName.equals("toString")) {
                return JadeDaoInvocationHandler.this.toString();
            }
            if (methodName.equals("hashCode")) {
                return definition.getDAOClazz().hashCode() * 13 + this.hashCode();
            }
            if (methodName.equals("equals")) {
                return args[0] == proxy;
            }
            if (methodName.equals("clone")) {
                throw new CloneNotSupportedException("clone is not supported for jade dao.");
            }
            throw new UnsupportedOperationException(definition.getDAOClazz().getName() + "#"
                    + method.getName());
        }

        JadeOperation operation = jdbcOperations.get(method);
        if (operation == null) {
            synchronized (jdbcOperations) {
                operation = jdbcOperations.get(method);
                if (operation == null) {
                    Modifier modifier = new Modifier(definition, method);
                    operation = jdbcOperationFactory.getOperation(dataAccess, modifier);
                    jdbcOperations.put(method, operation);
                }
            }
        }
        //
        // 将参数放入  Map
        Map<String, Object> parameters;
        if (args == null || args.length == 0) {
            parameters = new HashMap<String, Object>(4);
        } else {
            parameters = new HashMap<String, Object>(args.length * 2 + 4);
            SQLParam[] sqlParams = operation.getModifier().getParameterAnnotations(SQLParam.class);
            for (int i = 0; i < args.length; i++) {
                parameters.put(":" + (i + 1), args[i]);
                SQLParam sqlParam = sqlParams[i];
                if (sqlParam != null) {
                    parameters.put(sqlParam.value(), args[i]);
                }
            }
        }
        //

        return operation.execute(parameters);
    }

    @Override
    public String toString() {
        DAO dao = definition.getDAOClazz().getAnnotation(DAO.class);
        String toString = definition.getDAOClazz().getName()//
                + "[catalog=" + dao.catalog() + "]";
        return toString;
    }

}
