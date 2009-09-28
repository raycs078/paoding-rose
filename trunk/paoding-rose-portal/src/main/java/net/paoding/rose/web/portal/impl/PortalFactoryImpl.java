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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.PortalFactory;
import net.paoding.rose.web.portal.PortalListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PortalFactoryImpl implements PortalFactory {

    private Log logger = LogFactory.getLog(getClass());

    private int corePoolSize = 100;

    private int maximumPoolSize = Integer.MAX_VALUE;

    private long keepAliveTime = 60 * 1000;

    private ExecutorService executor;

    private PortalListener portalListener;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public void setExecutor(ThreadPoolTaskExecutor executor) {
        logger.info("using " + executor);
        this.executor = executor.getThreadPoolExecutor();
    }

    public void setExecutorService(ExecutorService executor) {
        logger.info("using " + executor);
        this.executor = executor;
    }

    public void setPortalListener(PortalListener portalListener) {
        this.portalListener = portalListener;
    }

    public PortalListener getPortalListener() {
        return portalListener;
    }

    public ExecutorService getExecutorService() {
        if (executor == null) {
            synchronized (this) {
                if (executor == null) {
                    executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
                }
            }
        }
        return executor;
    }

    @Override
    public Portal createPortal(Invocation inc) {
        PortalImpl portal = new PortalImpl(inc, portalListener, getExecutorService());
        portal.onPortalCreated(portal);
        return portal;
    }

}
