package net.paoding.rose.jade.jadeinterface;

import net.paoding.rose.jade.jadeinterface.provider.DataAccessProviderHolder;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 定义 Jade 的 Dao 访问接口。
 * 
 * @author han.liao
 */
public abstract class JadeDaoFactory {

    // 配置 JadeDaoFactory 名称
    public static final String JADE_DAO_FACTORY = "jadeDaoFactory";

    /**
     * 用所给的配置属性创建 Jade Dao 工厂。
     * 
     * @param context - Jade 配置属性
     */
    public static JadeDaoFactory getInstance() {

        DataAccessProviderHolder dataAccessProviderHolder = DataAccessProviderHolder.getInstance();

        // 获取已配置的 ApplicationContext
        ApplicationContext applocationContext = dataAccessProviderHolder.getApplicationContext();

        if (applocationContext == null) {
            // 创建独立的 ApplicationContext
            applocationContext = new ClassPathXmlApplicationContext(
                    "applicationContext-jade-interface.xml");
            // 传播 ApplicationContext
            dataAccessProviderHolder.setApplicationContext(applocationContext);
        }

        // 获取 JadeDaoFactory 实例
        return (JadeDaoFactory) applocationContext.getBean(JADE_DAO_FACTORY);
    }

    /**
     * 获取指定类型的 Dao 对象。
     * 
     * @param <T> - Dao 对象的类型
     * @param clazz - Dao 对象的类
     * 
     * @return Dao 对象
     */
    public abstract <T> T getDao(Class<T> clazz);
}
