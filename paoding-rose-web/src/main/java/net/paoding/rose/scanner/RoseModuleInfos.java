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

import static net.paoding.rose.RoseConstants.CONF_MODULE_IGNORED;
import static net.paoding.rose.RoseConstants.CONF_MODULE_PATH;
import static net.paoding.rose.RoseConstants.CONF_PARENT_MODULE_PATH;
import static net.paoding.rose.RoseConstants.CONTROLLERS_DIRECTORY_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.springframework.util.Log4jConfigurer;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class RoseModuleInfos {

    public static void main(String[] args) throws IOException {
        Log4jConfigurer.initLogging("src/test/java/log4j.properties");
        List<ModuleInfo> moduleInfos = new RoseModuleInfos().findModuleInfos();
        System.out.println("context resource="
                + Arrays.toString(moduleInfos.toArray(new ModuleInfo[0])));
    }

    protected Log logger = LogFactory.getLog(RoseModuleInfos.class);

    private List<ModuleInfo> moduleInfoList;

    private Map<FileObject, ModuleInfo> moduleInfoMap;

    public synchronized List<ModuleInfo> findModuleInfos() throws IOException {
        if (moduleInfoList == null) {
            //
            moduleInfoList = new LinkedList<ModuleInfo>();
            moduleInfoMap = new HashMap<FileObject, ModuleInfo>();
            //
            RoseScanner roseScanner = RoseScanner.getInstance();
            List<ResourceInfo> resources = new ArrayList<ResourceInfo>();
            resources.addAll(roseScanner.getClassesFolderResources());
            resources.addAll(roseScanner.getJarResources());
            List<FileObject> rootObjects = new ArrayList<FileObject>();
            FileSystemManager fsManager = VFS.getManager();
            for (ResourceInfo resourceInfo : resources) {
                if (resourceInfo.hasModifier("controllers")) {
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

            afterScanning();

            for (FileObject fileObject : rootObjects) {
                fsManager.closeFileSystem(fileObject.getFileSystem());
            }
        }
        return new ArrayList<ModuleInfo>(moduleInfoList);
    }

    protected void deepScanImpl(FileObject rootObject, FileObject fileObject) {
        try {
            if (!fileObject.getType().equals(FileType.FOLDER)) {
                logger.warn("fileObject shoud be a folder", new IllegalArgumentException());
                return;
            }
            if (CONTROLLERS_DIRECTORY_NAME.equals(fileObject.getName().getBaseName())) {
                handleWithFolder(rootObject, fileObject);
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

    protected void handleWithFolder(FileObject rootObject, FileObject matchedRootFolder)
            throws IOException {
        this.handleWithFolder(rootObject, matchedRootFolder, matchedRootFolder);
    }

    protected void handleWithFolder(FileObject rootObject, FileObject matchedRootFolder,
            FileObject thisFolder) throws IOException {
        String mappingPath = null;
        ModuleInfo parentModuleInfo = moduleInfoMap.get(thisFolder.getParent());
        // 如果rose.properties设置了controllers的module.path?
        FileObject rosePropertiesFile = thisFolder.getChild("rose.properties");// (null if there is no such child.)
        if (rosePropertiesFile != null) {
            Properties p = new Properties();
            InputStream in = rosePropertiesFile.getContent().getInputStream();
            p.load(in);
            in.close();

            // 如果controllers=ignored，则...
            String ignored = p.getProperty(CONF_MODULE_IGNORED, "false").trim();
            if ("true".equalsIgnoreCase(ignored) || "1".equalsIgnoreCase(ignored)) {
                if (logger.isDebugEnabled()) {
                    logger.info("ignored controllers " + thisFolder.getName());
                }
                return;
            }
            mappingPath = p.getProperty(CONF_MODULE_PATH);
            if (mappingPath != null) {
                mappingPath = mappingPath.trim();
                if (mappingPath.indexOf("${" + CONF_PARENT_MODULE_PATH + "}") != -1) {
                    if (thisFolder.getParent() != null) {
                        String replacePath;
                        if (parentModuleInfo == null) {
                            replacePath = "";
                        } else {
                            replacePath = parentModuleInfo.getMappingPath();
                        }
                        mappingPath = mappingPath.replace("${" + CONF_PARENT_MODULE_PATH + "}",
                                replacePath);
                    } else {
                        mappingPath = mappingPath.replace("${" + CONF_PARENT_MODULE_PATH + "}", "");
                    }
                }
                if (mappingPath.length() != 0) {
                    if (mappingPath.charAt(0) != '/') {
                        if (parentModuleInfo != null) {
                            mappingPath = parentModuleInfo.getMappingPath() + "/" + mappingPath;
                        }
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
                    if (!mappingPath.startsWith("/")) {
                        mappingPath = "/" + mappingPath;
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
        ModuleInfo moduleInfo = new ModuleInfo();
        moduleInfo.setMappingPath(mappingPath);
        moduleInfo.setModuleUrl(thisFolder.getURL());
        String relativePackagePath = matchedRootFolder.getName().getRelativeName(
                thisFolder.getName());
        if (relativePackagePath.startsWith("..")) {
            throw new Error("wrong relativePackagePath '" + relativePackagePath + "' for "
                    + thisFolder.getURL());
        }
        moduleInfo.setRelativePackagePath(relativePackagePath);
        moduleInfo.setParent(parentModuleInfo);
        moduleInfoMap.put(thisFolder, moduleInfo);
        moduleInfoList.add(moduleInfo);
        if (logger.isDebugEnabled()) {
            logger.debug("found module '" + mappingPath + "' in " + thisFolder.getURL());
        }

        FileObject[] children = thisFolder.getChildren();
        for (FileObject child : children) {
            if (child.getType().equals(FileType.FILE)) {
                handlerModuleResource(rootObject, thisFolder, child);
            }
        }
        for (FileObject child : children) {
            if (child.getType().equals(FileType.FOLDER)) {
                handleWithFolder(rootObject, matchedRootFolder, child);
            }
        }
    }

    protected void handlerModuleResource(FileObject rootObject, FileObject thisFolder,
            FileObject resource) throws FileSystemException {
        FileName fileName = resource.getName();
        String bn = fileName.getBaseName();
        if (bn.endsWith(".class") && bn.indexOf('$') == -1) {
            addModuleClass(rootObject, thisFolder, resource);
        } else if (bn.startsWith("applicationContext") && bn.endsWith(".xml")) {
            addModuleContext(rootObject, thisFolder, resource);
        } else if (bn.startsWith("messages") && (bn.endsWith(".xml") || bn.endsWith(".properties"))) {
            addModuleMessage(rootObject, thisFolder, resource);
        }
    }

    private void addModuleContext(FileObject rootObject, FileObject thisFolder, FileObject resource)
            throws FileSystemException {
        ModuleInfo moduleInfo = moduleInfoMap.get(thisFolder);
        moduleInfo.addContextResource(resource.getURL());
        if (logger.isDebugEnabled()) {
            logger.debug("module '" + moduleInfo.getMappingPath() + "': found context file, url="
                    + resource.getURL());
        }
    }

    private void addModuleMessage(FileObject rootObject, FileObject thisFolder, FileObject resource)
            throws FileSystemException {
        ModuleInfo moduleInfo = moduleInfoMap.get(thisFolder);
        moduleInfo.addMessageResource(resource.getParent().getURL() + "/messages");
        if (logger.isDebugEnabled()) {
            logger.debug("module '" + moduleInfo.getMappingPath() + "': found messages file, url="
                    + resource.getURL());
        }
    }

    private void addModuleClass(FileObject rootObject, FileObject thisFolder, FileObject resource)
            throws FileSystemException {
        String className = rootObject.getName().getRelativeName(resource.getName());
        className = StringUtils.removeEnd(className, ".class");
        className = className.replace('/', '.');
        ModuleInfo moduleInfo = moduleInfoMap.get(thisFolder);
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

    protected void afterScanning() {
        for (ModuleInfo moduleInfo : moduleInfoMap.values()) {
            if (moduleInfo.getContextResources().size() == 0
                    && moduleInfo.getModuleClasses().size() == 0) {
                moduleInfoList.remove(moduleInfo);
                if (logger.isDebugEnabled()) {
                    logger.debug("remove empty module '" + moduleInfo.getMappingPath() + "' "
                            + moduleInfo.getModuleUrl());
                }
            }
        }
    }

}
