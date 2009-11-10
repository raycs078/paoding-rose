package net.paoding.rose.jade.jadeinterface;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;

public class JadeDaoFactoryImpl extends JadeDaoFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T getDao(Class<T> clazz) {

        // 从类名构造 Dao 的名称
        String beanName = ClassUtils.getShortNameAsProperty(clazz);

        // 获取 Dao 对象
        return clazz.cast(applicationContext.getBean(beanName));
    }
}
