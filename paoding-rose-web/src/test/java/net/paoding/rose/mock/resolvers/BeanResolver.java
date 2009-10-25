package net.paoding.rose.mock.resolvers;

import java.lang.reflect.Method;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.paramresolver.ParamResolverBean;

public class BeanResolver implements ParamResolverBean {

    public static final String GET_VALUE = "bean0isadcae54";

    @Override
    public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
        return Bean.class == parameterType;
    }

    @Override
    public Bean resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
            Invocation inv, String parameterName, Param paramAnnotation) throws Exception {
        return new Bean() {

            @Override
            public String get() {
                return GET_VALUE;
            }
        };
    }

}
