package net.paoding.rose.mock.controllers.for_interceptors_test.sub;

import net.paoding.rose.web.annotation.Intercepted;
import net.paoding.rose.web.annotation.ReqMapping;

@Intercepted(allow = { "block", "hack" })
@ReqMapping(path = "block")
public class BlockController {

	@ReqMapping(path = { "", "index" })
	public Object index() {
		return "block-index";
	}
}
