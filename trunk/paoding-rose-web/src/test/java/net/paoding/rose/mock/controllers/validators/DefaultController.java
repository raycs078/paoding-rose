package net.paoding.rose.mock.controllers.validators;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;

@Path("")
public class DefaultController {

    public int hello(@Param("p") int p) {
        return p;
    }
}
