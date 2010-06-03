package net.paoding.rose.mock.controllers.errorhandler;

import net.paoding.rose.web.annotation.rest.Get;


public class MainController {

    @Get
    public void index() {
        throw new IllegalArgumentException("main");
    }
}
