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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.paoding.rose.web.portal.Window;
import net.paoding.rose.web.portal.WindowTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class WindowTaskImpl implements WindowTask, Runnable {

    private static final Log logger = LogFactory.getLog(WindowTaskImpl.class);

    private WindowImpl window;

    private Future<?> future;

    public WindowTaskImpl(WindowImpl window) {
        if (window == null) {
            throw new NullPointerException("window");
        }
        this.window = window;
        this.window.setTask(this);
    }

    public Future<?> submitTo(ExecutorService executor) {
        if (this.future == null) {
            future = executor.submit(this);
        }
        return future;
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

    public Window doRequest() throws Exception {
        window.setStartTime(System.currentTimeMillis());
        final WindowRequest request = new WindowRequest(window);
        final WindowResponse response = new WindowResponse(window);
        request.setAttribute("$$paoding-rose-portal.window", window);
        request.getRequestDispatcher(window.getPath()).forward(request, response);
        window.setDoneTime(System.currentTimeMillis());
        return window;
    }

    public PortalImpl getPortal() {
        return (PortalImpl) window.getPortal();
    }

    public Window getWindow() {
        return window;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (future.cancel(mayInterruptIfRunning)) {
            getPortal().onWindowCanceled(window);
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public void await() throws InterruptedException, ExecutionException {
        future.get();
    }

    @Override
    public void await(long await) throws InterruptedException, ExecutionException, TimeoutException {
        future.get(await, TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return "window [name=" + window.getName() + ", path=" + window.getPath() + "]";
    }

}
