package net.paoding.rose.jade.jadeinterface.initializer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 
 * 
 * 
 * @author 
 */
public class JadeServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent e) {

        String providerClassName = e.getServletContext().getInitParameter(
                "jadeDataAccessProviderClass");
        if (providerClassName == null) {
            throw new NullPointerException("jadeDataAccessProviderClass");
        }
        try {
            JadeInitializer.initialize(providerClassName);
        } catch (Exception exp) {
            throw new IllegalArgumentException(exp);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent e) {
    }

}
