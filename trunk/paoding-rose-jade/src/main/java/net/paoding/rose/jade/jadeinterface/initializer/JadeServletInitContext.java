package net.paoding.rose.jade.jadeinterface.initializer;

import javax.servlet.ServletContext;

/**
 * 实现用 {@link ServletContext} (web.xml) 配置 Jade 启动参数的上下文。
 * 
 * @author han.liao
 */
public class JadeServletInitContext extends JadeInitContext {

    private final ServletContext context;

    public JadeServletInitContext(ServletContext context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) context.getInitParameter(name);
    }

    @Override
    public void put(String name, Object value) {
        throw new UnsupportedOperationException("JadeServletInitContext#put");
    }
}
