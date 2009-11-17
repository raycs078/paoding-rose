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
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * {@link ContextLoader}用于创建 {@link WebApplicationContext}对象
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ContextLoader {

    // 
    private static final Log logger = LogFactory.getLog(ContextLoader.class);

    /**
     * 将 {@link URL} 列表封装为 {@link Resource}列表，使调用者可以调用
     * {@link #createWebApplicationContext(ServletContext, WebApplicationContext, List, String, String[], String)}
     * 
     * @param contextResources
     * @return
     */
    public static List<Resource> toResources(List<URL> contextResources) {
        List<Resource> resources = new ArrayList<Resource>();
        for (URL url : contextResources) {
            resources.add(new UrlResource(url));
        }
        return resources;
    }

    public static XmlWebApplicationContext createWebApplicationContext(
            ServletContext servletContext, // 
            List<Resource> contextResources,//
            String configLocations, //
            String[] messageBasenames, //
            String namespace) throws IOException {
        return createWebApplicationContext(servletContext, null, contextResources, configLocations,
                messageBasenames, namespace);
    }

    public static XmlWebApplicationContext createWebApplicationContext(
            WebApplicationContext parent, //
            List<Resource> contextResources,//
            String configLocations, //
            String[] messageBasenames, //
            String namespace) throws IOException {
        return createWebApplicationContext(null, parent, contextResources, configLocations,
                messageBasenames, namespace);
    }

    /**
     * 
     * @param servletContext 所在的ServletContext对象
     * @param parent 上级applicationContext,为null代表没有
     * @param contextResources 以Resource形式提供的 applicationContext文件资源
     * @param configLocations 以字符串提供的 applicationContext文件地址资源，如
     *        "/WEB-INF/applicationContext*.xml,classpath:applicationContext*.xml"
     * @param messageBasenames 以字符串提供的 messages 资源
     * @param namespace
     * @return
     * @throws IOException
     */
    public static XmlWebApplicationContext createWebApplicationContext(
            ServletContext servletContext, // 
            WebApplicationContext parent, //
            List<Resource> contextResources,//
            String configLocations, //
            String[] messageBasenames, //
            String namespace) throws IOException {

        long startTime = System.currentTimeMillis();

        String loadingMsg = "Loading Spring '" + namespace + "' WebApplicationContext";
        logger.info(loadingMsg);
        if (servletContext == null && parent != null) {
            servletContext = parent.getServletContext();
        }
        if (servletContext != null) {
            servletContext.log(loadingMsg);
        }
        ResourceXmlWebApplicationContext wac = new ResourceXmlWebApplicationContext();
        wac.setServletContext(servletContext);
        wac.setContextResources(contextResources);
        wac.setNamespace(namespace);
        if (configLocations != null) {
            wac.setConfigLocation(configLocations);
        }
        wac.setMessageBaseNames(messageBasenames);
        if (parent != null) {
            wac.setParent(parent);
            wac.setId(parent.getId() + "::" + namespace);
        } else {
            wac.setId(namespace);
        }
        wac.refresh();
        addBeanPostProcessors(wac);

        // 日志打印
        if (logger.isDebugEnabled()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            logger.debug("Using context class [" + wac.getClass().getName() + "] for " + namespace
                    + " WebApplicationContext");
            logger.info(namespace + " WebApplicationContext: initialization completed in "
                    + elapsedTime + " ms");
        }
        return wac;
    }

    // ---------------------------------------------------------------

    private static final String PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME = "org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor";

    private static final boolean jpaPresent = ClassUtils.isPresent(
            "javax.persistence.EntityManagerFactory", ContextLoader.class.getClassLoader())
            && ClassUtils.isPresent(PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME,
                    ContextLoader.class.getClassLoader());

    // org.springframework.context.annotation.AnnotationConfigUtils有相似的处理，但通过它不可行
    private static void addBeanPostProcessors(XmlWebApplicationContext context) {
        boolean autowiredAnnotationBeanPostProcessorDefined = false;
        boolean commonAnnotationBeanPostProcessorDefined = false;
        boolean persistenceAnnotationBeanPostProcessorDefined = false;

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
            if (jpaPresent
                    && !persistenceAnnotationBeanPostProcessorDefined
                    && beanDefinition.getBeanClassName().equals(
                            PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME)) {
                persistenceAnnotationBeanPostProcessorDefined = true;
            }
        }
        if (!autowiredAnnotationBeanPostProcessorDefined) {
            addAutowiredAnnotationBeanPostProcessor(context);
        }
        if (!commonAnnotationBeanPostProcessorDefined) {
            addCommonAnnotationBeanPostProcessor(context);
        }
        if (!persistenceAnnotationBeanPostProcessorDefined) {
            addPersistenceAnnotationBeanPostProcessor(context);
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

    private static void addPersistenceAnnotationBeanPostProcessor(XmlWebApplicationContext context) {
        Class<?> persistenceAnnotationBeanPostProcessorClass = null;
        try {
            persistenceAnnotationBeanPostProcessorClass = ClassUtils.forName(
                    PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME, ContextLoader.class
                            .getClassLoader());
        } catch (Throwable e) {
            throw new Error("", e);
        }
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
}
