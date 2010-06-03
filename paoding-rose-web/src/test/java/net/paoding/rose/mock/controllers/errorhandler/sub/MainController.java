package net.paoding.rose.mock.controllers.errorhandler.sub;

import net.paoding.rose.web.annotation.rest.Get;

public class MainController {

    @Get
    public void index() {
        throw new IllegalArgumentException("main-sub");
    }
}
