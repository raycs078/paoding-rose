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

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ControllerInterceptorWrapper implements ControllerInterceptor,
        NamedControllerInterceptor {

    protected ControllerInterceptor interceptor;

    private String name;

    public ControllerInterceptorWrapper(ControllerInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public ControllerInterceptor getInterceptor() {
        return interceptor;
    }

    @Override
    public String getName() {
        if (interceptor instanceof NamedControllerInterceptor) {
            name = ((NamedControllerInterceptor) interceptor).getName();
        }
        return name;
    }

    @Override
    public void setName(String name) {
        if (interceptor instanceof NamedControllerInterceptor) {
            ((NamedControllerInterceptor) interceptor).setName(name);
        }
        this.name = name;
    }

    @Override
    public int getPriority() {
        return interceptor.getPriority();
    }

    @Override
    public boolean isSupportDispatcher(Dispatcher dispatcher) {
        return interceptor.isSupportDispatcher(dispatcher);
    }

    @Override
    public Object before(Invocation inv) throws Exception {
        return interceptor.before(inv);
    }

    @Override
    public Object after(Invocation inv, Object instruction) throws Exception {
        return interceptor.after(inv, instruction);
    }

    @Override
    public void afterCompletion(Invocation inv, Throwable ex) throws Exception {
        interceptor.afterCompletion(inv, ex);
    }

    @Override
    public List<Class<? extends Annotation>> getAnnotationClasses() {
        return interceptor.getAnnotationClasses();
    }

}
