package net.paoding.rose.mock.controllers.resolver;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.paramresolver.ParamMetaData;
import net.paoding.rose.web.paramresolver.ParamResolver;

public class SomeResolver implements ParamResolver {

    public static final String DEFAULT_PHONE_ID = "45670tyuiuyt";

    @Override
    public boolean supports(ParamMetaData paramMetaData) {
        return paramMetaData.getParamType() == Phone.class;
    }

    @Override
    public Object resolve(Invocation inv, ParamMetaData paramMetaData) throws Exception {
        Phone phone = new Phone();
        phone.setId(DEFAULT_PHONE_ID);
        return phone;
    }

}
