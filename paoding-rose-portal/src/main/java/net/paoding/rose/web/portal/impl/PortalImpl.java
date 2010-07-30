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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.RoseConstants;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.portal.PipeRender;
import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.PortalListener;
import net.paoding.rose.web.portal.PortalListeners;
import net.paoding.rose.web.portal.Window;
import net.paoding.rose.web.var.Model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link Portal} 的实现类，Portal 框架的核心类。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class PortalImpl implements Portal, PortalListener {

    private static final Log logger = LogFactory.getLog(PortalImpl.class);

    private ExecutorService executorService;

    private PortalListeners portalListeners;

    private Invocation invocation;

    private List<Window> windows = Collections.emptyList();

    private long timeout;

    private long pipeTimeout;

    private PipeFactory pipeFactory;

    private PipeRender pipeRender;

    public PortalImpl(Invocation inv, PipeFactory pipeFactory, ExecutorService executorService,
            PortalListener portalListener) {
        this.invocation = inv;
        this.pipeFactory = pipeFactory;
        this.executorService = executorService;
        addListener(portalListener);
    }

    public void setTimeout(long timeoutInMills) {
        this.timeout = timeoutInMills;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setPipeTimeout(long pipeTimeout) {
        this.pipeTimeout = pipeTimeout;
    }

    public long getPipeTimeout() {
        return pipeTimeout;
    }

    @Override
    public Invocation getInvocation() {
        return invocation;
    }

    @Override
    public void addListener(PortalListener l) {
        if (l == null) {
            return;
        } else {
            synchronized (this) {
                if (portalListeners == null) {
                    portalListeners = new PortalListeners();
                }
                portalListeners.addListener(l);
            }
        }
    }

    @Override
    public synchronized List<Window> getWindows() {
        return Collections.unmodifiableList(windows);
    }

    @Override
    public List<Window> getSimpleWindows() {
        return getWindows(Window.TYPE_SIMPLE);
    }

    @Override
    public List<Window> getPipeWindows() {
        return getWindows(Window.TYPE_PIPE);
    }

    protected List<Window> getWindows(String type) {
        if (type == null || type.length() == 0 || type.equals("all")) {
            return getWindows();
        }
        List<Window> clone = new LinkedList<Window>(windows);
        String typePrefix = type + ":";
        for (Iterator<Window> iter = clone.iterator(); iter.hasNext();) {
            Window w = iter.next();
            if (type.equals(Window.TYPE_SIMPLE)) {
                if (w.getName().indexOf(':') >= 0) {
                    iter.remove();
                }
            } else {
                if (!w.getName().startsWith(typePrefix)) {
                    iter.remove();
                }
            }
        }
        return clone;
    }

    @Override
    public Window addWindow(String windowPath) {
        String windowName = windowPath;
        return this.addWindow(windowName, windowPath);
    }

    @Override
    public Window addWindow(String name, String windowPath) {
        return this.addWindow(name, windowPath, (Map<String, Object>) null);
    }

    @Override
    public Window addWindow(String name, String windowPath, Map<String, Object> attributes) {
        boolean usePipe = name.startsWith("pipe:");

        // 创建 窗口对象
        WindowImpl window = new WindowImpl((PortalImpl) this, name, windowPath);

        // PortalWaitInterceptor#waitForWindows
        // RoseFilter#supportsRosepipe
        window.getRequest().removeAttribute(RoseConstants.PIPE_WINDOW_IN);

        //
        if (attributes != null) {
            synchronized (attributes) {
                for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                    window.set(entry.getKey(), entry.getValue());
                }
            }
        }

        // 定义窗口任务
        WindowTask task = new WindowTask(window, pipeFactory);

        // 注册到相关变量中
        synchronized (this) {
            if (this.windows.size() == 0) {
                this.windows = new LinkedList<Window>();
            }
            this.windows.add(window);
        }
        if (usePipe) {
            // create pipe
            Pipe pipe = pipeFactory.getPipe(invocation, true);
            pipe.register(window);
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
    protected Future<?> submitWindow(ExecutorService executor, WindowTask task) {
        Future<?> future = executor.submit(task);
        return new WindowFuture(future, task.getWindow());
    }

    @Override
    public HttpServletRequest getRequest() {
        return invocation.getRequest();
    }

    @Override
    public HttpServletResponse getResponse() {
        return invocation.getResponse();
    }

    @Override
    public void addModel(String name, Object value) {
        invocation.addModel(name, value);
    }

    @Override
    public Model getModel() {
        return invocation.getModel();
    }

    //-------------实现toString()---------------F

    @Override
    public String toString() {
        return "portal ['" + invocation.getRequestPath().getUri() + "']";
    }

    //------------ 以下代码是PortalListener和Invocation的实现代码 --------------------------------

    @Override
    public void onPortalCreated(Portal portal) {
        if (portalListeners != null) {
            try {
                portalListeners.onPortalCreated(portal);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onPortalReady(Portal portal) {
        if (portalListeners != null) {
            try {
                portalListeners.onPortalReady(portal);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowAdded(Window window) {
        if (portalListeners != null) {
            try {
                portalListeners.onWindowAdded(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowStarted(Window window) {
        if (portalListeners != null) {
            try {
                portalListeners.onWindowStarted(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowCanceled(Window window) {
        if (portalListeners != null) {
            try {
                portalListeners.onWindowCanceled(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowDone(Window window) {
        if (portalListeners != null) {
            try {
                portalListeners.onWindowDone(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowError(Window window) {
        if (portalListeners != null) {
            try {
                portalListeners.onWindowError(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void onWindowTimeout(Window window) {
        if (portalListeners != null) {
            try {
                portalListeners.onWindowTimeout(window);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void setPipeRender(PipeRender render) {
        this.pipeRender = render;
    }

    @Override
    public PipeRender getPipeRender() {
        return pipeRender;
    }

}
