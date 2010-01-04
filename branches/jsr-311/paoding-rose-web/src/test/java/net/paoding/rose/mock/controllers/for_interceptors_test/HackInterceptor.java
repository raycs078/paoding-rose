package net.paoding.rose.mock.controllers.for_interceptors_test;

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;

public class HackInterceptor extends ControllerInterceptorAdapter {

	public static final String RETURN = "returned-by-HackInterceptor.after";
	public static final String AFTER_COMPLETION = "HackInterceptor.afterCompletion";

	public HackInterceptor() {
		setPriority(0);
	}

	@Override
	public Object after(Invocation inv, Object instruction)
			throws Exception {
		return RETURN;
	}
	
	@Override
	public void afterCompletion(Invocation inv, Throwable ex)
			throws Exception {
		inv.getRequest().setAttribute(AFTER_COMPLETION, true);
	}
}
