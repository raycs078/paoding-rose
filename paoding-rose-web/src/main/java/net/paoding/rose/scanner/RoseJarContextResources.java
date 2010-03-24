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
import java.util.LinkedList;
import java.util.List;

import net.paoding.rose.scanning.ResourceRef;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class RoseJarContextResources {

    protected static Log logger = LogFactory.getLog(RoseJarContextResources.class);

    public static List<Resource> findContextResources(String[] namespaces) throws IOException {
        List<ResourceRef> folders = RoseFolders.getRoseFolders(namespaces);
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(
                ClassUtils.getDefaultClassLoader());
        List<Resource> ctxResources = new LinkedList<Resource>();
        for (ResourceRef resourceInfo : folders) {
            if (resourceInfo.hasModifier("applicationContext")) {
                Resource resource = resourceInfo.getResource();
                String path = resource.getFile().getAbsolutePath().replace('\\', '/');
                String ctxPath;
                if (resource.getFilename().endsWith(".jar")) {
                    ctxPath = "jar:file:" + path + "!/applicationContext*.xml";
                } else {
                    ctxPath = "file:" + path + "/applicationContext*.xml";
                }
                Resource[] founds = resourcePatternResolver.getResources(ctxPath);
                for (Resource found : founds) {
                    ctxResources.add(found);
                    if (logger.isDebugEnabled()) {
                        logger.debug("add applicationContext resource: " + found);
                    }
                }
            }
        }
        return ctxResources;
    }

    public static String[] findMessageBasenames(String[] namespaces) throws IOException {
        List<ResourceRef> folders = RoseFolders.getRoseFolders(namespaces);
        List<String> ctxResources = new LinkedList<String>();
        for (ResourceRef resourceInfo : folders) {
            if (resourceInfo.hasModifier("messages")) {
                Resource resource = resourceInfo.getResource();
                String path = resource.getFile().getAbsolutePath().replace('\\', '/');
                String msgPath;
                if (resource.getFilename().endsWith(".jar")) {
                    msgPath = "jar:file:" + path + "!/messages*";
                } else {
                    msgPath = "file:" + path + "/messages*";
                }
                ctxResources.add(msgPath);
            }
        }
        return ctxResources.toArray(new String[0]);
    }
}
