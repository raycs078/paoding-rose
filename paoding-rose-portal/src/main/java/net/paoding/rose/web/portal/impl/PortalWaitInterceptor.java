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
        if (portal.getTimeout() > 0) {
            deadline = System.currentTimeMillis() + portal.getTimeout();
        } else {
            deadline = 0;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + " timeout=" + portal.getTimeout() + "; deadline="
                    + new SimpleDateFormat("HH:dd:ss SSS").format(new Date(deadline)));
        }
        for (WindowTaskImpl task : portal.getTasks()) {
            if (!task.forRender() || task.isDone() || task.isCancelled()) {
                continue;
            }
            try {
                if (deadline > 0) {
                    long awaitTime = deadline - System.currentTimeMillis();
                    if (awaitTime > 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(this + ".window[" + task.getName() + "].awaitTime="
                                    + awaitTime);
                        }
                        task.await(awaitTime);
                    } else {
                        portal.onWindowTimeout(task, task.getWindow());
                        task.cancel(true);
                    }
                } else {
                    task.await();
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (ExecutionException e) {
                task.getWindow().setThrowable(e);
                portal.onWindowError(task, task.getWindow());
            } catch (TimeoutException e) {
                portal.onWindowTimeout(task, task.getWindow());
                task.cancel(true);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + " is about to render");
        }
        portal.onPortalReady(portal);
    }
}
