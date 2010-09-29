/*
 * Copyright 2007-2012 the original author or authors.
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

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationChain;
import net.paoding.rose.web.portal.PortalUtils;
import net.paoding.rose.web.portal.Window;

/**
 * 
 * @author qieqie.wang@gmail.com
 * 
 */
public class WindowCancelableSupportInterceptor extends ControllerInterceptorAdapter {

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected Object round(Invocation inv, InvocationChain chain) throws Exception {
        WindowImpl win = (WindowImpl) PortalUtils.getWindow(inv);
        if (win == null) {
            return super.round(inv, chain);
        }
        //
        boolean cancelableSupport = true;
        Object value = win.get(Window.FUTURE_CANCEL_ENABLE_ATTR);
        if (value != null && (Boolean.FALSE.equals(value) || "false".equals(value))) {
            cancelableSupport = false;
            if (logger.isDebugEnabled()) {
                logger.debug("set window's cancelableSupport=false");
            }
        }
        if (cancelableSupport || !Thread.currentThread().isInterrupted()) {
            return super.round(inv, chain);
        } //
        else {
            WindowFuture<?> future = (WindowFuture<?>) win.getFuture();
            try {
                future.setCanclableSupport(false);
                Thread.interrupted();// clear the interruption
                return super.round(inv, chain);
            } finally {
                future.setCanclableSupport(true);
                Thread.currentThread().interrupt();// recover
                if (logger.isDebugEnabled()) {
                    logger.debug("recover currentThread's interrupted");
                }
            }
        }
    }

}
