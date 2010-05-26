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

    /**
     * 资源相对于上级的资源的名称，实际实现时，采用所表示结点的映射串(mappingPath)作为其值
     * 
     * @return
     */
    public String getSimpleName();

    /**
     * 注册该资源对指定请求方法的处理逻辑，可以对某一个具体的请求方法注册多个处理逻辑。
     * <p>
     * 一个请求方法有多个处理逻辑时，最终只有逻辑是真正执行的。不同的请求，根据其URI、参数等不同，真正执行的逻辑可能不一样。
     * 
     * @param method 可以使用 {@link ReqMethod#ALL}
     * @param engine
     */
    public void addEngine(ReqMethod method, Engine engine);

    /**
     * 返回某种请求方法的处理逻辑
     * 
     * @param method
     * @return 如果没有注册处理逻辑，返回一个长度为0的数组
     */
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
