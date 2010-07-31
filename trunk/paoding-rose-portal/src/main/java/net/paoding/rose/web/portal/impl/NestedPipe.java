package net.paoding.rose.web.portal.impl;

import java.io.IOException;

import net.paoding.rose.web.portal.Pipe;
import net.paoding.rose.web.portal.Window;

public interface NestedPipe extends Pipe {

    public void await(long timeout) throws InterruptedException;

    public void start() throws IOException;

    public void fire(Window window) throws IOException;

    public void close();

}
