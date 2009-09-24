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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.AfterInterceptors;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.PortalListener;
import net.paoding.rose.web.portal.Window;
import net.paoding.rose.web.portal.WindowTask;
import net.paoding.rose.web.var.Flash;
import net.paoding.rose.web.var.Model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class PortalImpl implements Portal, PortalListener, AfterInterceptors {

    private static final Log logger = LogFactory.getLog(PortalImpl.class);

    private Invocation invocation;

    private Executor executor;

    private List<WindowTaskImpl> tasks = new LinkedList<WindowTaskImpl>();

    private PortalListener portalListener;

    private long createTime = System.currentTimeMillis();

    private long sleepTime;

    private long timeout;

    public PortalImpl(Invocation inv, PortalListener portalListener, Executor executor) {
        this.invocation = inv;
        this.portalListener = portalListener;
        this.executor = executor;
    }

    @Override
    public long getSleepTime() {
        return sleepTime;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    public void setTimeout(long timeoutInMills) {
        this.timeout = timeoutInMills;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public Invocation getInvocation() {
        return invocation;
    }

    @Override
    public WindowTask addWindow(String name, String windowPath) {
        //
        Window window = new Window(this, name, windowPath);
        WindowTaskImpl task = new WindowTaskImpl(window);
        tasks.add(task);
        //
        addModel(name, window);
        //
        onWindowAdded(task);
        //
        executor.execute(task);
        //
        return task;
    }

    @Override
    public Object doAfterInterceptors(Invocation inc, Object instruction) {
        long deadline;
        if (this.timeout > 0) {
            deadline = System.currentTimeMillis() + timeout;
        } else {
            deadline = 0;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + " timeout=" + timeout + "; deadline="
                    + new SimpleDateFormat("HH:dd:ss SSS").format(new Date(deadline)));
        }
        for (WindowTaskImpl task : tasks) {
            if (!task.forRender() || task.isDone() || task.isCancelled()) {
                continue;
            }
            try {
                if (deadline > 0) {
                    long awaitTime = deadline - System.currentTimeMillis();
                    if (logger.isDebugEnabled()) {
                        logger.debug(this + ".window[" + task.getName() + "].awaitTime="
                                + awaitTime);
                    }
                    task.await(awaitTime);
                } else {
                    task.await();
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (ExecutionException e) {
                task.getWindow().setThrowable(e);
                this.onWindowError(task, task.getWindow());
            } catch (TimeoutException e) {
                this.onWindowTimeout(task, task.getWindow());
                task.cancel(true);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + " is about to render");
        }
        onPortalReady(this);
        return instruction;
    }

    @Override
    public String toString() {
        return "portal [" + invocation.getRequestPath().getUri() + "]";
    }

    //--------------------------------------------

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
    public void onWindowAdded(WindowTask window) {
        if (portalListener != null) {
            try {
                portalListener.onWindowAdded(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowStarted(WindowTask window) {
        if (portalListener != null) {
            try {
                portalListener.onWindowStarted(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowCanceled(WindowTask window) {
        if (portalListener != null) {
            try {
                portalListener.onWindowCanceled(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowDone(WindowTask window, Window result) {
        if (portalListener != null) {
            try {
                portalListener.onWindowDone(window, result);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowError(WindowTask window, Window result) {
        if (portalListener != null) {
            try {
                portalListener.onWindowError(window, result);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowTimeout(WindowTask window, Window result) {
        if (portalListener != null) {
            try {
                portalListener.onWindowTimeout(window, result);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void addModel(Object value) {
        getInvocation().addModel(value);
    }

    @Override
    public void addModel(String name, Object value) {
        getInvocation().addModel(name, value);
    }

    @Override
    public void changeMethodParameter(int index, Object value) {
        getInvocation().changeMethodParameter(index, value);
    }

    @Override
    public void changeMethodParameter(String name, Object value) {
        getInvocation().changeMethodParameter(name, value);
    }

    @Override
    public WebApplicationContext getApplicationContext() {
        return getInvocation().getApplicationContext();
    }

    @Override
    public Object getAttribute(String name) {
        return getInvocation().getAttribute(name);
    }

    @Override
    public Set<String> getAttributeNames() {
        return getInvocation().getAttributeNames();
    }

    @Override
    public BindingResult getBindingResult(Object bean) {
        return getInvocation().getBindingResult(bean);
    }

    @Override
    public List<String> getBindingResultNames() {
        return getInvocation().getBindingResultNames();
    }

    @Override
    public List<BindingResult> getBindingResults() {
        return getInvocation().getBindingResults();
    }

    @Override
    public Object getController() {
        return getInvocation().getController();
    }

    @Override
    public Class<?> getControllerClass() {
        return getInvocation().getControllerClass();
    }

    @Override
    public Flash getFlash() {
        return getInvocation().getFlash();
    }

    @Override
    public Method getMethod() {
        return getInvocation().getMethod();
    }

    @Override
    public Object getMethodParameter(String name) {
        return getInvocation().getMethodParameter(name);
    }

    @Override
    public String[] getMethodParameterNames() {
        return getInvocation().getMethodParameterNames();
    }

    @Override
    public Object[] getMethodParameters() {
        return getInvocation().getMethodParameters();
    }

    @Override
    public Model getModel() {
        return getInvocation().getModel();
    }

    @Override
    public Object getParameter(String name) {
        return getInvocation().getParameter(name);
    }

    @Override
    public BindingResult getParameterBindingResult() {
        return getInvocation().getParameterBindingResult();
    }

    @Override
    public String getRawParameter(String name) {
        return getInvocation().getRawParameter(name);
    }

    @Override
    public HttpServletRequest getRequest() {
        return getInvocation().getRequest();
    }

    @Override
    public RequestPath getRequestPath() {
        return getInvocation().getRequestPath();
    }

    @Override
    public HttpServletResponse getResponse() {
        return getInvocation().getResponse();
    }

    @Override
    public ServletContext getServletContext() {
        return getInvocation().getServletContext();
    }

    @Override
    public boolean isDestroyed() {
        return getInvocation().isDestroyed();
    }

    @Override
    public void removeAttribute(String name) {
        getInvocation().removeAttribute(name);
    }

    @Override
    public Invocation setAttribute(String name, Object value) {
        getInvocation().setAttribute(name, value);
        return this;
    }

}
