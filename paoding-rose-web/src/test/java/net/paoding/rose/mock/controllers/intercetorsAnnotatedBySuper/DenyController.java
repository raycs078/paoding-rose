package net.paoding.rose.mock.controllers.intercetorsAnnotatedBySuper;

import net.paoding.rose.web.annotation.rest.Get;

public class DenyController extends DenyBaseController {

    @Get
	public void index() {

	}
}
