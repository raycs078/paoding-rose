package net.paoding.rose.mock.controllers.errorhandler;


public class MainController {

    public void index() {
        throw new IllegalArgumentException("main");
    }
}
