package net.paoding.rose.mock.controllers.resolver;

import net.paoding.rose.mock.resolvers.Interface;

public class ChildController extends Base {

    public String intf(Interface intf) {
        return intf.get();
    }
}
