package net.paoding.rose.jade.jadeinterface.app.web;

import javax.servlet.ServletContext;

import net.paoding.rose.jade.jadeinterface.app.JadeConfig;

import org.springframework.web.context.WebApplicationContext;

/**
 * 实现用 web.xml 配置 Jade 启动参数。
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public class JadeWebAppConfig extends JadeConfig {

    private final ServletContext context;

    public JadeWebAppConfig(WebApplicationContext applicationContext) {
        this.context = applicationContext.getServletContext();
    }

    public JadeWebAppConfig(ServletContext context) {
        this.context = context;
    }

    @Override
    public Object getConfig(String param) {
        return context.getInitParameter(param);
    }
}
