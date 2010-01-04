package net.paoding.rose.mock.controllers.for_interceptors_test;

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;

public class BlockInterceptor extends ControllerInterceptorAdapter {

	public static final String RETURN = "returned-by-BlockInterceptor.before";
	public static final String AFTER_COMPLETION = "BlockInterceptor.afterCompletion";

	public BlockInterceptor() {
		setPriority(10);
	}

	@Override
	public Object before(Invocation inv) throws Exception {
		return RETURN;
	}

	@Override
	public void afterCompletion(Invocation inv, Throwable ex)
			throws Exception {
		inv.getRequest().setAttribute(AFTER_COMPLETION, true);
	}

}