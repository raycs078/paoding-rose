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
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * 
 * 
 * @author zhiliang.wang 王志亮 [qieqie.wang@gmail.com]
 */
public class ResourceRef {

    private Properties properties = new Properties();

    private Resource resource;

    private String[] modifiers;

    public static ResourceRef toResourceRef(Resource folder) throws IOException {
        ResourceRef rr = new ResourceRef(folder, null, new Properties());
        String[] modifiers = null;
        Resource rosePropertiesResource = rr.getInnerResource("/META-INF/rose.properties");
        if (rosePropertiesResource.exists()) {
            InputStream in = rosePropertiesResource.getInputStream();
            rr.getProperties().load(in);
            in.close();
            if (StringUtils.isNotEmpty(rr.getProperties().getProperty("rose"))) {
                modifiers = StringUtils.split(rr.getProperties().getProperty("rose"), ", ;\n\r\t");
            }
        } else {
            JarFile jarFile = new JarFile(rr.getResource().getFile());
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                Attributes attributes = manifest.getMainAttributes();
                String attrValue = attributes.getValue("Rose");
                if (attrValue != null) {
                    modifiers = StringUtils.split(rr.getProperties().getProperty("rose"),
                            ", ;\n\r\t");
                }
            }
        }
        rr.setModifiers(modifiers);
        return rr;
    }

    public ResourceRef(Resource resource, String[] modifiers, Properties p) {
        properties.putAll(p);
        setResource(resource);
        setModifiers(modifiers);
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
        StringBuilder sb = new StringBuilder();
        final String separator = ", ";
        for (String m : modifiers) {
            sb.append(m).append(separator);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - separator.length());
        }
        properties.put("rose", sb.toString());
    }

    public boolean hasModifier(String modifier) {
        return ArrayUtils.contains(modifiers, "**") || ArrayUtils.contains(modifiers, "*")
                || ArrayUtils.contains(modifiers, modifier);
    }

    public boolean hasNamespace(String namespace) throws IOException {
        boolean hasNamespaceByMetaInf = ArrayUtils.contains(modifiers, "ROOT_NAMESPACE")
                || ArrayUtils.contains(modifiers, namespace);
        if (!hasNamespaceByMetaInf) {
            String packagePath = namespace.replace('.', '/');
            Resource packageResource = getInnerResource(packagePath);
            if (packageResource != null && packageResource.exists()) {
                return true;
            }
        }
        return hasNamespaceByMetaInf;
    }

    public Resource getInnerResource(String subPath) throws IOException {
        if (!subPath.startsWith("/")) {
            subPath = "/" + subPath;
        }
        if (getProtocol().equals("jar")) {
            return new UrlResource("jar:file:" + resource.getFile().getPath() + "!" + subPath);
        } else {
            return new FileSystemResource("file:" + resource.getFile().getPath() + subPath);
        }
    }

    public Resource[] getInnerResources(ResourcePatternResolver resourcePatternResolver,
            String subPath) throws IOException {
        subPath = getInnerPath(subPath);
        return resourcePatternResolver.getResources(subPath);
    }

    public String getInnerPath(String subPath) throws IOException {
        if (!subPath.startsWith("/")) {
            subPath = "/" + subPath;
        }
        if (getProtocol().equals("jar")) {
            subPath = "jar:file:" + resource.getFile().getPath() + "!" + subPath;
        } else {
            subPath = "file:" + resource.getFile().getPath() + subPath;
        }
        return subPath;
    }

    public String getProtocol() {
        if (resource.getFilename().toLowerCase().endsWith(".jar")) {
            return "jar";
        }
        return "file";
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
