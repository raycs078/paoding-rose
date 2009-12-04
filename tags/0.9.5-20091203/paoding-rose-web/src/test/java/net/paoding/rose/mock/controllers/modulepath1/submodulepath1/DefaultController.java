package net.paoding.rose.mock.controllers.modulepath1.submodulepath1;

import net.paoding.rose.web.annotation.ReqMapping;

@ReqMapping(path = "")
public class DefaultController {

    public Object index() {
        return getClass();
    }
}
