package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

@Path( { "/", "def" })
public class DefController {

    @Get("/")
    public String index() {
        return "index";
    }

    /**
     * 支持特殊名称，如 /xxxx.html
     */
    @Get("t/{tinyurl:[0-9a-zA-Z]+}.html")
    public String tinyurl(Invocation inv, @Param("tinyurl") String tinyurl) {
        return tinyurl;
    }

    public String method() {
        return "method";
    }

    @Get("param_{id}")
    public String param(@Param("id") String id) {
        return "param_" + id;
    }
}
