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
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class FileSystemManager {

    protected Log logger = LogFactory.getLog(FileSystemManager.class);

    private Map<String, FileObject> cached = new HashMap<String, FileObject>();

    public FileObject resolveFile(String urlString) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("[fs] resolveFile ... by urlString '" + urlString + "'");
        }
        FileObject object = cached.get(urlString);
        if (object == null && !urlString.endsWith("/")) {
            object = cached.get(urlString + "/");
        }
        if (object != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("[fs] found cached file for '" + urlString + "'");
            }
            return object;
        }
        // not found in cache, resolves it!
        return resolveFile(new URL(urlString));
    }

    public synchronized FileObject resolveFile(URL url) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("[fs] resolveFile ... by url '" + url + "'");
        }
        String key = url.toString();
        FileObject object = cached.get(key);
        if (object == null) {
            if (ResourceUtils.isJarURL(url)) {
                if (!url.getPath().endsWith("/")) {
                    object = resolveFile(new URL(url + "/"));
                }
                if (object == null || !object.exists()) {
                    object = new JarFileObject(this, url);
                    if (logger.isTraceEnabled()) {
                        logger.trace("[fs] create jarFileObject for '" + url + "'");
                    }
                }
            } else {
                File file = ResourceUtils.getFile(url);
                if (file.isDirectory()) {
                    if (!url.toString().endsWith("/")) {
                        url = new URL(url + "/");
                    }
                } else if (file.isFile()) {
                    if (url.toString().endsWith("/")) {
                        url = new URL(StringUtils.removeEnd(url.toString(), "/"));
                    }
                }
                object = new SimpleFileObject(this, url);
                if (logger.isTraceEnabled()) {
                    logger.trace("[fs] create simpleFileObject for '" + url + "'");
                }
            }
            cached.put(key, object);
            if (!key.equals(object.getURL().toString())) {
                cached.put(object.getURL().toString(), object);
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("[fs] found cached file for '" + url + "'");
            }
        }
        return object;
    }

    public void clearCache() {
        cached.clear();
    }

}
