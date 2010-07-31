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

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class SessionAfterCommitted implements HttpSession {

    IllegalStateException exception;

    public SessionAfterCommitted(IllegalStateException exception) {
        this.exception = exception;
    }

    @Override
    public Object getAttribute(String arg0) {
        throw new IllegalStateException(exception);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration getAttributeNames() {
        throw new IllegalStateException(exception);
    }

    @Override
    public long getCreationTime() {
        throw new IllegalStateException(exception);
    }

    @Override
    public String getId() {
        throw new IllegalStateException(exception);
    }

    @Override
    public long getLastAccessedTime() {
        throw new IllegalStateException(exception);
    }

    @Override
    public int getMaxInactiveInterval() {
        throw new IllegalStateException(exception);
    }

    @Override
    public ServletContext getServletContext() {
        throw new IllegalStateException(exception);
    }

    @SuppressWarnings("deprecation")
    @Override
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        throw new IllegalStateException(exception);
    }

    @Override
    public Object getValue(String arg0) {
        throw new IllegalStateException(exception);
    }

    @Override
    public String[] getValueNames() {
        throw new IllegalStateException(exception);
    }

    @Override
    public void invalidate() {
        throw new IllegalStateException(exception);
    }

    @Override
    public boolean isNew() {
        throw new IllegalStateException(exception);
    }

    @Override
    public void putValue(String arg0, Object arg1) {
        throw new IllegalStateException(exception);
    }

    @Override
    public void removeAttribute(String arg0) {
        throw new IllegalStateException(exception);
    }

    @Override
    public void removeValue(String arg0) {
        throw new IllegalStateException(exception);
    }

    @Override
    public void setAttribute(String arg0, Object arg1) {
        throw new IllegalStateException(exception);
    }

    @Override
    public void setMaxInactiveInterval(int arg0) {
        throw new IllegalStateException(exception);
    }

}
