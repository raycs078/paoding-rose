package net.paoding.rose.mock.controllers.validators;

import org.springframework.validation.Errors;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.ParamValidator;
import net.paoding.rose.web.paramresolver.ParamMetaData;


public class PValidator implements ParamValidator {

    @Override
    public boolean supports(ParamMetaData metaData) {
        return "p".equals(metaData.getParamName());
    }

    @Override
    public Object validate(Invocation inv, Object target, Errors errors) {
        return ((Integer) target) == 0 ?  "error" : true;
    }

}
