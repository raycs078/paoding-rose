package net.paoding.rose.jade.jadeinterface.app;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * 加载配置文件资源的常用功能
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public class ResourceUtils {

    /**
     * 查找指定名称的配置文件资源。
     * 
     * @param applicationContext - 上下文
     * @param fileName - 配置文件名称
     * 
     * @return 配置文件资源
     */
    public static Resource findResource(// NL
            ApplicationContext applicationContext, // NL 
            String fileName) {

        Resource found = null;

        if (applicationContext instanceof WebApplicationContext) {
            found = ((WebApplicationContext) applicationContext)
                    .getResource("/WEB-INF/" + fileName);
        } else if (applicationContext instanceof ResourceLoader) {
            found = ((ResourceLoader) applicationContext).getResource(fileName);
        }

        if ((found == null) || !found.exists()) {
            found = new ClassPathResource(fileName);
        }

        return found;
    }
}
