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
package net.paoding.rose.ar.hibernate;

import static net.paoding.rose.RoseConstants.DOMAIN_DIRECTORY_NAME;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.paoding.rose.scanner.ResourceRef;
import net.paoding.rose.scanner.RoseScanner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.springframework.core.io.Resource;

/**
 * 
 * @author zhiliang.wang [qieqie.wang@paoding.net]
 * @since 0.1
 */

public class RoseDomainClasses {

    protected Log logger = LogFactory.getLog(RoseDomainClasses.class);

    private List<Class<?>> domainClasses;

    public synchronized List<Class<?>> findDomainClasses() throws IOException {
        if (domainClasses == null) {
            domainClasses = new ArrayList<Class<?>>();
            RoseScanner roseScanner = RoseScanner.getInstance();
            List<ResourceRef> resources = new ArrayList<ResourceRef>();
            resources.addAll(roseScanner.getClassesFolderResources());
            resources.addAll(roseScanner.getJarResources());
            List<FileObject> rootObjects = new ArrayList<FileObject>();
            FileSystemManager fsManager = VFS.getManager();
            for (ResourceRef resourceInfo : resources) {
                if (resourceInfo.hasModifier("domain")) {
                    Resource resource = resourceInfo.getResource();
                    File resourceFile = resource.getFile();
                    FileObject rootObject = null;
                    if (resourceFile.isFile()) {
                        String path = "jar:file:" + resourceFile.getAbsolutePath() + "!/";
                        rootObject = fsManager.resolveFile(path);
                    } else if (resourceFile.isDirectory()) {
                        rootObject = fsManager.resolveFile(resourceFile.getAbsolutePath());
                    }
                    if (rootObject == null) {
                        continue;
                    }
                    rootObjects.add(rootObject);
                    try {
                        deepScanImpl(rootObject, rootObject);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        return new ArrayList<Class<?>>(domainClasses);
    }

    protected void deepScanImpl(FileObject rootObject, FileObject fileObject) {
        try {
            if (!fileObject.getType().equals(FileType.FOLDER)) {
                logger.warn("fileObject shoud be a folder", new IllegalArgumentException());
                return;
            }
            if (DOMAIN_DIRECTORY_NAME.equals(fileObject.getName().getBaseName())) {
                handleWithFolder(rootObject, fileObject, fileObject);
            } else {
                FileObject[] children = fileObject.getChildren();
                for (FileObject child : children) {
                    if (child.getType().equals(FileType.FOLDER)) {
                        deepScanImpl(rootObject, child);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    protected void handleWithFolder(FileObject rootObject, FileObject matchedRootFolder,
            FileObject thisFolder) throws IOException {
        logger.info("found domain folder in " + thisFolder);
        FileObject[] children = thisFolder.getChildren();

        // 分两个循环，先处理类文件，再处理子目录，使日志更清晰
        for (FileObject child : children) {
            if (!child.getType().equals(FileType.FOLDER)) {
                handleDomainResource(rootObject, child);
            }
        }
        for (FileObject child : children) {
            if (child.getType().equals(FileType.FOLDER)) {
                handleWithFolder(rootObject, matchedRootFolder, child);
            }
        }
    }

    protected void handleDomainResource(FileObject rootObject, FileObject resource)
            throws FileSystemException {
        FileName fileName = resource.getName();
        String bn = fileName.getBaseName();
        if (bn.endsWith(".class") && bn.indexOf('$') == -1) {
            addDomainClass(rootObject, resource);
        }
    }

    private void addDomainClass(FileObject rootObject, FileObject resource)
            throws FileSystemException {
        String className = rootObject.getName().getRelativeName(resource.getName());
        className = StringUtils.removeEnd(className, ".class");
        className = className.replace('/', '.');
        for (int i = domainClasses.size() - 1; i >= 0; i--) {
            Class<?> clazz = domainClasses.get(i);
            if (clazz.getName().equals(className)) {
                logger.info("domain: skip replicated class " + className + " in "
                        + resource.getName());
                return;
            }
        }
        try {
            domainClasses.add(Class.forName(className));
            logger.info("domain: found class, name=" + className);
        } catch (ClassNotFoundException e) {
            logger.error("", e);
        }
    }
}
