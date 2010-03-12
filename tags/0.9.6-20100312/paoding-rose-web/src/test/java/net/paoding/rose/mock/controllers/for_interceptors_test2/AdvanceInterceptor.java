package net.paoding.rose.mock.controllers.for_interceptors_test2;

import java.lang.annotation.Annotation;
import java.util.BitSet;

import net.paoding.rose.mock.controllers.for_interceptors_test2.annotation.DenyAnnotation;
import net.paoding.rose.mock.controllers.for_interceptors_test2.annotation.RequiredAnnotation;
import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;


public class AdvanceInterceptor extends ControllerInterceptorAdapter {

    public static final String RETURN = "returned-by-AdvanceInterceptor.after";
    public static final String AFTER_COMPLETION = "AdvanceInterceptor.afterCompletion";

    public AdvanceInterceptor() {
        setPriority(10);
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
    protected BitSet getAnnotationScope(Class<? extends Annotation> annotationType) {
        return AnnotationScope.getMethodAndClassAndAnnotationScope();
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
