package net.paoding.rose.mock.controllers.samepath.s1;

import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

@Path("ab")
public class AController {

    @Get
    public String xx() {
        return "a";
    }
}
