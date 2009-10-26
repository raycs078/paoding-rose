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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.PortalListener;
import net.paoding.rose.web.portal.Window;
import net.paoding.rose.web.var.Flash;
import net.paoding.rose.web.var.Model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

/**
 * {@link Portal} 的实现类，Portal 框架的核心类。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class PortalImpl implements Portal, Invocation, PortalListener {

    private static final Log logger = LogFactory.getLog(PortalImpl.class);

    private ExecutorService executorService;

    private PortalListener portalListener;

    private Invocation invocation;

    private List<Window> windows = new LinkedList<Window>();

    private long timeout;

    public PortalImpl(Invocation inv, ExecutorService executorService, PortalListener portalListener) {
        this.invocation = inv;
        this.portalListener = portalListener;
        this.executorService = executorService;
    }

    public void setTimeout(long timeoutInMills) {
        this.timeout = timeoutInMills;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public Invocation getInvocation() {
        return invocation;
    }

    @Override
    public List<Window> getWindows() {
        synchronized (windows) {
            return new ArrayList<Window>(windows);
        }
    }

    @Override
    public Window addWindow(String windowPath) {
        String windowName = windowPath;
        return this.addWindow(windowName, windowPath);
    }

    @Override
    public Window addWindow(String name, String windowPath) {
        // 创建 窗口对象
        WindowImpl window = new WindowImpl((Portal) this, name, windowPath);

        // 定义窗口任务
        WindowTaskImpl task = new WindowTaskImpl(window);

        // 注册到相关变量中
        synchronized (windows) {
            this.windows.add(window);
        }
        this.invocation.addModel(name, window);

        // 事件侦听回调
        onWindowAdded(window);

        // 提交到执行服务中执行
        Future<?> future = submitWindow(this.executorService, task);
        window.setFuture(future);

        // 返回窗口对象
        return window;
    }

    @SuppressWarnings("unchecked")
    protected Future<?> submitWindow(ExecutorService executor, WindowTaskImpl task) {
        Future<?> future = executor.submit(task);
        return new WindowFuture(future, task.getWindow());
    }
    
    //-------------实现toString()---------------F

    @Override
    public String toString() {
        return "portal ['" + invocation.getRequestPath().getUri() + "']";
    }

    //------------ 以下代码是PortalListener和Invocation的实现代码 --------------------------------

    @Override
    public void onPortalCreated(Portal portal) {
        if (portalListener != null) {
            try {
                portalListener.onPortalCreated(portal);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onPortalReady(Portal portal) {
        if (portalListener != null) {
            try {
                portalListener.onPortalReady(portal);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowAdded(Window window) {
        if (portalListener != null) {
            try {
                portalListener.onWindowAdded(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowStarted(Window window) {
        if (portalListener != null) {
            try {
                portalListener.onWindowStarted(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowCanceled(Window window) {
        if (portalListener != null) {
            try {
                portalListener.onWindowCanceled(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowDone(Window window) {
        if (portalListener != null) {
            try {
                portalListener.onWindowDone(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowError(Window window) {
        if (portalListener != null) {
            try {
                portalListener.onWindowError(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowTimeout(Window window) {
        if (portalListener != null) {
            try {
                portalListener.onWindowTimeout(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void addModel(Object value) {
        invocation.addModel(value);
    }

    @Override
    public void addModel(String name, Object value) {
        invocation.addModel(name, value);
    }

    @Override
    public void changeMethodParameter(int index, Object value) {
        invocation.changeMethodParameter(index, value);
    }

    @Override
    public void changeMethodParameter(String name, Object value) {
        invocation.changeMethodParameter(name, value);
    }

    @Override
    public WebApplicationContext getApplicationContext() {
        return invocation.getApplicationContext();
    }

    @Override
    public Object getAttribute(String name) {
        return invocation.getAttribute(name);
    }

    @Override
    public Set<String> getAttributeNames() {
        return invocation.getAttributeNames();
    }

    @Override
    public BindingResult getBindingResult(Object bean) {
        return invocation.getBindingResult(bean);
    }

    @Override
    public List<String> getBindingResultNames() {
        return invocation.getBindingResultNames();
    }

    @Override
    public List<BindingResult> getBindingResults() {
        return invocation.getBindingResults();
    }

    @Override
    public Object getController() {
        return invocation.getController();
    }

    @Override
    public Class<?> getControllerClass() {
        return invocation.getControllerClass();
    }

    @Override
    public Flash getFlash() {
        return invocation.getFlash();
    }

    @Override
    public Method getMethod() {
        return invocation.getMethod();
    }

    @Override
    public Object getMethodParameter(String name) {
        return invocation.getMethodParameter(name);
    }

    @Override
    public String[] getMethodParameterNames() {
        return invocation.getMethodParameterNames();
    }

    @Override
    public Object[] getMethodParameters() {
        return invocation.getMethodParameters();
    }

    @Override
    public Model getModel() {
        return invocation.getModel();
    }

    @Override
    public Object getParameter(String name) {
        return invocation.getParameter(name);
    }

    @Override
    public BindingResult getParameterBindingResult() {
        return invocation.getParameterBindingResult();
    }

    @Override
    public String getRawParameter(String name) {
        return invocation.getRawParameter(name);
    }

    @Override
    public HttpServletRequest getRequest() {
        return invocation.getRequest();
    }

    @Override
    public RequestPath getRequestPath() {
        return invocation.getRequestPath();
    }

    @Override
    public HttpServletResponse getResponse() {
        return invocation.getResponse();
    }

    @Override
    public ServletContext getServletContext() {
        return invocation.getServletContext();
    }

    @Override
    public void removeAttribute(String name) {
        invocation.removeAttribute(name);
    }

    @Override
    public Invocation setAttribute(String name, Object value) {
        invocation.setAttribute(name, value);
        return this;
    }

    @Override
    public Object getOncePerRequestAttribute(String name) {
        return invocation.getOncePerRequestAttribute(name);
    }

    @Override
    public Invocation setOncePerRequestAttribute(String name, Object value) {
        return invocation.setOncePerRequestAttribute(name, value);
    }

    @Override
    public void setRequest(HttpServletRequest request) {
        invocation.setRequest(request);
    }

    class WindowFuture<T> implements Future<T> {

        private final Future<T> future;

        private final Window window;

        public WindowFuture(Future<T> future, Window window) {
            this.future = future;
            this.window = window;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (future.cancel(mayInterruptIfRunning)) {
                ((PortalListener) PortalImpl.this).onWindowCanceled(window);
                return true;
            }
            return false;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return future.get();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
                TimeoutException {
            return future.get(timeout, unit);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

    }
}
