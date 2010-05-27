package net.paoding.rose.mock.controllers;

import net.paoding.rose.web.annotation.IfParamExists;
import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.annotation.rest.Get;

@ReqMapping(path = "if")
public class IfController {

    @Get
    public String a() {
        return "a";
    }

    @Get
    @IfParamExists("b")
    public String b() {
        return "b";
    }

    @Get
    @IfParamExists("c")
    public String c() {
        return "c";
    }

    @Get("d")
    public String d() {
        return "d";
    }

    @Get
    @IfParamExists("c=3")
    public String c3() {
        return "c3";
    }

    @Get
    @IfParamExists("c=:[0-9]+")
    public String c2() {
        return "c2";
    }

    @Get
    @IfParamExists("a=a&b=b")
    public String ab1() {
        return "ab1";
    }

    @Get
    @IfParamExists("a=:[0-9]+&b=:[0-9]+")
    public String ab2() {
        return "ab2";
    }
    
    @Get
    @IfParamExists("a=:.*&b=:.*")
    public String ab3() {
        return "ab3";
    }
    
    @Get
    @IfParamExists("a=a&b=b&c=c")
    public String abc1() {
        return "abc1";
    }
    
}
