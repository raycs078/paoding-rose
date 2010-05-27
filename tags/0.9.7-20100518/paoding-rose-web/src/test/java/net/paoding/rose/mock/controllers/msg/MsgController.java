package net.paoding.rose.mock.controllers.msg;

import java.util.Locale;

import net.paoding.rose.web.InvocationLocal;
import net.paoding.rose.web.annotation.DefValue;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.annotation.rest.Get;

import org.springframework.beans.factory.annotation.Autowired;

@ReqMapping(path = "")
public class MsgController {

    @Autowired
    InvocationLocal inv;

    @Get("$1")
    public String hello(String key, String[] args, @Param("loc") @DefValue("zh_CN") Locale lo) {
        return inv.getApplicationContext().getMessage(key, args, lo);
    }
}
