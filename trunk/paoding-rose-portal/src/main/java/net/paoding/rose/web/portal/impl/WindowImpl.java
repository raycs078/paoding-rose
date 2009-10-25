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
package net.paoding.rose.web.portal.impl;

import java.util.HashMap;
import java.util.Map;

import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.Window;
import net.paoding.rose.web.portal.WindowTask;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class WindowImpl implements Window  {

    private long startTime = System.currentTimeMillis();

    private String name;

    private String path;

    private StringBuilder buffer;

    private long doneTime;

    private Throwable throwable;

    private int statusCode = 200;

    private String statusMessage = "";

    private Map<String, Object> attributes;

    private Portal portal;

    private boolean forRender = true;

    private WindowTask task;

    public WindowImpl(Portal portal, String name, String windowPath) {
        this.portal = portal;
        this.name = name;
        this.path = windowPath;
    }

    public Portal getPortal() {
        return portal;
    }

    public WindowTask getTask() {
        return task;
    }

    public void setTask(WindowTask task) {
        this.task = task;
    }

    public void set(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        attributes.put(key, value);
    }

    public Object get(String key) {
        return (attributes == null) ? null : attributes.get(key);
    }

    public void setTitle(Object title) {
        set("title", title);
    }

    public Object getTitle() {
        Object value = get("title");
        if (value == null) {
            value = name;
        }
        return value;
    }

    public String getContent() {
        return buffer == null ? "" : buffer.toString();
    }

    void appendContent(String content) {
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
        return doneTime > 0;
    }

    public void setDoneTime(long doneTime) {
        this.doneTime = doneTime;
    }

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDoneTime() {
        return doneTime;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public void setStatus(int sc) {
        this.statusCode = sc;
        this.statusMessage = "";
    }

    public void setStatus(int sc, String msg) {
        this.statusCode = sc;
        this.statusMessage = msg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public WindowImpl forRender(boolean forRender) {
        this.forRender = forRender;
        return this;
    }

    public boolean isForRender() {
        return forRender;
    }

    @Override
    public String toString() {
        return getContent();
    }

}
