package net.paoding.rose.mock.resolvers;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.paramresolver.ParamMetaData;
import net.paoding.rose.web.paramresolver.ParamResolver;

public class InterfaceResolver implements ParamResolver {

    public static final String GET_VALUE = "yhntgbrfv";

    @Override
    public boolean supports(ParamMetaData paramMetaData) {
        return Interface.class == paramMetaData.getParamType();
    }

    @Override
    public Object resolve(Invocation inv, ParamMetaData paramMetaData) throws Exception {
        return new Interface() {

            @Override
            public String get() {
                return GET_VALUE;
            }
        };
    }

}
