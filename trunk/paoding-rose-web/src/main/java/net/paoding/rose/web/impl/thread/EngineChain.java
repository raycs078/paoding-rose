package net.paoding.rose.web.impl.thread;


public interface EngineChain {

    public Object invokeNext(Rose rose, Object instruction) throws Throwable;

    public void addAfterCompletion(AfterCompletion task);

}
