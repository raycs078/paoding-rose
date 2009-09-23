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

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class OncePerRequestInterceptorWrapper extends ControllerInterceptorWrapper {

    private String alredyFiltered;

    public OncePerRequestInterceptorWrapper(ControllerInterceptor interceptor) {
        super(interceptor);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        this.alredyFiltered = "$$paoding-rose.interceptor.oncePerRequest." + name;
    }

    @Override
    public final Object before(Invocation inv) throws Exception {
        if (inv.getRequest().getAttribute(alredyFiltered) == null) {
            inv.getRequest().setAttribute(alredyFiltered, inv.getRequest());
            return super.before(inv);
        }
        return true;
    }

    @Override
    public final Object after(Invocation inv, Object instruction) throws Exception {
        if (inv.getRequest() == inv.getRequest().getAttribute(alredyFiltered)) {
            return super.after(inv, instruction);
        }
        return instruction;
    }

    @Override
    public final void afterCompletion(Invocation inv, Throwable ex) throws Exception {
        if (inv.getRequest() == inv.getRequest().getAttribute(alredyFiltered)) {
            super.afterCompletion(inv, ex);
        }
    }

}
