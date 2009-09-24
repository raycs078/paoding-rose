package net.paoding.rose.mock.controllers.errorhandler.sub;


public class MainController {

    public void index() {
        throw new IllegalArgumentException("main-sub");
    }
}
