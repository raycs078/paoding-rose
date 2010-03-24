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

import static net.paoding.rose.RoseConstants.CONTROLLERS_DIRECTORY_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.paoding.rose.RoseConstants;
import net.paoding.rose.scanning.ResourceRef;
import net.paoding.rose.scanning.RoseScanner;
import net.paoding.rose.scanning.vfs.FileName;
import net.paoding.rose.scanning.vfs.FileObject;
import net.paoding.rose.scanning.vfs.FileSystemManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class RoseModuleInfos {

    protected Log logger = LogFactory.getLog(RoseModuleInfos.class);

    private List<ModuleResource> moduleResourceList;

    private Map<FileObject, ModuleResource> moduleResourceMap;

    private FileSystemManager fsManager = new FileSystemManager();

    @SuppressWarnings("unchecked")
    public synchronized List<ModuleResource> findModuleResources(String[] namespaces)
            throws IOException {
        if (moduleResourceList == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("do find module resources!");
            }
            //
            moduleResourceList = new LinkedList<ModuleResource>();
            moduleResourceMap = new HashMap<FileObject, ModuleResource>();
            //
            RoseScanner roseScanner = RoseScanner.getInstance();
            List<ResourceRef> resources = new ArrayList<ResourceRef>();
            try {
                // 为兼容旧的scanning // 2010.03.24
                // TODO: 2010.04.24之后应该删除此try-catch代码，直接使用roseScanner.getClassesFolderResources(namespaces)
                Method getClassesFolderResources = RoseScanner.class.getMethod(
                        "getClassesFolderResources", String[].class);
                Method getJarResources = RoseScanner.class.getMethod("getJarResources",
                        String[].class);
                resources.addAll((List<ResourceRef>) getClassesFolderResources.invoke(roseScanner,
                        (Object) namespaces));
                resources.addAll((List<ResourceRef>) getJarResources.invoke(roseScanner,
                        (Object) namespaces));
            } catch (NoSuchMethodException e) {
                if (namespaces != null && namespaces.length > 0) {
                    throw new IllegalStateException(
                            "PLEASE UPDATE paoding-rose-scanning.jar for support rose namespaces filter, or remove roseFilter's namespaces init-param");
                }
                logger.warn(//
                        "PLEASE UPDATE paoding-rose-scanning.jar for support rose namespaces filter");
                resources.addAll(roseScanner.getClassesFolderResources());
                resources.addAll(roseScanner.getJarResources());
            } catch (Exception e) {
                logger.error("", e);
            }
            resources.addAll(roseScanner.getClassesFolderResources(namespaces));
            resources.addAll(roseScanner.getJarResources(namespaces));
            List<FileObject> rootObjects = new ArrayList<FileObject>();

            for (ResourceRef resourceRef : resources) {
                if (resourceRef.hasModifier("controllers")) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("try to find controllers: " + resourceRef.getResource());
                    }
                    Resource resource = resourceRef.getResource();
                    File resourceFile = resource.getFile();
                    FileObject rootObject = null;
                    if (resourceFile.isFile()) {
                        String path = "jar:file:" + resourceFile.getAbsolutePath() + "!/";
                        rootObject = fsManager.resolveFile(path);
                    } else if (resourceFile.isDirectory()) {
                        rootObject = fsManager.resolveFile(resourceFile.getAbsolutePath());
                    }
                    if (rootObject == null) {
                        if (logger.isInfoEnabled()) {
                            logger.info("It's not a directory or file: " + resourceFile);
                        }
                        continue;
                    }
                    rootObjects.add(rootObject);
                    try {
                        int oldSize = moduleResourceList.size();
                        deepScanImpl(rootObject, rootObject);
                        int newSize = moduleResourceList.size();
                        if (logger.isInfoEnabled()) {
                            logger.info("got " + (newSize - oldSize) + " modules from " //
                                    + rootObject);
                        }
                    } catch (Exception e) {
                        logger.error("error happend when scanning " + rootObject, e);
                    }
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info("It's not a module/controllers file: "
                                + resourceRef.getResource().getFile());
                    }
                }
            }

            afterScanning();
            logger.info("found " + moduleResourceList.size() + " module resources ");
        } else {

            if (logger.isDebugEnabled()) {
                logger.debug("found cached module resources; size=" + moduleResourceList.size());
            }
        }
        return new ArrayList<ModuleResource>(moduleResourceList);
    }

    protected void deepScanImpl(FileObject rootObject, FileObject fileObject) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug(fileObject + " .getBaseName()=" //
                    + fileObject.getName().getBaseName());
        }
        if (CONTROLLERS_DIRECTORY_NAME.equals(fileObject.getName().getBaseName())) {
            handleWithFolder(rootObject, fileObject);
        } else {
            FileObject[] children = fileObject.getChildren();
            for (FileObject child : children) {
                if (child.getType().hasChildren()) {
                    deepScanImpl(rootObject, child);
                }
            }
        }
    }

    protected void handleWithFolder(FileObject rootObject, FileObject matchedRootFolder)
            throws IOException {
        this.handleWithFolder(rootObject, matchedRootFolder, matchedRootFolder);
    }

    protected void handleWithFolder(FileObject rootObject, FileObject matchedRootFolder,
            FileObject thisFolder) throws IOException {

        String relativePackagePath = matchedRootFolder.getName().getRelativeName(
                thisFolder.getName());
        if (relativePackagePath.startsWith("..")) {
            throw new Error("wrong relativePackagePath '" + relativePackagePath + "' for "
                    + thisFolder.getURL());
        }

        String mappingPath = null;
        ModuleResource parentModuleInfo = moduleResourceMap.get(thisFolder.getParent());
        // 如果rose.properties设置了controllers的module.path?
        FileObject rosePropertiesFile = thisFolder.getChild("rose.properties");// (null if there is no such child.)
        if (rosePropertiesFile != null) {
            Properties p = new Properties();
            InputStream in = rosePropertiesFile.getContent().getInputStream();
            p.load(in);
            in.close();

            // 如果controllers=ignored，则...
            String ignored = p.getProperty(RoseConstants.CONF_MODULE_IGNORED, "false").trim();
            if ("true".equalsIgnoreCase(ignored) || "1".equalsIgnoreCase(ignored)) {
                logger.info("ignored controllers folder: " + thisFolder.getName());
                return;
            }
            mappingPath = p.getProperty(RoseConstants.CONF_MODULE_PATH);
            if (mappingPath != null) {
                mappingPath = mappingPath.trim();
                if (mappingPath.indexOf("${" + RoseConstants.CONF_PARENT_MODULE_PATH + "}") != -1) {
                    if (thisFolder.getParent() != null) {
                        String replacePath;
                        if (parentModuleInfo == null) {
                            replacePath = "";
                        } else {
                            replacePath = parentModuleInfo.getMappingPath();
                        }
                        mappingPath = mappingPath.replace("${"
                                + RoseConstants.CONF_PARENT_MODULE_PATH + "}", replacePath);
                    } else {
                        mappingPath = mappingPath.replace("${"
                                + RoseConstants.CONF_PARENT_MODULE_PATH + "}", "");
                    }
                }
                if (mappingPath.length() != 0 && !mappingPath.startsWith("/")) {
                    if (parentModuleInfo != null) {
                        mappingPath = parentModuleInfo.getMappingPath() + "/" + mappingPath;
                    } else if (StringUtils.isNotEmpty(relativePackagePath)) {
                        mappingPath = relativePackagePath + "/" + mappingPath;
                    } else {
                        mappingPath = "/" + mappingPath;
                    }
                }
                // 空串，或，以/开头的串，不能以/结尾，不能重复/
                if (mappingPath.length() != 0) {
                    while (mappingPath.indexOf("//") != -1) {
                        mappingPath = mappingPath.replace("//", "/");
                    }
                    while (mappingPath.endsWith("/")) {
                        mappingPath = mappingPath.substring(0, mappingPath.length() - 1);
                    }
                }
            }
        }
        if (mappingPath == null) {
            if (parentModuleInfo != null) {
                mappingPath = parentModuleInfo.getMappingPath() + "/"
                        + thisFolder.getName().getBaseName();
            } else {
                mappingPath = "";
            }
        }
        ModuleResource moduleResource = new ModuleResource();
        moduleResource.setMappingPath(mappingPath);
        moduleResource.setModuleUrl(thisFolder.getURL());
        moduleResource.setRelativePackagePath(relativePackagePath);
        moduleResource.setParent(parentModuleInfo);
        moduleResourceMap.put(thisFolder, moduleResource);
        moduleResourceList.add(moduleResource);
        if (logger.isDebugEnabled()) {
            logger.debug("found module '" + mappingPath + "' in " + thisFolder.getURL());
        }

        FileObject[] children = thisFolder.getChildren();
        for (FileObject child : children) {
            if (child.getType().hasContent() && !child.getType().hasChildren()) {
                handlerModuleResource(rootObject, thisFolder, child);
            }
        }
        for (FileObject child : children) {
            if (child.getType().hasChildren()) {
                handleWithFolder(rootObject, matchedRootFolder, child);
            }
        }
    }

    protected void handlerModuleResource(FileObject rootObject, FileObject thisFolder,
            FileObject resource) throws IOException {
        FileName fileName = resource.getName();
        String bn = fileName.getBaseName();
        if (logger.isDebugEnabled()) {
            logger.debug("handlerModuleResource baseName=" + bn + "; file="
                    + fileName.getFileObject());
        }
        if (bn.endsWith(".class") && bn.indexOf('$') == -1) {
            addModuleClass(rootObject, thisFolder, resource);
        } else if (bn.startsWith("applicationContext") && bn.endsWith(".xml")) {
            addModuleContext(rootObject, thisFolder, resource);
        } else if (bn.startsWith("messages") && (bn.endsWith(".xml") || bn.endsWith(".properties"))) {
            addModuleMessage(rootObject, thisFolder, resource);
        }
    }

    private void addModuleContext(FileObject rootObject, FileObject thisFolder, FileObject resource)
            throws IOException {
        ModuleResource moduleInfo = moduleResourceMap.get(thisFolder);
        moduleInfo.addContextResource(resource.getURL());
        if (logger.isDebugEnabled()) {
            logger.debug("module '" + moduleInfo.getMappingPath() + "': found context file, url="
                    + resource.getURL());
        }
    }

    private void addModuleMessage(FileObject rootObject, FileObject thisFolder, FileObject resource)
            throws IOException {
        ModuleResource moduleInfo = moduleResourceMap.get(thisFolder);
        String directory = resource.getParent().getURL().toString();
        if (directory.endsWith("/")) {
            moduleInfo.addMessageResource(directory + "messages");
        } else {
            moduleInfo.addMessageResource(directory + "/messages");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("module '" + moduleInfo.getMappingPath() + "': found messages file, url="
                    + resource.getURL());
        }
    }

    private void addModuleClass(FileObject rootObject, FileObject thisFolder, FileObject resource)
            throws IOException {
        String className = rootObject.getName().getRelativeName(resource.getName());
        className = StringUtils.removeEnd(className, ".class");
        className = StringUtils.removeStart(className, "/");
        className = className.replace('/', '.');
        ModuleResource moduleInfo = moduleResourceMap.get(thisFolder);
        try {
            // TODO: classloader...
            moduleInfo.addModuleClass(Class.forName(className));
            if (logger.isDebugEnabled()) {
                logger.debug("module '" + moduleInfo.getMappingPath() + "': found class, name="
                        + className);
            }
        } catch (ClassNotFoundException e) {
            logger.error("", e);
        }
    }

    // FIXME: 如果一个module只有rose.properties文件也会从moduleInfoList中remove，以后是否需要修改？
    protected void afterScanning() {
        for (ModuleResource moduleResource : moduleResourceMap.values()) {
            if (moduleResource.getContextResources().size() == 0
                    && moduleResource.getModuleClasses().size() == 0) {
                moduleResourceList.remove(moduleResource);
                if (logger.isInfoEnabled()) {
                    logger.info("remove empty module '" + moduleResource.getMappingPath() + "' "
                            + moduleResource.getModuleUrl());
                }
            }
        }
    }

}
