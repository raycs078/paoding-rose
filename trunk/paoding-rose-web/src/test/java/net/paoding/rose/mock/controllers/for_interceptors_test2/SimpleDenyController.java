package net.paoding.rose.mock.controllers.for_interceptors_test2;

import net.paoding.rose.mock.controllers.for_interceptors_test2.annotation.DenyAnnotation;
import net.paoding.rose.mock.controllers.for_interceptors_test2.annotation.RequiredAnnotation;

@RequiredAnnotation
public class SimpleDenyController {

    public static final String RETURN = "returned-by-SimpleDenyController";

    @DenyAnnotation
    public String index() {
        return RETURN + ".index";
    }

    public String method() {
        return RETURN + ".method";
    }
}
