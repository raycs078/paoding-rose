package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.rest.Get;

//测试 OncePerRequestInterceptor不会被调用2次！随便测试PreInvocation
public class OncePerRequestController {

    @Get
    public Object a(Invocation inv) throws Exception {
        Object value = "ok";
        inv.setOncePerRequestAttribute("once", value);
        inv.getRequest().setAttribute("preInv", inv);
        inv.getRequest().getRequestDispatcher("/oncePerRequest/b").forward(inv.getRequest(),
                inv.getResponse());
        return inv.getRequest().getAttribute("msg");
    }

    public String b(Invocation inv) {
        System.out.println("===success to forward  [OncePerRequestController]");
        Invocation preByActionA = (Invocation) inv.getRequest().getAttribute("preInv");
        if (preByActionA == null) {
            return "preInvocation.error.null";
        }
        if (preByActionA != inv.getPreInvocation()) {
            return "preInvocation.error";
        }
        inv.getRequest().setAttribute("msg", "ok");
        String ok = (String) inv.getOncePerRequestAttribute("once");
        if (!"ok".equals(ok)) {
            throw new IllegalArgumentException("setOncePerRequestAttribute");
        }
        return ok;
    }
}
