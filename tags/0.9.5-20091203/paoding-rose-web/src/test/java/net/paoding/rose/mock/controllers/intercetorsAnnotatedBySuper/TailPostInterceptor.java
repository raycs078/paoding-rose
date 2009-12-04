package net.paoding.rose.mock.controllers.intercetorsAnnotatedBySuper;

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;

public class TailPostInterceptor extends ControllerInterceptorAdapter {

	public static final String RETURN = "TailPostInterceptor.after";

	public TailPostInterceptor() {
		setPriority(-10);
	}

	@Override
	public Object after(Invocation inv, Object instruction) throws Exception {
		return RETURN;
	}
}
