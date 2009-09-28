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
            waitForWindows((PortalImpl) portal);
        }
        return instruction;
    }

    protected void waitForWindows(PortalImpl portal) {
        long deadline;
        long begin = System.currentTimeMillis();
        if (portal.getTimeout() > 0) {
            deadline = begin + portal.getTimeout();
        } else {
            deadline = 0;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("portal[" + portal.getRequestPath().getUri() + "] timeout="
                    + portal.getTimeout() + "; deadline="
                    + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date(deadline)));
        }
        for (WindowTaskImpl task : portal.getTasks()) {
            if (task.isDone() || task.isCancelled() || !task.forRender()) {
                if (logger.isDebugEnabled()) {
                    if (task.isDone()) {
                        logger.debug("continue[done]: " + task.getWindow().getName());
                    } else if (task.isCancelled()) {
                        logger.debug("continue[cancelled]: " + task.getWindow().getName());
                    } else if (!task.forRender()) {
                        logger.debug("continue[notForRender]: " + task.getWindow().getName());
                    }
                }
                continue;
            }
            long awaitTime = 0;
            try {
                if (deadline > 0) {
                    awaitTime = deadline - System.currentTimeMillis();
                    if (awaitTime > 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("[" + task.getWindow().getName() + "] waiting; max="
                                    + awaitTime);
                        }
                        task.await(awaitTime);
                        if (logger.isDebugEnabled()) {
                            logger.debug("[" + task.getWindow().getName() + "] done; wait="
                                    + (System.currentTimeMillis() - deadline + awaitTime));
                        }
                    } else {
                        logger.error("x[" + task.getWindow().getName() + "] been timeout now ");
                        portal.onWindowTimeout(task, task.getWindow());
                        task.cancel(true);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[" + task.getWindow().getName() + "] waiting ");
                    }
                    task.await();
                }
            } catch (InterruptedException e) {
                logger.error("x[" + task.getWindow().getName() + "] been interrupted ");
            } catch (ExecutionException e) {
                logger.error("x[" + task.getWindow().getName() + "] error happened ", e);
                task.getWindow().setThrowable(e);
                portal.onWindowError(task, task.getWindow());
            } catch (TimeoutException e) {
                logger.error("x[" + task.getWindow().getName() + "] waiting max=" + awaitTime
                        + " but timeout ");
                portal.onWindowTimeout(task, task.getWindow());
                task.cancel(true);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("portal [" + portal.getRequestPath().getUri() + "] is done; timeout="
                    + portal.getTimeout() + " wait=" + (System.currentTimeMillis() - begin));
        }
        portal.onPortalReady(portal);
    }
}
