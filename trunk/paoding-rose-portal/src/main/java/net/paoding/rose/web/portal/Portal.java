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
package net.paoding.rose.web.portal;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.var.Model;

/**
 * 一个 {@link Portal} 对象邦定在 {@link HttpServletRequest} 之上，提供了 portal
 * 框架的编程接口。通过多次调用 {@link #addWindow(String, String)} 接口来为一个 portal 增加窗口。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface Portal {

    /**
     * 增加一个窗口到本 portal 中，增加后将立即被执行。窗口名字取所给的窗口地址，，使得在 portal 渲染页面中使用
     * ${windowPath} 渲染该窗口内容。
     * <p>
     * 
     * 如果 portal 框架使用了多线程并发执行，将被派发给专门的并发线程处理，否则则是串行执行，只有执行完一个真正的窗口后才返回。
     * 
     * 在 portal 返回的渲染页面中使用可使用 ${windowPath} 渲染该窗口内容， ${windowPath}实际是
     * {@link Window}对象， {@link Window#toString()} 可返回该窗口的文本内容。
     * 
     * @param windowPath 这个参数表示窗口的地址，取值规范同 forward 请求到其他地址的规范一样
     * @return
     */
    public Window addWindow(String windowPath);

    /**
     * 增加一个窗口到本 portal 中，增加后将立即被执行。窗口名字取所给的 name 参数。
     * <p>
     * 
     * 如果 portal 框架使用了多线程并发执行，将被派发给专门的并发线程处理，否则则是串行执行，只有执行完一个真正的窗口后才返回。
     * <p>
     * 
     * 在 portal 返回的渲染页面中使用可使用 ${name} 渲染该窗口内容， ${name}实际是 {@link Window}对象，
     * {@link Window#toString()} 可返回该窗口的文本内容。
     * 
     * @param name 窗口的名字，可用于在 portal 页面中通过 ${name} 的形式获取该窗口的渲染结果
     * @param windowPath 这个参数表示窗口的地址，取值规范同 forward 请求到其他地址的规范一样
     * @return
     */
    public Window addWindow(String name, String windowPath);

    /**
     * 返回添加到这个 {@link Portal} 对象上的窗口
     * 
     * @return
     */
    public List<? extends Window> getWindows();

    /**
     * 返回这个 {@link Portal} 对象邦定的 {@link Invocation} 对象
     * 
     * @return
     */
    public Invocation getInvocation();

    /**
     * 设置超时时间。这个时间指的是控制器返回之后， portal 等待所有窗口执行完毕的最大时间。
     * 
     * @param timeoutInMillis 毫秒，小于或等于0表示 portal 应等待所有窗口执行完毕或被取消才最终渲染页面给用户
     */
    public void setTimeout(long timeoutInMillis);

    /**
     * 
     * @return
     */
    public HttpServletRequest getRequest();

    /**
     * 
     * @return
     */
    public HttpServletResponse getResponse();

    /**
     * 
     * @return
     */
    public Model getModel();

    /**
     * 
     * @param name
     * @param value
     */
    public void addModel(String name, Object value);

    /**
     * 为这个portal实例注册一个侦听器
     * 
     * @param l 如果为null则进行忽略
     */
    public void addListener(PortalListener l);
}
