package net.paoding.rose.testcases;

import java.lang.reflect.Method;

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Interceptor;

@Interceptor(oncePerRequest = true)
public class OncePerRequestInterceptor extends ControllerInterceptorAdapter {

    @Override
    protected boolean isForAction(Method actionMethod, Class<?> controllerClazz) {
        return OncePerRequestController.class == controllerClazz;
    }

    @Override
    public Object before(Invocation inv) throws Exception {
        if (inv.getRequest().getAttribute("onceper") != null) {
            throw new IllegalArgumentException("onceperrequest");
        }
        inv.getRequest().setAttribute("onceper", inv);
        return super.before(inv);
    }
}
