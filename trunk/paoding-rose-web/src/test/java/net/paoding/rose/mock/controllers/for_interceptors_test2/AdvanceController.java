package net.paoding.rose.mock.controllers.for_interceptors_test2;

import net.paoding.rose.mock.controllers.for_interceptors_test2.annotation.AdvanceRequiredAnnotation;

public class AdvanceController {

    public static final String RETURN = "returned-by-AdvanceController";

    public String index() {
        return RETURN + ".index";
    }

    @AdvanceRequiredAnnotation
    public String method() {
        return RETURN + ".method";
    }
}
