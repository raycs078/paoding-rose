package net.paoding.rose.mock.controllers.resolver;

import java.lang.reflect.Method;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.paramresolver.ParamResolverBean;

public class SomeResolver implements ParamResolverBean {

    public static final String DEFAULT_PHONE_ID = "45670tyuiuyt";

    @Override
    public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
        return parameterType == Phone.class;
    }

    @Override
    public Object resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
            Invocation inv, String parameterName, Param paramAnnotation) throws Exception {
        Phone phone = new Phone();
        phone.setId(DEFAULT_PHONE_ID);
        return phone;
    }

}
