package net.paoding.rose.mock.controllers.intercetorsAnnotatedBySuper;

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;

public class HeadInterceptor extends ControllerInterceptorAdapter {

	public static final String RETURN = "HeadInterceptor.after";
	
	@Override
	public Object after(Invocation inv, Object instruction) throws Exception {
		return instruction + "." + RETURN;
	}
}
