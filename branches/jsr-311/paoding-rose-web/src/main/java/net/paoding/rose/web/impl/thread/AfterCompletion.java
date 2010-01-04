package net.paoding.rose.web.impl.thread;

import net.paoding.rose.web.Invocation;



public interface AfterCompletion {

    void afterCompletion(Invocation inv, Throwable ex) throws Exception;
}
