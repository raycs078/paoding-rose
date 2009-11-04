package net.paoding.rose.jade.jadeinterface.initializer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 使用下列配置在容器 web.xml 中配置 {@link JadeServletContextListener}
 * 
 * <pre>
 * &lt;context-param&gt;
 *     &lt;param-name&gt;jadeDataAccessProviderClass&lt;/param-name&gt;
 *     &lt;param-value&gt;net.paoding.rose.jade.jadeinterface.provider.springjdbctemplte.SpringJdbcTemplateDataAccessProvider&lt;/param-value&gt;
 * &lt;/context-param&gt;
 * 
 * &lt;listener&gt;
 *     &lt;listener-class&gt;net.paoding.rose.jade.jadeinterface.initializer.JadeServletContextListener&lt;/listener-class&gt;
 * &lt;/listener&gt;
 * </pre>
 * 
 * <p>
 * 其中, 初始化参数 jadeDataAccessProviderClass 是需要指定的 Jade 实现, 它必须实现
 * {@link net.paoding.rose.jade.jadeinterface.provider.DataAccessProvider}
 * 接口, 请按实际需求改写。
 * </p>
 * 
 * @author han.liao
 */
public class JadeServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent e) {
        JadeInitializer.initialize(e.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent e) {
        // NO code here
    }
}
