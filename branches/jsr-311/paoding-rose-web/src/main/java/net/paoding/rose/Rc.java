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
package net.paoding.rose;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.ControllerInterceptor;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationUtils;
import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.var.Flash;
import net.paoding.rose.web.var.Model;
import net.paoding.rose.web.var.PrivateVar;

import org.springframework.validation.BindingResult;

/**
 * {@link Rc} 将1.0版本被删除， 请在控制器方法中声明 {@link Invocation} inv 代替完成对{@link Rc}
 * 的使用
 * <p>
 * {@link Rc} 主要用于Rose框架内部,但也可用于在基于Rose框架的应用中<strong>小心地</strong>使用,即：
 * 只能在request发生时所在的线程中使用.
 * <p>
 * 通过 {@link Rc} 提供了可以访问到全局性的对象(比如ServletContext、WebApplicationContext对象),<br>
 * 以及绑定在当前线程的请求对象和 {@link Invocation}对象等的静态方法.
 * <p>
 * <strong>编程建议</strong>：应尽可能不用本类提供的静态方法，而通过以下方式获取等价功能：<br>
 * 1、在控制器中，可在方法中声明 {@link Invocation} inv 参数，获取和本次请求调用相关的对象。<br>
 * 2、在拦截器 {@link ControllerInterceptor} 、错误处理器
 * {@link ControllerErrorHandler} 中，直接使用接口方法传入的 {@link Invocation}
 * 参数，获取和本次请求调用相关的对象。<br>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @since 0.9
 */
@Deprecated
public class Rc {

    //-------------------------------------------------------------------

    /**
     * 返回绑定到当前线程的请求对象.
     * 
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     */
    public static HttpServletRequest request() {
        return InvocationUtils.getCurrentThreadRequest();
    }

    /**
     * 在控制器中调用此方法，返回当前的HTTP响应对象
     * 
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     */
    public static HttpServletResponse response() {
        return invocation().getResponse();
    }

    /**
     * 获取本次绑定到当前线程的请求当前时刻的 {@link Invocation}对象.
     * 
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     * @see PrivateVar#getInvocation()
     */
    public static Invocation invocation() {
        return InvocationUtils.getInvocation(request());
    }

    /**
     * 在控制器中调用此方法，返回当前的HttpSession,如果之前没有HttpSession，按照Servlet API说明将自动创建
     * 
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     * @see HttpServletRequest#getSession()
     */
    public static HttpSession session() {
        return request().getSession();
    }

    /**
     * 在控制器中调用此方法，返回当前的HttpSession，如果之前没有HttpSession，则按照create参数指示是否创建
     * 
     * @param create
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     * @see HttpServletRequest#getSession(boolean)
     */
    public static HttpSession session(boolean create) {
        return request().getSession(create);
    }

    /**
     * 将对象、数组对象、集合对象加入到MVC的M中。(具体请看靠 {@link Model#add(Object)})
     * 
     * @see Model#add(Object)
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     * @see Model#add(Object)
     */
    public static Model addModel(Object value) {
        return model().add(value);
    }

    /**
     * 将对象、数组对象、集合对象加入到MVC的M中。(具体请看靠 {@link Model#add(String, Object)})
     * 
     * @see Model#add(String, Object)
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     * @see Model#add(String, Object)
     */
    public static Model addModel(String name, Object value) {
        return model().add(name, value);
    }

    /**
     * 在控制器中调用此方法，获取当前的MVC架构中的模型容器，然后通过调用model的add方法将对象传送到视图View中
     * <p>
     * 一般情况直接使用 {@link #addModel(Object)}或
     * {@link #addModel(String, Object)}即可
     * 
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     * @see Invocation#getModel()
     */
    public static Model model() {
        return invocation().getModel();
    }

    /**
     * 获取Flash对象，支持跨请求的信息传递(比如在redirect中)
     * 
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     * @see Invocation#getFlash()
     */
    public static Flash flash() {
        return invocation().getFlash();
    }

    /**
     * 获取在URI、请求查询串(即问号后的xx=yyy)中所带的指定参数的参数值.
     * <p>
     * 如果相应的参数已经映射到方法中的@Param参数，那么返回的将不是String类型，而是参数的实际类型。
     * <p>
     * URI中的参数需要通过在action方法中通过使用 {@link ReqMapping}定制并识别，例如使用 {@literal @}
     * ReqMapping
     * (path="abc_{name}"声明方法，这个地址/controller/abc_user的请求对应的name参数值是user
     * <p>
     * 对于id=3&name=5这样的参数如果绑定到一个bean中(比如@Param("user") User
     * user)，那么Rc.param("user")将返回给bean对象
     * <p>
     * 如果没有存在给定的参数，返回null
     * <p>
     * 
     * @param name
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     * @see Invocation#getParameter(String)
     * @see Invocation#getMethodParameter(String)
     */
    public static Object param(String name) {
        return invocation().getParameter(name);
    }

    /**
     * 获取控制器action方法普通参数的绑定结果
     * 
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     */
    public static BindingResult getParameterBindingResult() {
        return invocation().getParameterBindingResult();
    }

    /**
     * 获取控制器action方法各个bean的绑定结果
     * 
     * @param bean
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     */
    public static BindingResult getBindingResult(Object bean) {
        return invocation().getBindingResult(bean);
    }

}
