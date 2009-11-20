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

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author zhiliang.wang 王志亮 [qieqie.wang@gmail.com]
 */
public class RoseScanner {

    private static SoftReference<RoseScanner> softReference;

    public synchronized static RoseScanner getInstance() {
        if (softReference == null || softReference.get() == null) {
            RoseScanner roseScanner = new RoseScanner();
            softReference = new SoftReference<RoseScanner>(roseScanner);
        }
        return softReference.get();
    }

    // -------------------------------------------------------------

    protected Log logger = LogFactory.getLog(getClass());

    protected ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(
            Thread.currentThread().getContextClassLoader());

    private List<ResourceRef> classesFolderResources;

    private List<ResourceRef> jarResources;

    // -------------------------------------------------------------

    private RoseScanner() {
    }

    // -------------------------------------------------------------

    /**
     * 将要被扫描的普通类地址(比如WEB-INF/classes或target/classes之类的地址)
     * 
     * @param resourceLoader
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<ResourceRef> getClassesFolderResources() throws IOException {
        if (classesFolderResources == null) {
            List<ResourceRef> classesFolderResources = new ArrayList<ResourceRef>();
            Enumeration<URL> found = resourcePatternResolver.getClassLoader().getResources("");
            while (found.hasMoreElements()) {
                URL urlObject = found.nextElement();
                if ("file".equals(urlObject.getProtocol())) {
                    File file;
                    try {
                        file = new File(urlObject.toURI());
                    } catch (URISyntaxException e) {
                        throw new IOException(e);
                    }
                    if (file.isFile()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("skip classes folder (not a directory): " + urlObject);
                        }
                        continue;
                    }
                    Resource resource = new FileSystemResource(file);
                    classesFolderResources.add(new ResourceRef(resource, new String[] { "**" }));
                    if (logger.isDebugEnabled()) {
                        logger.debug("add classes folder: " + urlObject);
                    }
                }
            }
            //
            List<ResourceRef> toRemove = new LinkedList<ResourceRef>();
            for (int i = 0; i < classesFolderResources.size(); i++) {
                ResourceRef resourceInfo = classesFolderResources.get(i);
                String path = resourceInfo.getResource().getFile().getAbsolutePath();
                for (int j = i + 1; j < classesFolderResources.size(); j++) {
                    ResourceRef toCheck = classesFolderResources.get(j);
                    String toCheckPath = toCheck.getResource().getFile().getAbsolutePath();
                    if (path.startsWith(toCheckPath)) {
                        toRemove.add(toCheck);
                        if (logger.isDebugEnabled()) {
                            logger.debug("remove classes folder: " + toCheck);
                        }
                    }
                }
            }
            classesFolderResources.removeAll(toRemove);
            //
            this.classesFolderResources = new ArrayList<ResourceRef>(classesFolderResources);
            if (logger.isInfoEnabled()) {
                ResourceRef[] ret = classesFolderResources
                        .toArray(new ResourceRef[classesFolderResources.size()]);
                logger.info("found classes resources: " + Arrays.toString(ret));
            }
        }
        return Collections.unmodifiableList(classesFolderResources);
    }

    /**
     * 将要被扫描的jar资源
     * 
     * @param resourceLoader
     * @return
     * @throws IOException
     */
    public List<ResourceRef> getJarResources() throws IOException {
        if (jarResources == null) {
            List<ResourceRef> jarResources = new LinkedList<ResourceRef>();
            Enumeration<URL> found = resourcePatternResolver.getClassLoader().getResources(
                    "META-INF");
            while (found.hasMoreElements()) {
                URL urlObject = found.nextElement();
                if ("jar".equals(urlObject.getProtocol())) {
                    try {
                        String path = URLDecoder.decode(urlObject.getPath(), "UTF-8"); // fix 20%
                        if (path.startsWith("file:")) {
                            path = path.substring("file:".length(), path.lastIndexOf("!/"));
                        } else {
                            path = path.substring(0, path.lastIndexOf("!/"));
                        }
                        Resource resource = new FileSystemResource(path);
                        String[] modifier = getManifestRoseValue(resource.getFile());
                        if (modifier.length > 0) {
                            jarResources.add(new ResourceRef(resource, modifier));
                            if (logger.isDebugEnabled()) {
                                logger.debug("add jar resource: " + path);
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("not rose jar resource: " + path);
                            }
                        }
                    } catch (Exception e) {
                        logger.error(urlObject, e);
                    }
                }
            }
            this.jarResources = jarResources;
            if (logger.isInfoEnabled()) {
                ResourceRef[] ret = jarResources.toArray(new ResourceRef[jarResources.size()]);
                logger.info("found rose jar resources: " + Arrays.toString(ret));
            }
        }
        return Collections.unmodifiableList(jarResources);
    }

    protected String[] getManifestRoseValue(File pathname) throws IOException {
        JarFile jarFile = new JarFile(pathname);
        Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            return new String[0];
        }
        Attributes attributes = manifest.getMainAttributes();
        String attrValue = attributes.getValue("Rose");
        if (attrValue == null) {
            return new String[0];
        }
        String[] splits = StringUtils.split(attrValue, ",");
        ArrayList<String> result = new ArrayList<String>(splits.length);
        for (String split : splits) {
            split = split.trim();
            if (StringUtils.isNotEmpty(split)) {
                result.add(split);
            }
        }
        return result.toArray(new String[0]);
    }

}
