package net.paoding.rose.mock.controllers.samepath.x;

import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

@Path("")
public class AdController {

    @Get("ab")
    public String a() {
        return "a";
    }

    @Get("{d:*}")
    public String d() {
        return "d";
    }
}
