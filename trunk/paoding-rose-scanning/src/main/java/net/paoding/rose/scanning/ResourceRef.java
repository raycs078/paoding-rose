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

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.core.io.Resource;

/**
 * 
 * 
 * @author zhiliang.wang 王志亮 [qieqie.wang@gmail.com]
 */
public class ResourceRef {

    private Properties properties;

    private Resource resource;

    private String[] modifiers;

    public ResourceRef(Resource resource, String[] modifiers, Properties p) {
        setResource(resource);
        setModifiers(modifiers);
        this.properties = p;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public String[] getModifiers() {
        return modifiers;
    }

    public void setModifiers(String[] modifiers) {
        this.modifiers = modifiers;
    }

    public boolean hasModifier(String modifier) {
        return ArrayUtils.contains(modifiers, "**") || ArrayUtils.contains(modifiers, "*")
                || ArrayUtils.contains(modifiers, modifier);
    }

    public boolean hasNamespace(String namespace) {
        return ArrayUtils.contains(modifiers, "ROOT_NAMESPACE")
                || ArrayUtils.contains(modifiers, namespace);
    }

    @Override
    public int hashCode() {
        return 13 * resource.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (resource == null) return false;
        if (obj instanceof Resource) {
            return resource.equals(obj);
        } else if (obj instanceof ResourceRef) {
            return resource.equals(((ResourceRef) obj).resource);
        }
        return false;
    }

    @Override
    public String toString() {
        try {
            return resource.getURL().getFile() + Arrays.toString(modifiers);
        } catch (IOException e) {
            return resource + Arrays.toString(modifiers);
        }
    }
}
