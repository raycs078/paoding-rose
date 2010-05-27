package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationUtils;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.rest.Get;

public class CurrentController {

    @Get
    public String show(Invocation inv, @Param("testThread") String threadName) {
        if (!Thread.currentThread().getName().equals(threadName)) {
            // 为了在CurrentTest中确认最后的当前请求以及Inv为null
            // 所以需要判断一下Test和这里的线程应该是一样的，否则Test的测试没有意义
            return "Thread.currentThread().getName().error";
        }
        if (inv.getPreInvocation() != null) {
            new IllegalArgumentException("getPreInvocation.error.expected.null").printStackTrace();
            return "getPreInvocation.error.expected.null";
        }
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
