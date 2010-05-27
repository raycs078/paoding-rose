package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;

public class TrackInterceptor extends ControllerInterceptorAdapter {

	@Override
	public Object before(Invocation inv) throws Exception {
		return super.before(inv);
	}

	@Override
	public Object after(Invocation inv, Object instruction)
			throws Exception {
		return super.after(inv, instruction);
	}

	@Override
	public void afterCompletion(Invocation inv, Throwable ex)
			throws Exception {
		super.afterCompletion(inv, ex);
	}
}
