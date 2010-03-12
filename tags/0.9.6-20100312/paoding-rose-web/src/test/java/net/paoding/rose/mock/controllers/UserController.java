package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ReqMapping;

public class UserController {

    public String index() {
        return "index";
    }

    @ReqMapping(path = "{id}")
    public int show(@Param("id") Integer id, Invocation inv) {
        if (!id.toString().equals(inv.getParameter("id"))) {
            throw new IllegalStateException("inv.getParameter should return params in uri");
        }
        if (!String.valueOf(id).equals(inv.getRequest().getParameter("id"))) {
            throw new IllegalStateException("request.getParameter should return params in uri");
        }
        return id;
    }

    @ReqMapping(path = "{id}/account")
    public String account(@Param("id") Integer id) {
        return "account_" + id;
    }

    public String post() {
        return "POST";
    }

    public int queryString(@Param("id") int id) {
        return id;
    }

    public int inf(Interface inf, @Param("id") int id) {
        return id;
    }

    static interface Interface {

    }
}
