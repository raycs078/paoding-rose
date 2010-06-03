package net.paoding.rose.mock.controllers.modulepath1.submodulepath2.sub3;

import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

@Path("")
public class WelcomeController {

    @Get
    public Object index() {
        return getClass();
    }
}
