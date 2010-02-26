package net.paoding.rose.mock.controllers.samepath.s1;

import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.annotation.rest.Get;

@ReqMapping(path = "ab")
public class AController {

    @Get
    public String xx() {
        return "a";
    }
}
