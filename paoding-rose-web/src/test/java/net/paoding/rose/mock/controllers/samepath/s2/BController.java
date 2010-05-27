package net.paoding.rose.mock.controllers.samepath.s2;

import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;
@Path("ab{b:*}")
public class BController {

    @Get
    public String xx() {
        return "b";
    }
}
