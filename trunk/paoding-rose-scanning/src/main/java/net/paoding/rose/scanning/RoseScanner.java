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
package net.paoding.rose.scanning;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

    protected Date createTime = new Date();

    protected ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private List<ResourceRef> classesFolderResources;

    private List<ResourceRef> jarResources;

    // -------------------------------------------------------------

    private RoseScanner() {
    }

    public Date getCreateTime() {
        return createTime;
    }

    // -------------------------------------------------------------
    public List<ResourceRef> getJarOrClassesFolderResources() throws IOException {
        return getJarOrClassesFolderResources(null);
    }

    public List<ResourceRef> getJarOrClassesFolderResources(String[] scope) throws IOException {
        List<ResourceRef> resources;
        if (scope == null) {
            resources = new LinkedList<ResourceRef>();
            resources.addAll(getClassesFolderResources());
            resources.addAll(getJarResources());
        } else if (scope.length == 0) {
            return new ArrayList<ResourceRef>();
        } else {
            resources = new LinkedList<ResourceRef>();
            for (String namespace : scope) {
                String packagePath = namespace.replace('.', '/');
                Resource[] packageResources = resourcePatternResolver.getResources(packagePath);
                for (Resource pkgResource : packageResources) {
                    String uri = pkgResource.getURI().toString();
                    uri = StringUtils.removeEnd(uri, "/");
                    packagePath = StringUtils.removeEnd(packagePath, "/");
                    uri = StringUtils.removeEnd(uri, packagePath);
                    int beginIndex = uri.lastIndexOf("file:");
                    if (beginIndex == -1) {
                        beginIndex = 0;
                    } else {
                        beginIndex += "file:".length();
                    }
                    int endIndex = uri.lastIndexOf('!');
                    if (endIndex == -1) {
                        endIndex = uri.length();
                    }
                    String path = uri.substring(beginIndex, endIndex);
                    Resource folder = new FileSystemResource(path);
                    ResourceRef ref = ResourceRef.toResourceRef(folder);
                    if (!resources.contains(ref)) {
                        resources.add(ref);
                    }
                }
            }
        }
        return resources;
    }

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
            if (logger.isInfoEnabled()) {
                logger.info("start to get classes folder resources");
            }
            List<ResourceRef> classesFolderResources = new ArrayList<ResourceRef>();
            Enumeration<URL> founds = resourcePatternResolver.getClassLoader().getResources("");
            while (founds.hasMoreElements()) {
                URL urlObject = founds.nextElement();
                String path = urlObject.getPath();
                path = StringUtils.removeEnd(path, "/");
                if (!path.endsWith("/classes") && !path.endsWith("/bin")) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("skip classes folder (not ends with classes or bin): "
                                + urlObject);
                    }
                    continue;
                }
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
                    String[] modifier = new String[] { "**" };
                    Properties p = new Properties();
                    Resource rosePropertiesResource = new UrlResource(urlObject.toString()
                            + "/META-INF/rose.properties");
                    if (rosePropertiesResource.exists()) {
                        InputStream in = rosePropertiesResource.getInputStream();
                        p.load(in);
                        in.close();
                        if (StringUtils.isNotBlank(p.getProperty("rose"))) {
                            modifier = StringUtils.split(p.getProperty("rose"), " ,;");
                        }
                    }
                    ResourceRef resourceRef = new ResourceRef(resource, modifier, p);
                    if (classesFolderResources.contains(resourceRef)) {
                        // 删除重复的地址
                        if (logger.isDebugEnabled()) {
                            logger.debug("remove replicated classes folder: " + resourceRef);
                        }
                    } else {
                        classesFolderResources.add(resourceRef);
                        if (logger.isDebugEnabled()) {
                            logger.debug("add classes folder: " + resourceRef);
                        }
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("skip classes folder (not a file protocol url): " + urlObject);
                    }
                }
            }
            // 删除含有一个地址包含另外一个地址的
            List<ResourceRef> toRemove = new LinkedList<ResourceRef>();
            for (int i = 0; i < classesFolderResources.size(); i++) {
                ResourceRef resourceInfo = classesFolderResources.get(i);
                //                String path = resourceInfo.getResource().getFile().getAbsolutePath();
                String path = resourceInfo.getResource().getURI().getPath();
                for (int j = i + 1; j < classesFolderResources.size(); j++) {
                    ResourceRef toCheck = classesFolderResources.get(j);
                    String toCheckPath = toCheck.getResource().getURI().getPath();
                    //                    String toCheckPath = toCheck.getResource().getFile().getAbsolutePath();
                    if (path.startsWith(toCheckPath)) {
                        toRemove.add(toCheck);
                        if (logger.isDebugEnabled()) {
                            logger.debug("remove nested classes folder: " + toCheck);
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
            Resource[] metaInfResources = resourcePatternResolver
                    .getResources("classpath*:/META-INF");
            for (Resource metaInfResource : metaInfResources) {
                URL urlObject = metaInfResource.getURL();
                if ("jar".equals(urlObject.getProtocol())) {
                    try {
                        String path = URLDecoder.decode(urlObject.getPath(), "UTF-8"); // fix 20%
                        if (path.startsWith("file:")) {
                            path = path.substring("file:".length(), path.lastIndexOf('!'));
                        } else {
                            path = path.substring(0, path.lastIndexOf('!'));
                        }
                        Resource resource = new FileSystemResource(path);
                        if (jarResources.contains(resource)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("skip replicated jar resource: " + path);// 在多个 linux环境 下发现有重复,fix it!
                            }
                        } else {
                            ResourceRef ref = ResourceRef.toResourceRef(resource);
                            if (ref.getModifiers() != null) {
                                jarResources.add(ref);
                                if (logger.isInfoEnabled()) {
                                    logger.info("add jar resource: " + ref);
                                }
                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("not rose jar resource: " + path);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error(urlObject, e);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("not rose type(not a jar) " + urlObject);
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

}
