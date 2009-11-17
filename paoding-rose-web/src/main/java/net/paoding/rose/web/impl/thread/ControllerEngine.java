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
package net.paoding.rose.web.impl.thread;

import java.lang.reflect.Proxy;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.impl.module.ControllerInfo;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.thread.tree.Rose;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ControllerEngine implements Engine {

    private static Log logger = LogFactory.getLog(ControllerEngine.class);

    private final Module module;

    private final String controllerPath;

    private final Object controller;

    private final boolean proxiedController;

    private final Class<?> controllerClass;

    private final String viewPrefix;

    public ControllerEngine(Module module, String controllerPath, ControllerInfo controller) {
        this.module = module;
        this.controllerPath = controllerPath;
        this.controller = controller.getControllerObject();
        this.controllerClass = controller.getControllerClass();
        this.viewPrefix = controller.getControllerName() + "-";
        this.proxiedController = Proxy.isProxyClass(this.controller.getClass());
        if (proxiedController && logger.isDebugEnabled()) {
            logger.debug("it's a proxied controller: " + controllerClass.getName());
        }
    }

    public Module getModule() {
        return module;
    }

    public String getViewPrefix() {
        return viewPrefix;
    }

    public String getControllerPath() {
        return controllerPath;
    }

    public Object getController() {
        return controller;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public boolean isProxiedController() {
        return proxiedController;
    }

    @Override
    public Object invoke(Rose rose, MatchResult<? extends Engine> mr, Object instruction,
            EngineChain chain) throws Throwable {
        Invocation inv = rose.getInvocation();
        inv.getRequestPath().setControllerPath(mr.getMatchedString());
        ((InvocationBean) inv).setController(controller);
        for (String matchResultParam : mr.getParameterNames()) {
            inv.addModel(matchResultParam, mr.getParameter(matchResultParam));
        }
        return chain.invokeNext(rose, instruction);
    }

    public void destroy() {
    }

    @Override
    public String toString() {
        return getControllerClass().getName();
    }
}
