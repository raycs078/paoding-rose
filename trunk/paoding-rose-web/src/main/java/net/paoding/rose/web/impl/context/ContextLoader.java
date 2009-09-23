/*
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.paoding.rose.web.impl.context;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * {@link ContextLoader}用于创建 {@link WebApplicationContext}对象
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ContextLoader {

    private static final Log logger = LogFactory.getLog(ContextLoader.class);

    /**
     * 
     * @param servletContext
     * @param parent
     * @param contextResources
     * @param namespace
     * @return
     * @throws IOException
     */
    public static XmlWebApplicationContext createWebApplicationContext(
            ServletContext servletContext, WebApplicationContext parent,
            final List<Resource> contextResources, final String[] messageBasenames, String namespace)
            throws IOException {

        long startTime = System.currentTimeMillis();
        logger.info(namespace + " WebApplicationContext: initialization started");
        servletContext.log("Loading Spring " + namespace + " WebApplicationContext");

        ResourceXmlWebApplicationContext wac = new ResourceXmlWebApplicationContext();
        wac.setContextResources(contextResources);
        wac.setMessageBaseNames(messageBasenames);
        wac.setNamespace(namespace);
        wac.setConfigLocations(new String[0]);
        wac.setId("rose.ResourceXmlWebApplicationContext@" + namespace);
        wac.setServletContext(servletContext);
        if (parent != null) {
            wac.setParent(parent);
            wac.setId(namespace + "," + parent.getId());
        } else {
            wac.setId(namespace);
        }
        wac.refresh();
        addBeanPostProcessors(wac);

        // 日志打印
        if (logger.isDebugEnabled()) {
            logger.debug("Using context class [" + wac.getClass().getName() + "] for " + namespace
                    + " WebApplicationContext");
        }
        if (logger.isInfoEnabled()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            logger.info(namespace + " WebApplicationContext: initialization completed in "
                    + elapsedTime + " ms");
        }
        return wac;
    }

    /**
     * 根据构造函数设置的配置文件地址，创建Spring应用上下文环境对象
     * 
     * @param servletContext
     * @return
     * @throws IOException
     */
    public static XmlWebApplicationContext createWebApplicationContext(
            ServletContext servletContext, WebApplicationContext parent, String configLocation,
            String[] messageBaseNames, String namespace) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info(namespace + " WebApplicationContext: initialization started");
        servletContext.log("Loading Spring " + namespace + " WebApplicationContext");

        ResourceXmlWebApplicationContext wac = new ResourceXmlWebApplicationContext();
        wac.setConfigLocation(configLocation);
        wac.setMessageBaseNames(messageBaseNames);
        wac.setServletContext(servletContext);
        wac.setNamespace(namespace);
        if (parent != null) {
            wac.setParent(parent);
            wac.setId(namespace + "," + parent.getId());
        } else {
            wac.setId(namespace);
        }
        if (configLocation != null) {
            String[] splits = StringUtils.tokenizeToStringArray(configLocation,
                    ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS);
            wac.setConfigLocations(splits);
        }
        //        registerMessageSource(wac, messageResources);
        wac.refresh();
        addBeanPostProcessors(wac);

        // 日志打印
        if (logger.isDebugEnabled()) {
            logger.debug("Using context class [" + wac.getClass().getName() + "] for " + namespace
                    + " WebApplicationContext");
        }
        if (logger.isInfoEnabled()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            logger.info(namespace + " WebApplicationContext: initialization completed in "
                    + elapsedTime + " ms");
        }
        return wac;

    }

    private static void addBeanPostProcessors(XmlWebApplicationContext context) {
        boolean autowiredAnnotationBeanPostProcessorDefined = false;
        boolean commonAnnotationBeanPostProcessorDefined = false;
        boolean persistenceAnnotationBeanPostProcessorDefined = false;
        // 为何不直接使用PersistenceAnnotationBeanPostProcessor判断？==>这让程序可以选择不依赖于jpa，如果程序员觉得不需要的话
        Class<?> persistenceAnnotationBeanPostProcessorClass = null;
        try {
            Class.forName("javax.persistence.EntityManager");
            try {
                persistenceAnnotationBeanPostProcessorClass = Class
                        .forName("org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor");
            } catch (ClassNotFoundException e1) {
                logger.error("is spring too old: " + e1.getMessage(), e1);
            }
        } catch (ClassNotFoundException e) {
            // 找不到jpa的类，表示不用使用persistenceAnnotationBeanPostProcessor
        }

        String[] beanDefinitionNames = context.getBeanFactory().getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = context.getBeanFactory().getBeanDefinition(
                    beanDefinitionName);
            if (!autowiredAnnotationBeanPostProcessorDefined
                    && beanDefinition.getBeanClassName().equals(
                            AutowiredAnnotationBeanPostProcessor.class.getName())) {
                autowiredAnnotationBeanPostProcessorDefined = true;
            }
            if (!commonAnnotationBeanPostProcessorDefined
                    && beanDefinition.getBeanClassName().equals(
                            CommonAnnotationBeanPostProcessor.class.getName())) {
                commonAnnotationBeanPostProcessorDefined = true;
            }
            if (persistenceAnnotationBeanPostProcessorClass != null
                    && !persistenceAnnotationBeanPostProcessorDefined
                    && beanDefinition.getBeanClassName().equals(
                            persistenceAnnotationBeanPostProcessorClass.getName())) {
                persistenceAnnotationBeanPostProcessorDefined = true;
            }
        }
        if (!autowiredAnnotationBeanPostProcessorDefined) {
            addAutowiredAnnotationBeanPostProcessor(context);
        }
        if (!commonAnnotationBeanPostProcessorDefined) {
            addCommonAnnotationBeanPostProcessor(context);
        }
        if (persistenceAnnotationBeanPostProcessorClass != null
                && !persistenceAnnotationBeanPostProcessorDefined) {
            addPersistenceAnnotationBeanPostProcessor(context,
                    persistenceAnnotationBeanPostProcessorClass);
        }
    }

    private static void addAutowiredAnnotationBeanPostProcessor(XmlWebApplicationContext context) {
        AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
        autowiredAnnotationBeanPostProcessor.setBeanFactory(context.getBeanFactory());
        context.getBeanFactory().addBeanPostProcessor(autowiredAnnotationBeanPostProcessor);
        logger.debug("context " + context.getNamespace()
                + ": add autowiredAnnotationBeanPostProcessor");
    }

    private static void addCommonAnnotationBeanPostProcessor(XmlWebApplicationContext context) {
        CommonAnnotationBeanPostProcessor commonAnnotationBeanPostProcessor = new CommonAnnotationBeanPostProcessor();
        commonAnnotationBeanPostProcessor.setBeanFactory(context.getBeanFactory());
        context.getBeanFactory().addBeanPostProcessor(commonAnnotationBeanPostProcessor);
        logger.debug("context " + context.getNamespace()
                + ": add commonAnnotationBeanPostProcessor");
    }

    private static void addPersistenceAnnotationBeanPostProcessor(XmlWebApplicationContext context,
            Class<?> persistenceAnnotationBeanPostProcessorClass) {
        // 
        BeanPostProcessor persistenceAnnotationBeanPostProcessor = (BeanPostProcessor) BeanUtils
                .instantiateClass(persistenceAnnotationBeanPostProcessorClass);
        Method setBeanFactory = ReflectionUtils.findMethod(
                persistenceAnnotationBeanPostProcessorClass, "setBeanFactory",
                new Class[] { BeanFactory.class });
        ReflectionUtils.invokeMethod(setBeanFactory, persistenceAnnotationBeanPostProcessor,
                new Object[] { context.getBeanFactory() });
        //
        context.getBeanFactory().addBeanPostProcessor(persistenceAnnotationBeanPostProcessor);
        logger.debug("context " + context.getNamespace()
                + ": add persistenceAnnotationBeanPostProcessor");
    }

    public static List<Resource> toResources(List<URL> contextResources) {
        List<Resource> resources = new ArrayList<Resource>();
        for (URL url : contextResources) {
            resources.add(new UrlResource(url));
        }
        return resources;
    }
}
