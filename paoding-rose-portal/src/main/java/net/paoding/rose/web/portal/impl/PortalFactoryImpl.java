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

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.PortalFactory;
import net.paoding.rose.web.portal.PortalListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

/**
 * {@link PortalFactory} 的实现。
 * <p>
 * 
 * 创建 {@link PortalFactoryImpl}实例后，应该通过
 * {@link #setExecutorService(ExecutorService)} 或
 * {@link #setExecutorServiceBySpring(ThreadPoolTaskExecutor)}
 * 设置执行器，用于执行Portal下的每个“窗口请求”。
 * <p>
 * 
 * 可选设置 {@link PortalListener} 来获知portal的创建以及窗口的创建、执行等状态信息。
 * 
 * @see PortalImpl
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PortalFactoryImpl implements PortalFactory, InitializingBean {

    protected Log logger = LogFactory.getLog(getClass());

    private ExecutorService executorService;

    private PortalListener portalListener;

    private PipeFactory pipeFactory;

    public void setExecutorService(ExecutorService executor) {
        if (logger.isInfoEnabled()) {
            logger.info("using executorService: " + executor);
        }
        this.executorService = executor;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setPortalListener(PortalListener portalListener) {
        this.portalListener = portalListener;
    }

    public PortalListener getPortalListener() {
        return portalListener;
    }

    public void setPipeFactory(PipeFactory pipeFactory) {
        this.pipeFactory = pipeFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(portalListener);
        Assert.notNull(executorService);
        Assert.notNull(pipeFactory);
    }

    @Override
    public Portal createPortal(Invocation inv) {
        PortalImpl portal = new PortalImpl(inv, pipeFactory, executorService, portalListener);
        portal.onPortalCreated(portal);
        return portal;
    }

}
