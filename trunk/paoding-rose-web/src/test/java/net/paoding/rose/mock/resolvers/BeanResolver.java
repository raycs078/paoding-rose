package net.paoding.rose.mock.resolvers;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.paramresolver.ParamMetaData;
import net.paoding.rose.web.paramresolver.ParamResolver;

public class BeanResolver implements ParamResolver {

    public static final String GET_VALUE = "bean0isadcae54";

    @Override
    public boolean supports(ParamMetaData paramMetaData) {
        return Bean.class == paramMetaData.getParamType();
    }

    @Override
    public Bean resolve(Invocation inv, ParamMetaData paramMetaData) throws Exception {
        return new Bean() {

            @Override
            public String get() {
                return GET_VALUE;
            }
        };
    }

}
