package net.paoding.rose.jade.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.paoding.rose.jade.annotation.SQLParam;
import net.paoding.rose.jade.provider.DataAccess;
import net.paoding.rose.jade.provider.Definition;
import net.paoding.rose.jade.provider.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
            return method.invoke(this, args);
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
        return definition.getDAOClazz().getName() + "@"
                + Integer.toHexString(System.identityHashCode(this));
    }

}
