/*
 * Copyright 2007-2012 the original author or authors.
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
package net.paoding.rose.web.portal.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.Window;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class WindowImpl implements Window {

    private String name;

    private String path;

    private StringBuilder buffer;

    private Throwable throwable;

    private int statusCode = -1;

    private String statusMessage = "";

    private GenericWindowContainer container;

    private Future<?> future;

    public WindowImpl(GenericWindowContainer container, String name, String windowPath) {
        this.container = container;
        this.name = name;
        this.path = windowPath;
    }

    @Override
    @Deprecated
    public Portal getPortal() {
        return (Portal) container;
    }

    @Override
    public GenericWindowContainer getContainer() {
        return container;
    }

    @Override
    public Future<?> getFuture() {
        return future;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    /**
     * 窗口请求对象私有的、有别于其他窗口的属性
     */
    private Map<String, Object> privateAttributes;

    @Override
    public void set(String key, Object value) {
        if (privateAttributes == null) {
            privateAttributes = new HashMap<String, Object>();
        }
        privateAttributes.put(key, value);
    }

    @Override
    public Object get(String key) {
        return privateAttributes == null ? null : privateAttributes.get(key);
    }

    @Override
    public void remove(String key) {
        if (privateAttributes != null) {
            privateAttributes.remove(key);
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        if (privateAttributes == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(privateAttributes);
        }
    }

    @Override
    public void setTitle(Object title) {
        set(TITLE_ATTR, title);
    }

    @Override
    public Object getTitle() {
        Object value = get(TITLE_ATTR);
        if (value == null) {
            value = name;
        }
        return value;
    }

    @Override
    public int getContextLength() {
        return buffer == null ? -1 : buffer.length();
    }

    @Override
    public String getContent() {
        return buffer == null ? "" : buffer.toString();
    }

    @Override
    public void clearContent() {
        if (buffer != null) {
            buffer.setLength(0);
        }
    }

    void appendContent(String content) {
        if (buffer == null) {
            buffer = new StringBuilder();
        }
        this.buffer.append(content);
    }

    void appendContent(CharSequence content) {
        if (buffer == null) {
            buffer = new StringBuilder();
        }
        this.buffer.append(content);
    }

    void appendContent(char[] content) {
        if (buffer == null) {
            buffer = new StringBuilder();
        }
        this.buffer.append(content);
    }

    void appendContent(char[] content, int offset, int len) {
        if (buffer == null) {
            buffer = new StringBuilder();
        }
        this.buffer.append(content, offset, len);
    }

    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public boolean isSuccess() {
        return future.isDone() && getStatusCode() == HttpServletResponse.SC_OK;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
        this.statusMessage = throwable.getMessage();
        if (statusCode < 500 || statusCode >= 600) {
            statusCode = 500;
        }
    }

    public void setStatus(int sc) {
        this.statusCode = sc;
        this.statusMessage = "";
    }

    public void setStatus(int sc, String msg) {
        this.statusCode = sc;
        this.statusMessage = msg;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public String toString() {
        return this.container.render(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Window)) {
            return false;
        }
        return this.name.equals(((Window) obj).getName());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

}
