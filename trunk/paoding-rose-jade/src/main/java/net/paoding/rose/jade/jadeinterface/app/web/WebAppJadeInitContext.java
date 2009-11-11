package net.paoding.rose.jade.jadeinterface.app.web;

import javax.servlet.ServletContext;


/**
 * 实现用 {@link ServletContext} (web.xml) 配置 Jade 启动参数的上下文。
 * 
 * @author han.liao
 */
public class WebAppJadeInitContext extends JadeInitContext {

    private final ServletContext context;

    public WebAppJadeInitContext(ServletContext context) {
        this.context = context;
    }

    @Override
    public Object get(String param) {
        return context.getInitParameter(param);
    }

    @Override
    public void put(String param, Object value) {
        throw new UnsupportedOperationException("WebJadeInitContext#put");
    }
}
