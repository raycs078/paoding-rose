package net.paoding.rose.mock.controllers.paths;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.impl.thread.Engine;
import net.paoding.rose.web.impl.thread.InvocationBean;

@Path( { "path", "path/c{c}" })
public class PathController {

    @Get( { "action", "action/a{a}" })
    public String action(Invocation inv) {
        return "1" + inv.getRequestPath().getActionPath();
    }

    public String controller(Invocation inv) {
        return "2" + inv.getRequestPath().getControllerPath();
    }

    public String module(Invocation inv) {
        return "3" + inv.getRequestPath().getModulePath();
    }

    public Engine moduleEngine(Invocation inv) {
        InvocationBean invb = (InvocationBean) inv;
        return invb.getModuleEngine();
    }

    public Engine controllerEngine(Invocation inv) {
        InvocationBean invb = (InvocationBean) inv;
        return invb.getControllerEngine();
    }

    public Engine actionEngine(Invocation inv) {
        InvocationBean invb = (InvocationBean) inv;
        return invb.getActionEngine();
    }
}
