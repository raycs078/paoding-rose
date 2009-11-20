package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.annotation.rest.Get;

@ReqMapping(path = "{user.id}/order")
public class OrderController {

    public String list(@Param("user.id") Long userId) {
        return "list/" + userId;
    }

    @Get("$id")
    public String show(@Param("user.id") Long userId, @Param("id") String id) {
        return "show/" + userId + "/" + id;
    }

    @Get
    public String def() {
        return "def";
    }
}
