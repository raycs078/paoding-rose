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

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */

public class ModuleResource {

    private String mappingPath;

    private URL moduleUrl;

    // 相对于controllers的地址，以'/'分隔，空串或以'/'开始
    private String modulePath;

    private List<URL> contextResources = new LinkedList<URL>();

    private Set<String> messageBasenames = new HashSet<String>();

    private List<Class<?>> moduleClasses = new LinkedList<Class<?>>();

    private ModuleResource parent;

    public URL getModuleUrl() {
        return moduleUrl;
    }

    public void setModuleUrl(URL moduleUrl) {
        this.moduleUrl = moduleUrl;
    }

    public void setRelativePackagePath(String modulePath) {
        if (".".equals(modulePath)) {
            modulePath = "";
        }
        if (modulePath.length() > 0 && !modulePath.startsWith("/")) {
            modulePath = "/" + modulePath;
        }
        this.modulePath = modulePath;
    }

    public String getModulePath() {
        return modulePath;
    }

    public ModuleResource getParent() {
        return parent;
    }

    public void setParent(ModuleResource parent) {
        this.parent = parent;
    }

    public String getMappingPath() {
        return mappingPath;
    }

    public void setMappingPath(String mappingPath) {
        this.mappingPath = mappingPath;
    }

    public List<URL> getContextResources() {
        return contextResources;
    }

    public void addContextResource(URL contextResource) {
        this.contextResources.add(contextResource);
    }

    public String[] getMessageBasenames() {
        return messageBasenames.toArray(new String[messageBasenames.size()]);
    }

    public void addMessageResource(String messageBasename) {
        this.messageBasenames.add(messageBasename);
    }

    public List<Class<?>> getModuleClasses() {
        return moduleClasses;
    }

    public void addModuleClass(Class<?> moduleClass) {
        this.moduleClasses.add(moduleClass);
    }

    @Override
    public String toString() {
        return "[" + mappingPath + "]url=" + moduleUrl + "; classes="
                + Arrays.toString(moduleClasses.toArray()) + "; applicationContext="
                + Arrays.toString(contextResources.toArray());
    }

}
