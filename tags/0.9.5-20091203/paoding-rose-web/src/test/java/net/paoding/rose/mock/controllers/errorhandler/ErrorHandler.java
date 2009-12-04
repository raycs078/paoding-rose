package net.paoding.rose.mock.controllers.errorhandler;

import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.Invocation;

public class ErrorHandler implements ControllerErrorHandler {

    @Override
    public Object onError(Invocation inv, Throwable ex) {
        return ex;
    }

}
