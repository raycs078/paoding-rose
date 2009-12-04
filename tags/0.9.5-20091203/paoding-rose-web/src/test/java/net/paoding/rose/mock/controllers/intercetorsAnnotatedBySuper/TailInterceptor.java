package net.paoding.rose.mock.controllers.intercetorsAnnotatedBySuper;

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;

public class TailInterceptor extends ControllerInterceptorAdapter {

	public static final String RETURN = "TailInterceptor.after";

	public TailInterceptor() {
		setPriority(-1);
	}

	@Override
	public Object after(Invocation inv, Object instruction) throws Exception {
		return RETURN;
	}
}
