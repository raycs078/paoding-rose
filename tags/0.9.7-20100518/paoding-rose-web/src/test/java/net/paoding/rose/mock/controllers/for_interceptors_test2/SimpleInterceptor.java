package net.paoding.rose.mock.controllers.for_interceptors_test2;

import java.lang.annotation.Annotation;

import net.paoding.rose.mock.controllers.for_interceptors_test2.annotation.DenyAnnotation;
import net.paoding.rose.mock.controllers.for_interceptors_test2.annotation.RequiredAnnotation;
import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;

public class SimpleInterceptor extends ControllerInterceptorAdapter {

    public static final String RETURN = "returned-by-SimpleInterceptor.after";
    public static final String AFTER_COMPLETION = "SimpleInterceptor.afterCompletion";

    public SimpleInterceptor() {
        setPriority(20);
    }

    @Override
    protected Class<? extends Annotation> getRequiredAnnotationClass() {
        return RequiredAnnotation.class;
    }

    @Override
    protected Class<? extends Annotation> getDenyAnnotationClass() {
        return DenyAnnotation.class;
    }

    @Override
    public Object after(Invocation inv, Object instruction) throws Exception {
        return RETURN;
    }

    @Override
    public void afterCompletion(Invocation inv, Throwable ex) throws Exception {
        inv.getRequest().setAttribute(AFTER_COMPLETION, true);
    }
}
