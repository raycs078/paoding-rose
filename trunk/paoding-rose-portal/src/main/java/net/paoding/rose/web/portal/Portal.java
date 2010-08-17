/*
 * Copyright 2007-2012 the original author or authors.
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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.paramresolver.ParamResolver;
import net.paoding.rose.web.portal.impl.PortalResolver;

/**
 * {@link Portal} 是 {@link Window} 的容器，由多个 Window 对象组成，一旦把 Window 放到 Portal
 * 中，那么每个 Window 的执行将具有独立性和并发性。
 * <p>
 * 
 * <strong>*编程接口*</strong>
 * 
 * <pre>
 * 1、在需要使用Portal技术的控制器方法上声明一个Portal参数，比如public String index(Portal portal);
 * 2、Portal参数和Invocation等其他各种参数可以同时存在，包括Pipe pipe参数;
 * 3、在方法中，你可以不断地调用 {@link Portal#addWindow(String name, String path)} 方法增加给定地址的窗口；
 * 4、注册到Portal的窗口控制器方法，还可以继续声明Portal参数，但是新声明的Portal是另外的一个独立Portal，此时它是最先那个Portal的内嵌Portal;
 * </pre>
 * <p>
 * 
 * <strong>*rose和portal*</strong>
 * 
 * <pre>
 * 1、rose是一个应用于web开发框架，portal不是rose的核心，只是rose的一个插件；
 * 2、如果你不需要portal特性时，您可以把portal的jar包移走，而不会影响普通rose程序；
 * 3、portal使用rose开放出来的spring“配置文件”插入到rose框架中，使得portal可以在rose的程序中使用；
 * </pre>
 * 
 * <strong>*Portal的创建*</strong>
 * 
 * <pre>
 * 1、Portal参数的创建由框架完成，你只需要将Portal声明为方法的参数即可；
 * 2、rose框架提供了 {@link ParamResolver}接口，portal提供了该接口的实现 {@link PortalResolver}，并配置到 jar 包中的 applicatonContext*.xml，使得rose框架能够识别
 * 3、当rose框架确定一个地址应该由某个控制器来处理时，他就会在调用各种参数验证器、拦截器、控制器前创建好，并持续到页面渲染结束；
 * 4、虽然一个portal参数的声明周期直到页面渲染结束来完成，但如果您在多个控制器方法中声明Portal参数，请求在这些方法之间转发，这些Portal是不同的对象。
 * </pre>
 * 
 * 
 * <strong>*portal和window的执行*</strong>
 * 
 * <pre>
 * 1、当一个window加入到portal时，portal便会调用全局的executorService执行该window，不同的portal调用的都是同一个executorService;
 * 2、由于portal使用的是线程池实现的executorService，所以window的执行不由非web容器“主线程”执行(通常其线程名以http开始)，而是由线程名称都以portalExecutor开头的线程执行;
 * 3、在产品环境下，window的执行时候都会长于portal控制器本身的执行时间，portal需要这协调这些时间关系，框架会保证让portal控制器等得所有窗口都执行完毕之后才向用户吐页面内容；
 * 4、如果您认为portal线程不应该等待所有的窗口都执行完，而是应先把整体页面框架输出给客户端，一旦某个窗口执行完毕，再送回该窗口的页面数据，请使用rosepipe技术；
 * 
 * <pre>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface Portal {

    /**
     * 返回这个 {@link ServerPortal} 对象邦定的 {@link Invocation} 对象
     * <p>
     * 等价于您在控制器方法参数声明的Invocation inv。
     * 
     * @return
     */
    public Invocation getInvocation();

    /**
     * 请您调用: {@link #getInvocation()#addModel(String, Object)} 来完成
     * 现在是2010-08-04，2010国庆后将去掉此代码
     */
    @Deprecated
    public void addModel(String name, Object value);

    /**
     * 返回本次调用的 {@link HttpServletRequest}对象，等价于
     * {@link Portal#getInvocation()#getRequest()}
     * 
     * @return
     */
    public HttpServletRequest getRequest();

    /**
     * 返回本次调用的 {@link HttpServletResponse}对象，等价于
     * {@link Portal#getInvocation()#getResponse()}
     * 
     * @return
     */
    public HttpServletResponse getResponse();

    /**
     * 设置自定义的窗口渲染器，决定在给定的portal页面位置中，如何输出什么window代表的内容，比如嵌入在一个<div
     * class="window"></div>的块中。
     * <p>
     * portal默认没有窗口渲染器，此时之间将window的内容原封不动地输出到portal页面的指定位置中
     * 
     * @param render
     */
    public void setWindowRender(WindowRender render);

    /**
     * 返回设置的窗口渲染器
     * 
     * @return
     */
    public WindowRender getWindowRender();

    /**
     * 设置超时时间。这个时间指的是 portal 控制器返回之后，portal线程等待所有窗口执行完毕的最大时间。
     * 如果等待超过了这个时间，那些还为能执行完的window将被在被等待，portal将理解执行后续的流程，向客户端输出内容；
     * <p>
     * 如果portal认为一个window超时了，默认将会去 cancle 它，除非您设置了该window的
     * {@link Window#FUTURE_CANCEL_ENABLE_ATTR}
     * 为false，您要确切了解您的系统，看是否支持cancle
     * ，也就是对线程中断的响应策略(如果中断window会导致整个系统的基础设施被破坏，建议您addWindow时候，
     * 利用callback把window的FUTURE_CANCEL_ENABLE_ATTR属性设置为Boolean
     * .FALSE或者字符串"false"
     * 
     * @param timeoutInMillis 毫秒，小于或等于0表示不进行超时判断(这也是默认设置)，
     *        portal将等待所有窗口执行完毕或被取消才最终渲染页面给用户
     */
    public void setTimeout(long timeoutInMills);

    /**
     * 返回设置的超时时间
     * 
     * @return
     */
    public long getTimeout();

    /**
     * 为这个portal实例注册一个侦听器
     * 
     * @param l 如果为null则进行忽略
     */
    public void addListener(PortalListener l);

    /**
     * 增加一个窗口到本 portal 中，增加后将立即被执行。窗口名字取所给的窗口地址，使得在 portal 渲染页面中使用
     * ${windowPath} 渲染该窗口内容。
     * <p>
     * 
     * 如果 portal 框架使用了多线程并发执行，将被派发给专门的并发线程处理，否则则是串行执行，只有执行完一个真正的窗口后才返回。
     * 
     * 在 portal 返回的渲染页面中使用可使用 ${windowPath} 渲染该窗口内容， ${windowPath}实际是
     * {@link Window}对象， {@link Window#toString()} 可返回该窗口的文本内容。
     * 
     * @param windowPath 这个参数表示窗口的地址，可以包含参数比如xxx?a=b; <br>
     *        如果地址以'/'开始表示相对于该web应用的根路径(注意：这里是指该应用的根路径，而非跳脱之外的host:
     *        port下的根路径)<br>
     *        如果地址不一'/'开始表示相对当前的请求的URI地址<br>
     *        这个地址<strong>*不能*</strong>是其他远程地址，比如http://host:port/somepath
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
     * @param windowPath 这个参数表示窗口的地址，可以包含参数比如xxx?a=b; <br>
     *        如果地址以'/'开始表示相对于该web应用的根路径(注意：这里是指该应用的根路径，而非跳脱之外的host:
     *        port下的根路径)<br>
     *        如果地址不一'/'开始表示相对当前的请求的URI地址<br>
     *        这个地址<strong>*不能*</strong>是其他远程地址，比如http://host:port/somepath
     * @return
     */
    public Window addWindow(String name, String windowPath);

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
     * @param windowPath 这个参数表示窗口的地址，可以包含参数比如xxx?a=b; <br>
     *        如果地址以'/'开始表示相对于该web应用的根路径(注意：这里是指该应用的根路径，而非跳脱之外的host:
     *        port下的根路径)<br>
     *        如果地址不一'/'开始表示相对当前的请求的URI地址<br>
     *        这个地址<strong>*不能*</strong>是其他远程地址，比如http://host:port/somepath
     * @param attributes 在window未执行之前设置给这个window的属性，可以为null
     * @return
     */
    public Window addWindow(String name, String windowPath, Map<String, Object> attributes);

    /**
     * 
     * 增加一个窗口到本 portal 中，增加后将立即被执行。窗口名字取所给的 name 参数。
     * <p>
     * 
     * 如果 portal 框架使用了多线程并发执行，将被派发给专门的并发线程处理，否则则是串行执行，只有执行完一个真正的窗口后才返回。
     * <p>
     * 
     * 在 portal 返回的渲染页面中使用可使用 ${name} 渲染该窗口内容， ${name}实际是 {@link Window}对象，
     * {@link Window#toString()} 可返回该窗口的文本内容。
     * 
     * 
     * @param name 窗口名
     * @param windowPath 这个参数表示窗口的地址，可以包含参数比如xxx?a=b; <br>
     *        如果地址以'/'开始表示相对于该web应用的根路径(注意：这里是指该应用的根路径，而非跳脱之外的host:
     *        port下的根路径)<br>
     *        如果地址不一'/'开始表示相对当前的请求的URI地址<br>
     *        这个地址<strong>*不能*</strong>是其他远程地址，比如http://host:port/somepath
     * @param callback 在window创建后未被执行前的一些回掉
     * @return
     */
    public Window addWindow(String name, String windowPath, WindowCallback callback);

    /**
     * 返回添加到这个 {@link Portal} 上的窗口，如果没有条件返回一个size=0的列表；
     * <p>
     * 请不要对返回的列表做任何增删改操作，如果您需要，请clone出一份新的出来
     * 
     * @return
     */
    public List<Window> getWindows();

}
