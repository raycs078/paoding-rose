package net.paoding.rose.mock.controllers.methodparameter;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ReqMapping;

@ReqMapping(path = "$controller.id/$controller.bool")
public class MethodParameterController {

    public void innt(@Param("controller.id") int cid) {
    }

    public void integer(@Param("controller.id") Integer cid) {
    }

    public void bool(@Param("controller.bool") boolean cid) {
    }

    public void boool(@Param("controller.bool") Boolean cid) {
    }

    public void loong(@Param("controller.id") long cid) {
    }

    public void looong(@Param("controller.id") Long cid) {
    }

    public void string(@Param("controller.id") String cid) {
    }

    public void nullPrimitiveBool(@Param("controller.abcded") boolean cid) {
    }

    public void nullPrimitiveBoolWrapper(@Param("controller.abcded") Boolean cid) {
    }
}
