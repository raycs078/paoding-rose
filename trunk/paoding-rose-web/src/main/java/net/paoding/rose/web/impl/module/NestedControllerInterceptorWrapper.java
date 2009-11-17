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
package net.paoding.rose.web.impl.module;

import net.paoding.rose.web.ControllerInterceptor;
import net.paoding.rose.web.ControllerInterceptorWrapper;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.NamedControllerInterceptor;
import net.paoding.rose.web.OncePerRequestInterceptorWrapper;
import net.paoding.rose.web.annotation.Intercepted;
import net.paoding.rose.web.impl.thread.tree.AfterCompletion;

/**
 * {@link ControllerInterceptor}的一个简单封装，给被封装的拦截器一个命名。
 * <p>
 * 开发者在控制器配置 {@link Intercepted} 的allow和deny的名字指的就是这个名字
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class NestedControllerInterceptorWrapper extends ControllerInterceptorWrapper implements
        ControllerInterceptor, NamedControllerInterceptor {

    public static class Builder {

        private boolean oncePerRequest;

        private String name;

        private ControllerInterceptor interceptor;

        public Builder(ControllerInterceptor interceptor) {
            this.interceptor = interceptor;
        }

        public Builder name(String name) {
            if (name.indexOf('.') != -1) {
                throw new IllegalArgumentException("illegal name '" + name
                        + "', the dot char is not allowed.");
            }
            this.name = name;
            return this;
        }

        public Builder oncePerRequest(boolean oncePerRequest) {
            this.oncePerRequest = oncePerRequest;
            return this;
        }

        public NestedControllerInterceptorWrapper build() {
            ControllerInterceptor interceptor = this.interceptor;
            if (oncePerRequest) {
                interceptor = new OncePerRequestInterceptorWrapper(interceptor);
            }
            NestedControllerInterceptorWrapper wrapper = new NestedControllerInterceptorWrapper(
                    interceptor);
            wrapper.setName(name);
            return wrapper;
        }
    }

    private NestedControllerInterceptorWrapper(ControllerInterceptor interceptor) {
        super(interceptor);
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
    public String toString() {
        return getName();
    }

}
