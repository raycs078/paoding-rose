package net.paoding.rose.web;

import net.paoding.rose.web.paramresolver.ParamMetaData;

import org.springframework.validation.Errors;


public class WindowParamValidator implements ParamValidator {

    @Override
    public boolean supports(ParamMetaData metaData) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object validate(Invocation inv, Object target, Errors errors) {
        // TODO Auto-generated method stub
        return null;
    }

}
