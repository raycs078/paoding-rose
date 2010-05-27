package net.paoding.rose.mock.controllers.for_interceptors_test;

import net.paoding.rose.web.annotation.Intercepted;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

@Intercepted(allow = { "block", "hack" })
@Path("block")
public class BlockController {

    @Get({ "", "index" })
	public Object index() {
		return "block-index";
	}
}
