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

import javax.servlet.RequestDispatcher;

import net.paoding.rose.web.portal.Window;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
final class WindowTaskImpl implements Runnable {

    private static final Log logger = LogFactory.getLog(WindowTaskImpl.class);

    private final WindowImpl window;

    public WindowTaskImpl(WindowImpl window) {
        if (window == null) {
            throw new NullPointerException("window");
        }
        this.window = window;
    }

    public Window getWindow() {
        return window;
    }

    public PortalImpl getPortal() {
        return (PortalImpl) window.getPortal();
    }

    @Override
    public void run() {
        try {
            getPortal().onWindowStarted(window);
            doRequest();
            getPortal().onWindowDone(window);
        } catch (Throwable e) {
            logger.error("", e);
            window.setThrowable(e);
            getPortal().onWindowError(window);
        }
    }

    protected void doRequest() throws Exception {
        final WindowRequest request = new WindowRequest(window.getPortal().getRequest());
        final WindowResponse response = new WindowResponse(window);
        final RequestDispatcher rd = request.getRequestDispatcher(window.getPath());
        request.setAttribute("$$paoding-rose-portal.window", window);
        // !!forward!!
        rd.forward(request, response);
    }
    
    @Override
    public String toString() {
        return "window [name=" + window.getName() + ", path=" + window.getPath() + "]";
    }

}
