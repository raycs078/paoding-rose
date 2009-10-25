package net.paoding.rose.mock.resolvers;

import java.lang.reflect.Method;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.paramresolver.ParamResolverBean;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextResolver implements ParamResolverBean, ApplicationContextAware {

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {

        return ApplicationContext.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
            Invocation inv, String parameterName, Param paramAnnotation) throws Exception {
        return applicationContext;
    }

}
