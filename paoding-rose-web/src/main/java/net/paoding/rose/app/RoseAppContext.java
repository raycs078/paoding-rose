/*
* Copyright 2007-2009 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package net.paoding.rose.app;

import java.io.IOException;
import java.util.List;

import net.paoding.rose.scanner.RoseJarContextResources;
import net.paoding.rose.web.impl.context.RoseContextLoader;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;

/**
 * 提供扫描 Rose: applicationContext-*.xml 配置并初始化 Bean 的功能.
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public class RoseAppContext {

    /**
     * 默认的 ApplicationContext 加载路径
     */
    public static final String APP_CONTEXT_CONFIG_LOCATION = "classpath:applicationContext*.xml";

    private final Log logger = LogFactory.getLog(this.getClass());

    protected ApplicationContext rootContext;

    /**
     * 创建 RoseAppContext.
     * 
     * @param contextConfigLocation - 配置加载路径
     */
    public RoseAppContext() {
        this(APP_CONTEXT_CONFIG_LOCATION);
    }

    /**
     * 创建 RoseAppContext.
     * 
     * @param contextConfigLocation - 配置加载路径
     */
    public RoseAppContext(String contextConfigLocation) {

        try {
            String namespace = "rose.app.root";

            // 确认所使用的 applicationContext 配置
            if (StringUtils.isBlank(contextConfigLocation)) {
                contextConfigLocation = APP_CONTEXT_CONFIG_LOCATION;
            }

            List<Resource> jarContextResources = RoseJarContextResources.findContextResources();
            if (logger.isInfoEnabled()) {
                logger.info("jarContextResources: "
                        + ArrayUtils.toString(jarContextResources.toArray()));
            }

            rootContext = RoseContextLoader.createApplicationContext(null, jarContextResources,
                    contextConfigLocation, namespace);

            if (logger.isInfoEnabled()) {
                logger.info("Built root XmlApplicationContext [" + rootContext + "]");
            }

        } catch (IOException e) {
            logger.error("RoseAppContext", e);
            throw new ApplicationContextException( // 
                    "RoseAppContext initializing error", e);
        }
    }

    /**
     * 返回 {@link ApplicationContext} 上下文.
     */
    public ApplicationContext getApplicationContext() {
        return rootContext;
    }

    /**
     * 检查对应名字的 Bean 是否存在.
     * 
     * @param name - Bean 的名字
     */
    public boolean containsBean(String name) {
        return rootContext.containsBean(name);
    }

    /**
     * 返回对应名字的 Bean.
     * 
     * @param name - Bean 的名字
     * 
     * @throws BeansException
     */
    public Object getBean(String name) throws BeansException {
        return rootContext.getBean(name);
    }

    /**
     * 返回对应名字与类型的 Bean.
     * 
     * @param name - Bean 的名字
     * @param beanType - Bean 的类型
     * 
     * @throws BeansException
     */
    public <T> T getBean(String name, Class<T> beanType) throws BeansException {
        return beanType.cast(rootContext.getBean(name, beanType));
    }

    /**
     * 返回对应类型的唯一 Bean, 不查找祖先 {@link ApplicationContext} 中对应类型的 Bean.
     * 
     * @param beanType - Bean 的类型
     * 
     * @throws BeansException
     */
    public <T> T getBean(Class<T> beanType) throws BeansException {
        return beanType.cast(BeanFactoryUtils.beanOfType(rootContext, beanType));
    }

    /**
     * 返回对应类型的唯一 Bean, 如果当前 ApplicationContext 没有，则查找祖先
     * {@link ApplicationContext} 中对应类型的 Bean.
     * 
     * @param beanType - Bean 的类型
     * 
     * @throws BeansException
     */
    public <T> T getBeanIncludingAncestors(Class<T> beanType) throws BeansException {
        return beanType.cast(BeanFactoryUtils.beanOfTypeIncludingAncestors(rootContext, beanType));
    }
}
