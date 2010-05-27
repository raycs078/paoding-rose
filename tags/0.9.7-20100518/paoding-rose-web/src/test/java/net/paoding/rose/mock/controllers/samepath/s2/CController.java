package net.paoding.rose.mock.controllers.samepath.s2;

import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.annotation.rest.Get;
@ReqMapping(path = "a{c:*}")
public class CController {

    @Get
    public String xx() {
        return "c";
    }
}
