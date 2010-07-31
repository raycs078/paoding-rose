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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.paoding.rose.web.portal.PortalListener;
import net.paoding.rose.web.portal.Window;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class WindowFuture<T> implements Future<T> {

    private static Log logger = LogFactory.getLog(WindowFuture.class);

    private final Future<T> future;

    private final WindowImpl window;

    public WindowFuture(Future<T> future, WindowImpl window) {
        this.future = future;
        this.window = window;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (mayInterruptIfRunning) {
            Object value = this.window.get(Window.FUTURE_CANCEL_ENABLE_ATTR);
            if (value != null && (Boolean.FALSE.equals(value) || "false".equals(value))) {
                mayInterruptIfRunning = false;
                if (logger.isDebugEnabled()) {
                    logger.debug("can't interrupt window when running , window['"
                            + window.getPath() + "'].cancel.enable=" + value);
                }
            }
        }
        if (future.cancel(mayInterruptIfRunning)) {
            ((PortalListener) window.getAggregate()).onWindowCanceled(window);
            return true;
        }
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
        return future.get(timeout, unit);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

}
