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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class SimpleFileObject implements FileObject {

    private File file;

    public SimpleFileObject(String path) {
        file = new File(path);
    }

    public SimpleFileObject(File file) {
        this.file = file;
    }

    @Override
    public FileObject getChild(final String child) {
        File[] files = file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().equals(child)) {
                    return true;
                }
                return false;
            }

        });
        return files.length == 0 ? null : new SimpleFileObject(files[0].getPath());
    }

    @Override
    public FileObject[] getChildren() {
        File[] files = file.listFiles();
        FileObject[] children = new FileObject[files.length];
        for (int i = 0; i < children.length; i++) {
            children[i] = new SimpleFileObject(files[i].getPath());
        }
        return children;
    }

    @Override
    public FileContent getContent() {
        return new FileContent() {

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }
        };
    }

    @Override
    public FileName getName() {
        return new FileName() {

            @Override
            public FileObject getFileObject() {
                return SimpleFileObject.this;
            }

            @Override
            public String getBaseName() {
                return file.getName();
            }

            @Override
            public String getRelativeName(FileName name) {
                SimpleFileObject relative = (SimpleFileObject) name.getFileObject();
                String relativePath = relative.file.getAbsolutePath().replace("\\", "/");
                String thisPath = file.getAbsolutePath().replace("\\", "/");
                if (!relativePath.startsWith(thisPath)) {
                    throw new IllegalArgumentException("[thisPath=" + thisPath + "] [relative="
                            + relative + "]");
                }
                return relativePath.substring(thisPath.length());
            }
        };
    }

    @Override
    public FileObject getParent() {
        File parent = file.getParentFile();
        if (parent == null) {
            return null;
        }
        return new SimpleFileObject(parent);
    }

    @Override
    public FileType getType() {
        if (file.isFile()) {
            return FileType.FILE;
        } else if (file.isDirectory()) {
            return FileType.FOLDER;
        }
        return FileType.UNKNOWN;
    }

    @Override
    public URL getURL() throws MalformedURLException {
        return file.toURI().toURL();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleFileObject)) {
            return false;
        }
        SimpleFileObject t = (SimpleFileObject) obj;
        return this.file.equals(t.file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public String toString() {
        return file.toString();
    }

}
