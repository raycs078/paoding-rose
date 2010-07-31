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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.portal.Window;
import net.paoding.rose.web.portal.WindowRender;

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

    private AggregateImpl aggregate;

    private WindowRequest request;

    private WindowResponse response;

    private Future<?> future;

    public WindowImpl(AggregateImpl aggregate, String name, String windowPath) {
        this.aggregate = aggregate;
        this.name = name;
        this.path = windowPath;
        this.request = new WindowRequest(this, aggregate.getRequest());
        this.response = new WindowResponse(this);
        this.request.setAttribute("$$paoding-rose-portal.window.name", name);
        this.request.setAttribute("$$paoding-rose-portal.window.path", path);
    }

    public AggregateImpl getAggregate() {
        return aggregate;
    }

    public WindowRequest getRequest() {
        return request;
    }

    public WindowResponse getResponse() {
        return response;
    }

    @Override
    public Future<?> getFuture() {
        return future;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    @Override
    public void set(String key, Object value) {
        request.setAttribute(key, value);
    }

    @Override
    public Object get(String key) {
        return request.getAttribute(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return request.getPrivateAttributes();
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
    public String getContent() {
        return buffer == null ? "" : buffer.toString();
    }

    public String getOutputContent() {
        WindowRender render = aggregate.getWindowRender();
        if (render == null) {
            return getContent();
        }
        StringWriter stringWriter = new StringWriter(getContent().length() << 1);
        try {
            render.render(this, new PrintWriter(stringWriter));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return stringWriter.getBuffer().toString();
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
        return getOutputContent();
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
