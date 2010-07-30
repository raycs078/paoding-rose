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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.portal.PipeRender;
import net.paoding.rose.web.portal.SimplePipeRender;
import net.paoding.rose.web.portal.Window;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */

public class PipeImpl implements Pipe {

    private static final Log logger = LogFactory.getLog(PipeImpl.class);

    private int state = 0;

    private Set<Window> waits = new HashSet<Window>();

    private List<Window> firing;

    private CountDownLatch latch;

    private Invocation inv;

    public PipeImpl(Invocation inv) {
        this.inv = inv;
    }

    @Override
    public synchronized void register(Window window) {
        if (state != 0) {
            throw new IllegalStateException("only avalabled when state=0; now state is " + state);
        }
        if (waits.contains(window)) {
            throw new IllegalArgumentException("duplicated windows '" + window.getName() + "'");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("register pipe window '" + window.getName() + "'");
        }
        waits.add(window);
    }

    @Override
    public void await(long timeout) throws InterruptedException {
        synchronized (this) {
            if (state < 1) {
                throw new IllegalStateException("only avalabled when started.");
            }
        }
        if (timeout > 0) {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } else {
            latch.await();
        }
    }

    @Override
    public synchronized void start() throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("start pipe " + inv.getRequestPath().getUri());
        }
        state = 1;
        inv.getResponse().flushBuffer();
        latch = new CountDownLatch(waits.size());
        if (firing != null) {
            for (Window window : firing) {
                doFire(window);
            }
            firing.clear();
            firing = null;
        }
    }

    @Override
    public synchronized void fire(Window window) throws IOException {
        if (!waits.contains(window)) {
            throw new IllegalArgumentException(//
                    "not a register piped window '" + window.getName() + "'");
        }
        if (state < 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("firing '" + window.getName() + "' but pipe is closed");
            }
            return;
        } else if (state == 0) {
            if (firing == null) {
                firing = new LinkedList<Window>();
            }
            firing.add(window);
            waits.remove(window);
            if (logger.isDebugEnabled()) {
                logger.debug("firing '" + window.getName() + "' : add to waiting list");
            }
        } else {
            try {
                doFire(window);
            } finally {
                latch.countDown();
            }
        }
    }

    protected synchronized void doFire(Window window) throws IOException {
        if (state != 1) {
            throw new IllegalStateException("only avalabled when started.");
        }
        PipeRender render = window.getPortal().getPipeRender();
        if (render == null) {
            logger.warn(//
                    "please set your pipeRender to your portal in controller to customer your PipeRender");
            render = SimplePipeRender.getInstance();
        }
        // 这里不用设置response的encoding，即使用和主控一致的encoding
        PrintWriter out = inv.getResponse().getWriter();
        render.render(window, out);
        out.flush();
        if (logger.isDebugEnabled()) {
            logger.debug("firing '" + window.getName() + "' : done");
        }
    }

    @Override
    public synchronized void close() {
        if (logger.isDebugEnabled()) {
            logger.debug("close pipe " + inv.getRequestPath().getUri());
        }
        this.state = -1;
        this.waits = null;
    }

}
