package net.paoding.rose.mock.controllers.for_interceptors_test.sub;

import net.paoding.rose.web.annotation.Intercepted;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

@Intercepted(allow = "hack")
@Path("hack")
public class HackController {

    @Get({ "", "index" })
	public Object index() {
		return "hack-index";
	}
}
