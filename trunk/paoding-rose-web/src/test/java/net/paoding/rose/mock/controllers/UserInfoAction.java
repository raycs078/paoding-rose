package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.rest.Get;

public class UserInfoAction {

    @Get("hello/{id:[0-9]+}")
    public int hello(@Param("id") Integer id) {
        return id;
    }
}
