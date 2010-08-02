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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.RoseConstants;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.ServerPortal;
import net.paoding.rose.web.portal.PortalListener;
import net.paoding.rose.web.portal.PortalListeners;
import net.paoding.rose.web.portal.Window;
import net.paoding.rose.web.portal.WindowCallback;
import net.paoding.rose.web.portal.WindowRender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link ServerPortal} 的实现类，Portal 框架的核心类。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public abstract class AbstractPortal implements Portal, PortalListener {

    private static final Log logger = LogFactory.getLog(AbstractPortal.class);

    protected ExecutorService executorService;

    protected PortalListeners portalListeners;

    protected Invocation invocation;

    protected List<Window> windows = new LinkedList<Window>();

    protected WindowRender render;

    protected long timeout;

    public AbstractPortal(Invocation inv, ExecutorService executorService,
            PortalListener portalListener) {
        this.invocation = inv;
        this.executorService = executorService;
        addListener(portalListener);
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
    public HttpServletRequest getRequest() {
        return invocation.getRequest();
    }

    @Override
    public HttpServletResponse getResponse() {
        return invocation.getResponse();
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
    public Window addWindow(String windowPath) {
        String windowName = windowPath;
        return this.addWindow(windowName, windowPath, (WindowCallback) null);
    }

    @Override
    public Window addWindow(String name, String windowPath) {
        return this.addWindow(name, windowPath, (WindowCallback) null);
    }

    @Override
    public Window addWindow(String name, String windowPath, final Map<String, Object> attributes) {
        WindowCallback callback = null;
        if (attributes != null && attributes.size() > 0) {
            callback = new WindowCallback() {

                @Override
                public void beforeSubmit(Window window) {
                    synchronized (attributes) {
                        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                            window.set(entry.getKey(), entry.getValue());
                        }
                    }
                }
            };
        }
        return this.addWindow(name, windowPath, callback);
    }

    @Override
    public Window addWindow(String name, String windowPath, WindowCallback callback) {
        // 创建 窗口对象
        WindowImpl window = new WindowImpl(this, name, windowPath);

        // PortalWaitInterceptor#waitForWindows
        // RoseFilter#supportsRosepipe
        window.getRequest().removeAttribute(RoseConstants.PIPE_WINDOW_IN);

        // 定义窗口任务
        WindowTask task = new WindowTask(window);

        // 注册到相关变量中
        synchronized (windows) {
            this.windows.add(window);
        }
        this.invocation.addModel(name, window);

        if (callback != null) {
            callback.beforeSubmit(window);
        }

        // 事件侦听回调
        onWindowAdded(window);

        // 提交到执行服务中执行
        Future<?> future = submitWindow(this.executorService, task);
        window.setFuture(future);

        // 返回窗口对象
        return window;
    }

    @Override
    public List<Window> getWindows() {
        return windows;
    }

    @Override
    public WindowRender getWindowRender() {
        return render;
    }

    @Override
    public void setWindowRender(WindowRender render) {
        this.render = render;
    }

    @SuppressWarnings("unchecked")
    protected Future<?> submitWindow(ExecutorService executor, WindowTask task) {
        Future<?> future = executor.submit(task);
        return new WindowFuture(future, task.getWindow());
    }

    //-------------实现toString()---------------F

    @Override
    public String toString() {
        return "aggregate ['" + invocation.getRequestPath().getUri() + "']";
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

}
