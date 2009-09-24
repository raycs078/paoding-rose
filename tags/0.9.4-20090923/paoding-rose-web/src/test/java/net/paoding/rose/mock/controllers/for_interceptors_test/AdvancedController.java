package net.paoding.rose.mock.controllers.for_interceptors_test;

import net.paoding.rose.web.annotation.Intercepted;
import net.paoding.rose.web.annotation.ReqMapping;

@Intercepted(allow = {"advanced", "block", "hack" })
public class AdvancedController {

	@ReqMapping(path = { "", "index" })
	public Object index() {
		return "advanced-block-index";
	}
}
