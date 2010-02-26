package net.paoding.rose.mock.controllers.samepath.s1;

import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.annotation.rest.Get;
@ReqMapping(path = "{d:*}")
public class DController {

    @Get
    public String xx() {
        return "d";
    }
}
