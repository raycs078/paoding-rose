package net.paoding.rose.mock.controllers.resolver.sub;

import net.paoding.rose.mock.controllers.resolver.Phone;
import net.paoding.rose.web.annotation.AsSuperController;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.rest.Get;

// 测试超类定义的方法被子类访问时候参数Resolver是否正常
@AsSuperController
public abstract class SuperController {

    @Get
    public String get(@Param("phone") Phone phone) {
        if (phone == null) {
            return "superController.paramResolver";
        }
        return "ok";
    }
}
