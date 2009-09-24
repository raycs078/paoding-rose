package net.paoding.rose.web.impl.module;

import net.paoding.rose.web.NamedValidator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class NamedValidatorWrapper implements NamedValidator {

    private String name;

    private Validator validator;

    public NamedValidatorWrapper(String name, Validator validator) {
        this.name = name;
        this.validator = validator;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
        return validator.supports(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validator.validate(target, errors);
    }

}
