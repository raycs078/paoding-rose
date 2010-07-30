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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.paoding.rose.RoseConstants;
import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.PortalListener;
import net.paoding.rose.web.portal.Window;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 这个拦截器只拦截 Portal 控制器方法，用于等待所有该 portal 的窗口执行完成或进行超时取消。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PortalWaitInterceptor extends ControllerInterceptorAdapter {

    @Autowired
    private PipeFactory pipeFactory;

    // 只拦截含有 Portal 的控制器方法
    @Override
    protected boolean isForAction(Method actionMethod, Class<?> controllerClazz) {
        int portalCount = 0;
        for (Class<?> paramType : actionMethod.getParameterTypes()) {
            if (paramType == Portal.class) {
                portalCount++;
            }
        }
        if (portalCount > 1) {
            throw new IllegalArgumentException("only one portal parameter is allowed: "
                    + controllerClazz.getName() + "." + actionMethod.getName());
        }
        return portalCount > 0;
    }

    @Override
    public Object after(Invocation inv, Object instruction) throws Exception {
        PortalImpl portal = null;
        for (Object param : inv.getMethodParameters()) {
            if (param instanceof Portal) {
                portal = (PortalImpl) param;
                break;
            }
        }
        long begin = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug(portal + " is going to wait windows.");
        }
        //
        long start = System.currentTimeMillis();
        waitForWindows(portal, (PortalListener) portal);
        long cost = System.currentTimeMillis() - start;
        if (portal.getTimeout() > 0) {
            portal.setPipeTimeout(portal.getTimeout() - cost);
        }
        //
        if (logger.isDebugEnabled()) {
            logger.debug(portal + ".waitForWindows is done; cost="
                    + (System.currentTimeMillis() - begin));
        }
        return instruction;
    }

    protected void waitForWindows(PortalImpl portal, PortalListener listener)
            throws InterruptedException {
        boolean debugEnabled = logger.isDebugEnabled();
        long deadline;
        long begin = System.currentTimeMillis();
        if (portal.getTimeout() > 0) {
            deadline = begin + portal.getTimeout();
            if (debugEnabled) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                logger.debug(portal + ".maxWait=" + portal.getTimeout() + "; deadline="
                        + sdf.format(new Date(deadline)));
            }
        } else {
            deadline = 0;
            if (debugEnabled) {
                logger.debug(portal + ".maxWait=(forever)");
            }
        }
        int winSize = portal.getWindows().size();
        int winIndex = 0;
        List<Window> windows = portal.getWindows();
        for (Window window : windows) {
            if (window.getName().indexOf(':') >= 0) {
                continue;
            }
            winIndex++;
            Future<?> future = window.getFuture();
            if (future.isDone()) {
                if (debugEnabled) {
                    if (future.isCancelled()) {
                        logger.debug("[" + winIndex + "/" + winSize + "] continue[cancelled]: "
                                + window.getName());
                    }
                    if (future.isDone()) {
                        logger.debug("[" + winIndex + "/" + winSize + "] continue[done]: "
                                + window.getName());
                    }
                }
                continue;
            }
            long awaitTime = 0;
            try {
                long begineWait = System.currentTimeMillis();
                if (deadline > 0) {
                    awaitTime = deadline - begineWait;
                    if (awaitTime > 0) {
                        if (debugEnabled) {
                            logger.debug("[" + winIndex + "/" + winSize + "] waiting[begin]: "
                                    + window.getName() + "; maxWait=" + awaitTime);
                        }
                        future.get(awaitTime, TimeUnit.MILLISECONDS);
                        if (debugEnabled) {
                            logger.debug("[" + winIndex + "/" + winSize + "] waiting[done]: "
                                    + window.getName() + "; actualWait="
                                    + (System.currentTimeMillis() - begineWait));
                        }
                    } else {
                        logger.error("[" + winIndex + "/" + winSize
                                + "] waiting[been timeout now]: " + window.getName());
                        listener.onWindowTimeout(window);
                        future.cancel(true);
                    }
                } else {
                    if (debugEnabled) {
                        logger.debug("[" + winIndex + "/" + winSize + "] waiting[begin]: "
                                + window.getName() + "; maxWait=(forever)");
                    }
                    future.get();
                    if (debugEnabled) {
                        logger.debug("[" + winIndex + "/" + winSize + "] waiting[done]: "
                                + window.getName() + "; actualWait="
                                + (System.currentTimeMillis() - begineWait));
                    }
                }
            } catch (InterruptedException e) {
                logger.error("x[" + winIndex + "/" + winSize + "] waiting[interrupted]: "
                        + window.getName());
            } catch (ExecutionException e) {
                logger.error("x[" + winIndex + "/" + winSize + "] waiting[error]: "
                        + window.getName(), e);
                window.setThrowable(e);
                listener.onWindowError(window);
            } catch (TimeoutException e) {
                logger.error("x[" + winIndex + "/" + winSize + "] waiting[timeout]: "
                        + window.getName(), e);
                listener.onWindowTimeout(window);
                future.cancel(true);
            }
        }
        if (debugEnabled) {
            logger.debug("[" + winIndex + "/" + winSize + "] size of simple windows = " + winIndex);
        }
        listener.onPortalReady(portal);

        // codes for fix this exception: "Cannot forward after response has been committed"
        // @see RoseFilter#supportsRosepipe
        // @see PortalImpl#addWindow
        for (Window window : windows) {
            if (window.getRequest().getAttribute(RoseConstants.PIPE_WINDOW_IN) != Boolean.TRUE) {
                if (debugEnabled) {
                    logger.debug("waitting for window '" + window.getName() + "''s forwarding");
                }
                synchronized (window) {
                    while (window.getRequest().getAttribute(RoseConstants.PIPE_WINDOW_IN) != Boolean.TRUE) {
                        window.wait();
                    }
                }
            }
        }
    }

    @Override
    public void afterCompletion(Invocation inv, Throwable ex) throws Exception {
        //
        Pipe pipe = pipeFactory.getPipe(inv, false);
        if (pipe == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("there's no pipe windows.");
            }
            return;
        }
        if (ex != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("close the pipe and returen because of exception previous.");
            }
            pipe.close();
            return;
        }
        pipe.start();
        PortalImpl portal = null;
        for (Object param : inv.getMethodParameters()) {
            if (param instanceof Portal) {
                portal = (PortalImpl) param;
                break;
            }
        }
        if (portal.getPipeTimeout() >= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("waiting for pipe windows up to " + portal.getPipeTimeout() + "ms");
            }
            long start = System.currentTimeMillis();
            pipe.await(portal.getPipeTimeout());
            long cost = System.currentTimeMillis() - start;

            if (logger.isDebugEnabled()) {
                logger.debug("it takes " + cost + "ms for pipe windows.");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("there's no time to wait pipe windows.");
            }
        }
        pipe.close();
    }

}
