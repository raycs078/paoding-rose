package net.paoding.rose.jade.jadeinterface.provider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassUtils;

/**
 * 假的 {@link DataAccessProvider} 实现。
 * 
 * @author han.liao
 */
public class DataAccessProviderMock implements DataAccessProvider {

    private static final Log logger = LogFactory.getLog(DataAccessProvider.class);

    @Override
    public DataAccess createDataAccess(String dataSourceName) {

        if (logger.isWarnEnabled()) {
            logger.warn("jade is not configured, return mock instance");
        }

        return (DataAccess) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(),
                new Class[] { DataAccess.class }, new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {

                        if (method.getDeclaringClass() == DataAccess.class) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("jade is not configured");
                            }
                            throw new IllegalStateException("jade is not configured");
                        }

                        return method.invoke(proxy, args);
                    }
                });
    }

}
