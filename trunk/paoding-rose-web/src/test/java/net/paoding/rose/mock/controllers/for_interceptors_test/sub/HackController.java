package net.paoding.rose.mock.controllers.for_interceptors_test.sub;

import net.paoding.rose.web.annotation.Intercepted;
import net.paoding.rose.web.annotation.ReqMapping;

@Intercepted(allow = "hack")
@ReqMapping(path = "hack")
public class HackController {

	@ReqMapping(path = { "", "index" })
	public Object index() {
		return "hack-index";
	}
}
