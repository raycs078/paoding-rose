package net.paoding.rose.testcases;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationUtils;
import net.paoding.rose.web.annotation.rest.Get;

public class CurrentController {

    @Get
    public String show(Invocation inv) {
        if (inv.getRequest() != InvocationUtils.getCurrentThreadRequest()) {
            return "getCurrentThreadRequest.error";
        }
        if (inv != InvocationUtils.getCurrentThreadInvocation()) {
            return "getCurrentThreadInvocation.error";
        }
        if (inv != InvocationUtils.getInvocation(inv.getRequest())) {
            return "getInvocation.error";
        }
        return "ok";
    }
}
