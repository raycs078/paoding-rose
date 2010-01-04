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
package net.paoding.rose.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class OncePerRequestInterceptorWrapper extends ControllerInterceptorWrapper {

    private static Log logger = LogFactory.getLog(OncePerRequestInterceptorWrapper.class);

    private final String filtered;

    public OncePerRequestInterceptorWrapper(ControllerInterceptor interceptor) {
        super(interceptor);
        this.filtered = "$$paoding-rose.interceptor.oncePerRequest."
                + getInterceptor().getClass().getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public final Object before(Invocation inv) throws Exception {
        Invocation temp = inv;
        boolean tobeIntercepted = false;
        while (tobeIntercepted = (temp.getAttribute(filtered) == null)
                && (temp = temp.getPreInvocation()) != null) {
            // do nothing
        }
        if (tobeIntercepted) {
            inv.setAttribute(filtered, true);
            if (logger.isDebugEnabled()) {
                logger.debug("do oncePerRequest interceptor.before: " + getName());
            }
            return interceptor.before(inv);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("skip oncePerRequest interceptor.before: " + getName());
            }
            return true;
        }
    }

    @Override
    public final Object after(Invocation inv, Object instruction) throws Exception {
        if (inv.getAttribute(filtered) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("do oncePerRequest interceptor.after: " + getName());
            }
            return interceptor.after(inv, instruction);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("skip oncePerRequest interceptor.after: " + getName());
            }
        }
        return instruction;
    }

    @Override
    public final void afterCompletion(Invocation inv, Throwable ex) throws Exception {
        if (inv.getAttribute(filtered) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("do oncePerRequest interceptor.afterCompletion: " + getName());
            }
            interceptor.afterCompletion(inv, ex);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("skip oncePerRequest interceptor.afterCompletion: " + getName());
            }
        }
    }

    @Override
    public String toString() {
        return "oncePerRequest." + this.interceptor.toString();
    }

}
