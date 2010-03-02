package net.paoding.rose.mock.controllers.for_interceptors_test2;

import net.paoding.rose.mock.controllers.for_interceptors_test2.annotation.AdvanceDenyAnnotation;
import net.paoding.rose.mock.controllers.for_interceptors_test2.annotation.AdvanceRequiredAnnotation;

@AdvanceRequiredAnnotation
public class AdvanceDenyController {

    public static final String RETURN = "returned-by-AdvanceDenyController";

    @AdvanceDenyAnnotation
    public String index() {
        return RETURN + ".index";
    }

    public String method() {
        return RETURN + ".method";
    }
}
