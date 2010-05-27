package net.paoding.rose.mock.controllers.samepath.x;

import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

@Path("")
public class BcController {

    @Get("ab{b:*}")
    public String b() {
        return "b";
    }

    @Get("a{c:*}")
    public String c() {
        return "c";
    }
}
