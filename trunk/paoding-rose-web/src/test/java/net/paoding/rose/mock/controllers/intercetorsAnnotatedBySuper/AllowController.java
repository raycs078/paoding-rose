package net.paoding.rose.mock.controllers.intercetorsAnnotatedBySuper;

import net.paoding.rose.web.annotation.rest.Get;

public class AllowController extends AllowBaseController {

    @Get
	public void index() {

	}
}
