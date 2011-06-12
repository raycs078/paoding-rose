/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.context.spring;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.paoding.rose.scanning.ResourceRef;
import net.paoding.rose.scanning.RoseScanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ResourceUtils;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public class JadeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    protected static final Log logger = LogFactory.getLog(JadeBeanFactoryPostProcessor.class);

    private List<TypeFilter> filters;

    /**
     * 
     * @param filters
     */
    public void setExcludeFilters(List<TypeFilter> filters) {
        this.filters = filters;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        if (logger.isInfoEnabled()) {
            logger.info("[jade] starting ...");
        }
        final List<ResourceRef> resources;
        try {
            // 怎么传入scope呢？
            resources = RoseScanner.getInstance().getJarOrClassesFolderResources();
        } catch (IOException e) {
            throw new ApplicationContextException(
                    "error on getJarResources/getClassesFolderResources", e);
        }
        List<String> urls = new LinkedList<String>();
        for (ResourceRef ref : resources) {
            if (ref.hasModifier("dao") || ref.hasModifier("DAO")) {
                try {
                    Resource resource = ref.getResource();
                    File resourceFile = resource.getFile();
                    if (resourceFile.isFile()) {
                        urls.add("jar:file:" + resourceFile.toURI().getPath()
                                + ResourceUtils.JAR_URL_SEPARATOR);
                    } else if (resourceFile.isDirectory()) {
                        urls.add(resourceFile.toURI().toString());
                    }
                } catch (IOException e) {
                    throw new ApplicationContextException("error on resource.getFile", e);
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("[jade] found " + urls.size() + " jade urls: " + urls);
        }
        if (urls.size() > 0) {
            DAOComponentProvider provider = new DAOComponentProvider(true);
            if (filters != null) {
                for (TypeFilter excludeFilter : filters) {
                    provider.addExcludeFilter(excludeFilter);
                }
            }

            Set<String> daoClassNames = new HashSet<String>();

            for (String url : urls) {
                if (logger.isInfoEnabled()) {
                    logger.info("[jade] call 'jade/find'");
                }
                Set<BeanDefinition> dfs = provider.findCandidateComponents(url);
                if (logger.isInfoEnabled()) {
                    logger.info("[jade] found " + dfs.size() + " beanDefinition from '" + url + "'");
                }
                for (BeanDefinition beanDefinition : dfs) {
                    String daoClassName = beanDefinition.getBeanClassName();

                    if (daoClassNames.contains(daoClassName)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("[jade] ignored replicated jade dao class: "
                                    + daoClassName + "  [" + url + "]");
                        }
                        continue;
                    }
                    daoClassNames.add(daoClassName);

                    MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
                    propertyValues.addPropertyValue("DAOClass", daoClassName);
                    ScannedGenericBeanDefinition scannedBeanDefinition = (ScannedGenericBeanDefinition) beanDefinition;
                    scannedBeanDefinition.setPropertyValues(propertyValues);
                    scannedBeanDefinition.setBeanClass(DAOFactoryBean.class);

                    DefaultListableBeanFactory defaultBeanFactory = (DefaultListableBeanFactory) beanFactory;
                    defaultBeanFactory.registerBeanDefinition(daoClassName, beanDefinition);

                    if (logger.isDebugEnabled()) {
                        logger.debug("[jade] register DAO: " + daoClassName);
                    }
                }
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("[jade] exits");
        }
    }

}
