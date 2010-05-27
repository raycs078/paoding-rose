package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ReqMapping;

public class UserInfoC {

    @ReqMapping(path = "{id:[0-9]+}")
    public int show(@Param("id") Integer id) {
        return id;
    }
}
