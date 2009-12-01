package net.paoding.rose.jade.jadeinterface.app.java;

import net.paoding.rose.jade.jadeinterface.app.JadeConfig;

import org.springframework.context.ApplicationContext;

/**
 * 实现用 applicationContext.xml 配置 Jade 启动参数。
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public class JadeAppConfig extends JadeConfig {

    private final ApplicationContext applicationContext;

    public JadeAppConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getConfig(String param) {

        // 防止抛出  NoSuchBeanDefinitionException
        if (applicationContext.containsBean(param)) {
            return applicationContext.getBean(param);
        }

        return null;
    }
}
