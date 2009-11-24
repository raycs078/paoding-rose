package net.paoding.rose.testcases;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.rest.Get;

public class OncePerRequestController {

    @Get
    public String a(Invocation inv) throws Exception {
        inv.getRequest().setAttribute("preInv", inv);
        inv.getRequest().getRequestDispatcher("/oncePerRequest/b").forward(inv.getRequest(),
                inv.getResponse());
        return (String) inv.getRequest().getAttribute("msg");
    }

    public String b(Invocation inv) {
        System.out.println("OncePerRequestController============bbbbbbbbb");
        Invocation pre = (Invocation) inv.getRequest().getAttribute("preInv");
        if (pre == null) {
            return "preInvocation.error.null";
        }
        if (pre != inv) {
            return "preInvocation.error";
        }
        inv.getRequest().setAttribute("msg", "ok");
        return "ok";
    }
}
