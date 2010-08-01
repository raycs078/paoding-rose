/*
 * Copyright 2007-2010 the original author or authors.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.portal.Pipe;
import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.PortalListener;
import net.paoding.rose.web.portal.PortalListenerAdapter;
import net.paoding.rose.web.portal.Window;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */

public class PipeImpl extends AggregateImpl implements Pipe {

    private static final Log logger = LogFactory.getLog(PipeImpl.class);

    private int state = 0;

    private CountDownLatch latch;

    private final HttpServletResponse fireResponse;

    // 暂时阻塞的窗口
    private List<Window> blocking;

    private Portal portal;

    public PipeImpl(Portal portal, ExecutorService executorService, PortalListener portalListener) {
        this(portal, executorService, portalListener, portal.getResponse());
    }

    public PipeImpl(Portal portal, ExecutorService executorService, PortalListener portalListener,
            HttpServletResponse fireResponse) {
        super(portal.getInvocation(), executorService, portalListener);
        this.fireResponse = fireResponse;
        this.portal = portal;
        addListener(new FireListener());
    }

    private class FireListener extends PortalListenerAdapter {

        @Override
        public void onWindowDone(Window window) {
            try {
                fire(window);
            } catch (IOException e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public Portal getPortal() {
        return portal;
    }

    public HttpServletResponse getFireResponse() {
        return fireResponse;
    }

    public void await(long timeout) throws InterruptedException {
        if (timeout > 0) {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } else {
            latch.await();
        }
    }

    public synchronized void start() throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("start pipe " + getInvocation().getRequestPath().getUri());
        }
        state = 1;
        fireResponse.flushBuffer();
        latch = new CountDownLatch(windows.size());
        if (blocking != null) {
            for (Window window : blocking) {
                doFire(window);
            }
            blocking = null;
        }
    }

    public synchronized void fire(Window window) throws IOException {
        if (!super.windows.contains(window)) {
            throw new IllegalArgumentException(//
                    "not a register piped window '" + window.getName() + "'");
        }
        if (state < 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("firing '" + window.getName() + "' but pipe is closed");
            }
            return;
        } else if (state == 0) {
            if (blocking == null) {
                blocking = new ArrayList<Window>(windows.size());
            } else {
                if (blocking.contains(window)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("firing '" + window.getName()
                                + "' : has been add to waiting list");
                    }
                    return;
                }
            }
            blocking.add(window);
            if (logger.isDebugEnabled()) {
                logger.debug("firing '" + window.getName() + "' : add to waiting list");
            }
        } else {
            doFire(window);
        }
    }

    protected synchronized void doFire(Window window) throws IOException {
        if (state != 1) {
            throw new IllegalStateException("only avalabled when started.");
        }
        try {
            // 这里不用设置response的encoding，即使用和主控一致的encoding
            PrintWriter out = fireResponse.getWriter();
            out.println(window.getOutputContent());
            out.flush();
        } finally {
            latch.countDown();
        }
        if (logger.isDebugEnabled()) {
            logger.debug(//
                    "firing '" + window.getName() + "' : done  content=" + window.getContent());
        }
    }

    public synchronized void close() {
        if (logger.isDebugEnabled()) {
            logger.debug("close pipe " + getInvocation().getRequestPath().getUri());
        }
        this.state = -1;
    }

    //-------------实现toString()---------------F

    @Override
    public String toString() {
        return "pipe ['" + getInvocation().getRequestPath().getUri() + "']";
    }

}
