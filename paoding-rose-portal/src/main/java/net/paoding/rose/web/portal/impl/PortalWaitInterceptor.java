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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.PortalUtils;
import net.paoding.rose.web.portal.Window;
import net.paoding.rose.web.portal.WindowTask;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PortalWaitInterceptor extends ControllerInterceptorAdapter {

    @Override
    public Object after(Invocation inv, Object instruction) throws Exception {
        Portal portal = PortalUtils.getPortal(inv);
        if (portal != null) {
            long begin = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug(portal + " is going to wait windows.");
            }
            //
            waitForWindows((PortalImpl) portal);
            //
            if (logger.isDebugEnabled()) {
                logger.debug(portal + ".waitForWindows is done; cost="
                        + (System.currentTimeMillis() - begin));
            }
        }
        return instruction;
    }

    protected void waitForWindows(PortalImpl portal) {
        long deadline;
        long begin = System.currentTimeMillis();
        if (portal.getTimeout() > 0) {
            deadline = begin + portal.getTimeout();
            if (logger.isDebugEnabled()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                logger.debug(portal + ".maxWait=" + portal.getTimeout() + "; deadline="
                        + sdf.format(new Date(deadline)));
            }
        } else {
            deadline = 0;
            if (logger.isDebugEnabled()) {
                logger.debug(portal + ".maxWait=(forever)");
            }
        }
        int winSize = portal.getWindows().size();
        int winIndex = 0;
        for (Window window : portal.getWindows()) {
            winIndex++;
            WindowTask task = window.getTask();
            if (task.isDone() || task.isCancelled() || !window.isForRender()) {
                if (logger.isDebugEnabled()) {
                    if (task.isDone()) {
                        logger.debug("[" + winIndex + "/" + winSize + "] continue[done]: "
                                + window.getName());
                    } else if (task.isCancelled()) {
                        logger.debug("[" + winIndex + "/" + winSize + "] continue[cancelled]: "
                                + window.getName());
                    } else if (!window.isForRender()) {
                        logger.debug("[" + winIndex + "/" + winSize + "] continue[notForRender]: "
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
                        if (logger.isDebugEnabled()) {
                            logger.debug("[" + winIndex + "/" + winSize + "] waiting[begin] : "
                                    + window.getName() + "; maxWait=" + awaitTime);
                        }
                        task.await(awaitTime);
                        if (logger.isDebugEnabled()) {
                            logger.debug("[" + winIndex + "/" + winSize + "] waiting[done] : "
                                    + window.getName() + "; actualWait="
                                    + (System.currentTimeMillis() - begineWait));
                        }
                    } else {
                        logger.error("[" + winIndex + "/" + winSize
                                + "] waiting[been timeout now] : " + window.getName());
                        portal.onWindowTimeout(window);
                        task.cancel(true);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[" + winIndex + "/" + winSize + "] waiting[begin] : "
                                + window.getName() + "; maxWait=(forever)");
                    }
                    task.await();
                    if (logger.isDebugEnabled()) {
                        logger.debug("[" + winIndex + "/" + winSize + "] waiting[done] : "
                                + window.getName() + "; actualWait="
                                + (System.currentTimeMillis() - begineWait));
                    }
                }
            } catch (InterruptedException e) {
                logger.error("x[" + winIndex + "/" + winSize + "] waiting[interrupted] : "
                        + window.getName());
            } catch (ExecutionException e) {
                logger.error("x[" + winIndex + "/" + winSize + "] waiting[error] : "
                        + window.getName(), e);
                window.setThrowable(e);
                portal.onWindowError(window);
            } catch (TimeoutException e) {
                logger.error("x[" + winIndex + "/" + winSize + "] waiting[timeout] : "
                        + window.getName(), e);
                portal.onWindowTimeout(window);
                task.cancel(true);
            }
        }
        portal.onPortalReady(portal);
    }
}
