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

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Dispatcher;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.portal.PortalUtils;
import net.paoding.rose.web.portal.Window;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
//TODO: window 404的怎么处理?
public class WindowInInterceptor extends ControllerInterceptorAdapter {

    public WindowInInterceptor() {
        setPriority(Integer.MAX_VALUE - 1);
    }

    @Override
    public boolean isForDispatcher(Dispatcher dispatcher) {
        return dispatcher == Dispatcher.FORWARD;
    }

    @Override
    protected Object before(Invocation inv) throws Exception {
        Window window = PortalUtils.getWindow(inv);
        if (window != null) {
            synchronized (window) {
                window.set("$$window.in", Boolean.TRUE);
                if (window.get("$$window.in.wait") == Boolean.TRUE) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("notify window '" + window.getName() + "'");
                    }
                    window.notifyAll();
                }
            }
        }
        return Boolean.TRUE;
    }

}
