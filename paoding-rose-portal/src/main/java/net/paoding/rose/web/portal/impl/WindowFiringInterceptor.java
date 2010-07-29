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
public class WindowFiringInterceptor extends ControllerInterceptorAdapter {

    private PipeFactory pipeFactory;

    public void setPipeFactory(PipeFactory pipeFactory) {
        this.pipeFactory = pipeFactory;
    }

    @Override
    public boolean isForDispatcher(Dispatcher dispatcher) {
        return dispatcher == Dispatcher.FORWARD;
    }

    @Override
    public void afterCompletion(Invocation inv, Throwable ex) throws Exception {
        Window window = PortalUtils.getWindow(inv);
        if (window != null && window.getName().startsWith("pipe:")) {
            Invocation pipeInv = window.getPortal().getInvocation();
            pipeFactory.getPipe(pipeInv, true).fire(window);
        }
    }
}
