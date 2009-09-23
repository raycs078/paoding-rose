package net.paoding.rose.mock.controllers.autowire;

import javax.annotation.Resource;

import net.paoding.rose.mock.controllers.DefController;
import net.paoding.rose.testcases.controllers.autowire.AutowireBean;
import net.paoding.rose.testcases.controllers.autowire.AutowireBean2;
import net.paoding.rose.web.annotation.rest.Get;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class OrderController {

    @Autowired
    DefController defController;

    @Autowired
    AutowireBean2 autowireBean2;

    @Autowired
    AutowireBean autowireBean;

    @Resource
    ApplicationContext applicationContext;

    public String ok() {
        return "ok";
    }

    @Get
    public AutowireBean2 xxx() {
        return autowireBean2;
    }

    public DefController def() {
        return defController;
    }

    public ApplicationContext ctx() {
        return applicationContext;
    }

    public AutowireBean autowireBean() {
        return autowireBean;
    }
}
