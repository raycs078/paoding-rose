package net.paoding.rose.mock.controllers.mapping;

import net.paoding.rose.web.annotation.rest.Get;

public class ConstantController {

    @Get("/mn/show")
    public String xx() {
        return "xx:mn/show";
    }

    @Get("$1")
    public String def(String def) {
        return def;
    }

}
