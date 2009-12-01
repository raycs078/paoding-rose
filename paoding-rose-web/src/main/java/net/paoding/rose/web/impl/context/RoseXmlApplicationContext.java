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
package net.paoding.rose.web.impl.context;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * 
 * @author han.liao [in355hz@gmail.com]
 * 
 */
public class RoseXmlApplicationContext extends AbstractXmlApplicationContext {

    private List<Resource> contextResources = Collections.emptyList();

    public RoseXmlApplicationContext() {
    }

    public void setContextResources(List<Resource> contextResources) {
        this.contextResources = contextResources;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    protected Resource[] getConfigResources() {
        return contextResources.toArray(new Resource[0]);
    }

    @Override
    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        prepareBeanFactoryByRose(beanFactory);
        super.prepareBeanFactory(beanFactory);
    }

    /** Rose对BeanFactory的特殊处理，必要时可以覆盖这个方法去掉Rose的特有的处理 */
    protected void prepareBeanFactoryByRose(ConfigurableListableBeanFactory beanFactory) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        AnnotationConfigUtils.registerAnnotationConfigProcessors(registry);
    }
}
