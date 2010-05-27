package net.paoding.rose.mock.controllers.for_interceptors_test.sub;

import net.paoding.rose.web.annotation.Intercepted;
import net.paoding.rose.web.annotation.rest.Get;

@Intercepted(allow = {"advanced", "block", "hack" })
public class AdvancedController {

    @Get({ "", "index" })
	public Object index() {
		return "advanced-block-index";
	}
}
