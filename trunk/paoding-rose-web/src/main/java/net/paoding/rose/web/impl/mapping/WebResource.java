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
package net.paoding.rose.web.impl.mapping;

import java.util.List;

import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.thread.Engine;

/**
 * {@link WebResource} 代表一个可参数化的资源及其支持的操作逻辑。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface WebResource {

    public String getName();

    /**
     * 添加该资源的某种操作，如果所给的 method 是 {@link ReqMethod#ALL}
     * ，则不覆盖之前设置操作，只影响那些还没有设置的操作。
     * 
     * @param method
     * @param engine
     */
    public void addEngine(ReqMethod method, Engine engine);

    //    /**
    //     * 返回处理这个资源的处理逻辑，如果该资源不支持该操作方法返回null。
    //     * 
    //     * @param method 除 {@link ReqMethod#ALL} 外的其他 {@link ReqMethod} 实例
    //     * @return
    //     */
    //    public Engine getEngine(ReqMethod method);

    public Engine[] getEngines(ReqMethod method);

    /**
     * 本资源是否支持此操作?
     * 
     * @param method
     * @return
     */
    public boolean isMethodAllowed(ReqMethod method);

    public List<ReqMethod> getAllowedMethods();

    /**
     * 销毁该资源
     */
    public void destroy();

    public String toString();
}
