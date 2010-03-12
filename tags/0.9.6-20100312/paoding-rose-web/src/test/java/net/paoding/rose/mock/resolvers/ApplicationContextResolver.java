package net.paoding.rose.mock.resolvers;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.paramresolver.ParamMetaData;
import net.paoding.rose.web.paramresolver.ParamResolver;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextResolver implements ParamResolver, ApplicationContextAware {

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean supports(ParamMetaData paramMetaData) {

        return ApplicationContext.class.isAssignableFrom(paramMetaData.getParamType());
    }

    @Override
    public Object resolve(Invocation inv, ParamMetaData paramMetaData) throws Exception {
        return applicationContext;
    }

}
