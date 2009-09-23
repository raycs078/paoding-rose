package net.paoding.rose.mock.controllers.rest;

import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.annotation.rest.Post;

public class SomeController {

    @Get
    public String haha() {
        return "get";
    }

    @Post
    public String gaga() {
        return "post";
    }
}
