/*
 * Copyright 2007-2010 the original author or authors.
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
package net.paoding.rose.scanning.vfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class JarFileObject implements FileObject {

    private static Log logger = LogFactory.getLog(JarFileObject.class);

    private JarFileObject root;

    private JarFile jarFile;

    private JarEntry entry;

    private String path;

    private String name;

    private String jarFilePath;

    public JarFileObject(JarFileObject parent, String childName) throws IOException {
        this.jarFile = parent.jarFile;
        this.root = parent.root;
        this.path = parent.path + childName;
        this.name = StringUtils.removeEnd(childName, "/");
        this.path = this.path.replace("\\", "/");
        this.jarFilePath = parent.jarFilePath;
        entry = jarFile.getJarEntry(path);
        if (entry == null) {
            throw new FileNotFoundException(parent + childName);
        }
    }

    public JarFileObject(String jarPath) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("represent jar: " + jarPath);
        }
        if (jarPath.endsWith("!")) {
            jarPath = jarPath + "/";
        }
        int index = jarPath.indexOf("!");
        if (index > 0) {
            jarFilePath = jarPath.substring(0, index);
            if (jarPath.endsWith("!/") || jarPath.endsWith("!")) {
                this.path = "";
            } else {
                this.path = jarPath.substring(index + 2);
            }
            this.path = this.path.replace('\\', '/');
        } else {
            jarFilePath = jarPath;
            this.path = "";
            this.name = "";
        }
        jarFilePath = new File(jarFilePath).getPath().replace('\\', '/');
        JarFileObject root = null;
        if (StringUtils.isBlank(path)) {
            this.jarFile = new JarFile(jarFilePath);
            this.root = this;
            this.entry = null;
        } else {
            root = new JarFileObject(jarFilePath);
            this.root = root;
            this.jarFile = root.jarFile;

            String folderPath = null;
            if (!path.endsWith("/")) {
                folderPath = path + "/";
            }
            Enumeration<JarEntry> e = jarFile.entries();
            while (e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                if (path.equals(entry.getName())
                        || (folderPath != null && folderPath.equals(entry.getName()))) {
                    this.entry = entry;
                    break;
                }
            }
            if (entry == null) {
                throw new FileNotFoundException(jarPath);
            }
            if (entry.isDirectory() && !this.path.endsWith("/")) {
                this.path = this.path + "/";
            }
            this.name = this.path.substring(this.path.lastIndexOf('/', this.path.length() - 2));
            this.name = StringUtils.removeEnd(name, "/");
        }
    }

    @Override
    public FileObject getChild(String name) throws IOException {
        JarEntry entry = jarFile.getJarEntry(this.path + name);
        if (entry == null) {
            return null;
        }
        return new JarFileObject(this, name);
    }

    @Override
    public FileObject[] getChildren() throws IOException {
        List<FileObject> children = new LinkedList<FileObject>();
        Enumeration<JarEntry> e = jarFile.entries();
        while (e.hasMoreElements()) {
            JarEntry entry = e.nextElement();
            if (entry.getName().startsWith(this.path)
                    && entry.getName().length() > this.path.length()) {
                int index = entry.getName().indexOf('/', this.path.length() + 1);
                if (index == -1 || index == entry.getName().length() - 1) {
                    children.add(new JarFileObject(this, entry.getName().substring(
                            this.path.length())));
                }
            }
        }
        FileObject[] ret = children.toArray(new FileObject[0]);
        if (logger.isDebugEnabled()) {
            if (ret.length == 0) {
                logger.debug("get empty children of " + this);
            }
        }
        return ret;
    }

    @Override
    public FileContent getContent() throws IOException {

        return new FileContent() {

            @Override
            public InputStream getInputStream() throws IOException {
                return getURL().openStream();
            }
        };
    }

    @Override
    public FileName getName() throws IOException {
        return new FileName() {

            @Override
            public FileObject getFileObject() {
                return JarFileObject.this;
            }

            @Override
            public String getBaseName() {
                return name;
            }

            @Override
            public String getRelativeName(FileName name) {
                JarFileObject fjo = (JarFileObject) name.getFileObject();
                if (!jarFilePath.equals(fjo.root.jarFilePath)) {
                    throw new IllegalArgumentException();
                }
                String rootPath = path;
                String thisPath = fjo.path;
                if (!thisPath.startsWith(rootPath)) {
                    throw new IllegalArgumentException();
                }
                return thisPath.substring(rootPath.length());
            }
        };
    }

    @Override
    public FileObject getParent() throws IOException {
        if (entry == null) return null;
        String parentPath = path.substring(0, path.lastIndexOf('/', path.length() - 2));
        return new JarFileObject(jarFilePath + ResourceUtils.JAR_URL_SEPARATOR + parentPath);
    }

    @Override
    public FileType getType() throws IOException {
        return (entry == null || entry.isDirectory()) ? FileType.FOLDER : FileType.FILE;
    }

    @Override
    public URL getURL() throws IOException {
        String url = "jar:file:" + jarFilePath + "!/" + path;
        return new URL(url);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JarFileObject)) {
            return false;
        }
        JarFileObject t = (JarFileObject) obj;
        return this.jarFilePath.equals(t.jarFilePath) && this.path.equals(t.path);
    }

    @Override
    public int hashCode() {
        return jarFilePath.hashCode() * 13 + path.hashCode();
    }

    @Override
    public String toString() {
        return "jar:" + new File(jarFilePath).toURI() + "!/" + path;
    }

}
