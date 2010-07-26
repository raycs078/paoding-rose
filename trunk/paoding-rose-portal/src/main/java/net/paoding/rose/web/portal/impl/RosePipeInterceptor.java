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

import org.springframework.beans.factory.annotation.Autowired;

public class RosePipeInterceptor extends ControllerInterceptorAdapter {

    @Autowired
    private PipeManager pipeManager;

    @Override
    public boolean isForDispatcher(Dispatcher dispatcher) {
        return dispatcher == Dispatcher.FORWARD;
    }

    @Override
    public void afterCompletion(Invocation inv, Throwable ex) throws Exception {
        Window window = PortalUtils.getWindow(inv);
        if (window != null && window.getName().startsWith("pipe:")) {
            // TODO
            // 到此表示某个pipe window已经准备好页面了
            // 识别pipe windows，并把它交给一个统一的处理器，让这个处理器负责输出该window出去
            pipeManager.getPipe(inv.getRequest()).submit(window);
        }
    }
}
