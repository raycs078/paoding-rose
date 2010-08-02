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
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.portal.Pipe;
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

public class PipeImpl extends AbstractPortal implements Pipe {

    private static final Log logger = LogFactory.getLog(PipeImpl.class);

    private int state = 0;

    private CountDownLatch latch;

    // 暂时阻塞的窗口
    private List<Window> blocking;

    private Writer out;

    public PipeImpl(Invocation inv, ExecutorService executorService, PortalListener portalListener) {
        super(inv, executorService, portalListener);
        addListener(new FireListener());
    }

    private class FireListener extends PortalListenerAdapter {

        @Override
        public void onWindowDone(Window window) {
            try {
                PipeImpl.this.fire(window);
            } catch (IOException e) {
                logger.error("", e);
            }
        }
    }

    public synchronized boolean isStarted() {
        return out != null;
    }

    public void write(Writer out) throws IOException {
        if (isStarted()) {
            if (logger.isDebugEnabled()) {
                logger.debug(this + " has been started yet.");
            }
            return;
        }
        doStart(out);
        onPortalReady(this);;
        if (getTimeout() >= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("waiting for pipe windows up to " + getTimeout() + "ms");
            }
            long start = System.currentTimeMillis();
            try {
                await(getTimeout());
            } catch (InterruptedException e) {
                logger.error("pipe was interrupted", e);
            }
            long cost = System.currentTimeMillis() - start;

            if (logger.isDebugEnabled()) {
                logger.debug("it takes " + cost + "ms for pipe windows.");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("there's no time to wait pipe windows.");
            }
        }
    }

    private synchronized void doStart(Writer writer) throws IOException {
        if (this.out != null) {
            throw new IllegalStateException("has been started.");
        }
        this.out = writer;
        if (logger.isDebugEnabled()) {
            logger.debug("start pipe " + getInvocation().getRequestPath().getUri());
        }
        writer.flush();
        latch = new CountDownLatch(windows.size());
        state = 1;
        if (blocking != null) {
            for (Window window : blocking) {
                doFire(window);
            }
            blocking = null;
        }
    }

    private void await(long timeout) throws InterruptedException {
        if (timeout > 0) {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } else {
            latch.await();
        }
    }

    private synchronized void fire(Window window) throws IOException {
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

    private synchronized void doFire(Window window) throws IOException {
        if (state != 1) {
            throw new IllegalStateException("only avalabled when started.");
        }
        try {
            // 这里不用设置response的encoding，即使用和主控一致的encoding
            out.append(window.getOutputContent());
            out.flush();
        } finally {
            latch.countDown();
        }
        if (logger.isDebugEnabled()) {
            logger.debug(//
                    "firing '" + window.getName() + "' : done  content=" + window.getContent());
        }
    }

    //-------------实现toString()---------------F

    @Override
    public String toString() {
        return "pipe ['" + getInvocation().getRequestPath().getUri() + "']";
    }

}
