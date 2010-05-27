package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

@Path({ "", "def" })
public class DefController {

    public String index() {
        return "index";
    }

    public String method() {
        return "method";
    }

    @Get("param_{id}")
    public String param(@Param("id") String id) {
        return "param_" + id;
    }
}
