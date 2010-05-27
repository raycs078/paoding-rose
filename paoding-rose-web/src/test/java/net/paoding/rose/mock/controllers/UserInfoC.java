package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.rest.Get;

public class UserInfoC {

    @Get("{id:[0-9]+}")
    public int show(@Param("id") Integer id) {
        return id;
    }
}
