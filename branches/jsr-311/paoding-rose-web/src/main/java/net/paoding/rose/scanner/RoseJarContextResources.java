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

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.Log4jConfigurer;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class RoseJarContextResources {

    public static void main(String[] args) throws IOException {
        Log4jConfigurer.initLogging("src/test/java/log4j.properties");
        List<Resource> resources = RoseJarContextResources.findContextResources();
        System.out.println("context resource="
                + Arrays.toString(resources.toArray(new Resource[0])));
    }

    public static List<Resource> findContextResources() throws IOException {
        List<ResourceRef> jarResources = RoseScanner.getInstance().getJarResources();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(
                ClassUtils.getDefaultClassLoader());
        List<Resource> ctxResources = new LinkedList<Resource>();
        for (ResourceRef resourceInfo : jarResources) {
            if (resourceInfo.hasModifier("applicationContext")) {
                Resource resource = resourceInfo.getResource();
                String jarPath = resource.getFile().getAbsolutePath();
                String ctxPath = "jar:file:" + jarPath + "!/applicationContext*.xml";
                Resource[] founds = resourcePatternResolver.getResources(ctxPath);
                for (Resource found : founds) {
                    ctxResources.add(found);
                }
            }
        }
        return ctxResources;
    }

    public static String[] findMessageBasenames() throws IOException {
        List<ResourceRef> jarResources = RoseScanner.getInstance().getJarResources();
        List<String> ctxResources = new LinkedList<String>();
        for (ResourceRef resourceInfo : jarResources) {
            if (resourceInfo.hasModifier("messages")) {
                Resource resource = resourceInfo.getResource();
                String jarPath = resource.getFile().getAbsolutePath();
                ctxResources.add("jar:file:" + jarPath + "!/messages*");
            }
        }
        return ctxResources.toArray(new String[0]);
    }
}
