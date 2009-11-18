package net.paoding.rose.web.impl.thread;

import net.paoding.rose.web.impl.thread.tree.Rose;

public interface EngineChain {

    public Object invokeNext(Rose rose, Object instruction) throws Throwable;

    public void addAfterCompletion(AfterCompletion task);

}
