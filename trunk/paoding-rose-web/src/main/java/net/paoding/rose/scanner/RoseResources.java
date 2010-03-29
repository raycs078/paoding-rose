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
package net.paoding.rose.scanner;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.paoding.rose.scanning.LoadScope;
import net.paoding.rose.scanning.ResourceRef;
import net.paoding.rose.scanning.RoseScanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class RoseResources {

    protected static Log logger = LogFactory.getLog(RoseResources.class);

    public static List<Resource> findContextResources(LoadScope load) throws IOException {
        String[] scope = load.getScope("applicationContext");
        List<ResourceRef> resources;
        if (scope == null) {
            resources = new LinkedList<ResourceRef>();
            resources.addAll(RoseScanner.getInstance().getClassesFolderResources());
            resources.addAll(RoseScanner.getInstance().getJarResources());
        } else {
            resources = RoseScanner.getInstance().getJarOrClassesFolderResources(scope);
        }
        List<Resource> ctxResources = new LinkedList<Resource>();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        for (ResourceRef ref : resources) {
            if (ref.hasModifier("applicationContext")) {
                Resource[] founds = ref.getInnerResources(resourcePatternResolver,
                        "/applicationContext*.xml");
                List<Resource> asList = Arrays.asList(founds);
                ctxResources.addAll(asList);
                if (logger.isDebugEnabled()) {
                    logger.debug("add applicationContext resources: " + asList);
                }
            }
        }
        return ctxResources;
    }

    public static String[] findMessageBasenames(LoadScope load) throws IOException {
        String[] scope = load.getScope("messages");
        List<ResourceRef> resources;
        if (scope == null) {
            resources = new LinkedList<ResourceRef>();
            resources.addAll(RoseScanner.getInstance().getClassesFolderResources());
            resources.addAll(RoseScanner.getInstance().getJarResources());
        } else {
            resources = RoseScanner.getInstance().getJarOrClassesFolderResources(scope);
        }
        List<String> messagesResources = new LinkedList<String>();
        for (ResourceRef ref : resources) {
            if (ref.hasModifier("messages")) {
                messagesResources.add(ref.getInnerPath("/messages"));
            }
        }
        return messagesResources.toArray(new String[0]);
    }
}
