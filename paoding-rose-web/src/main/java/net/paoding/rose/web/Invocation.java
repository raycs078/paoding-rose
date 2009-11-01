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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.var.Flash;
import net.paoding.rose.web.var.Model;

import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

/**
 * {@link Invocation}代表对一个Controller action
 * 方法的一次调用，通过它能够了解这个调用的方法名、参数以及哪个Controller等
 * <p>
 * 每一个{@link Invocation} 实例的生命周期仅在一次request之内， {@link Invocation}作为拦截器
 * {@link ControllerInterceptor} 方法的一个参数，对一次独立的控制器action调用请求只使用一个实例，
 * 即对同一次action方法的调用过程中所有拦截器中的方法的 {@link Invocation}
 * 参数总是同一个实例。所以，如有需要在拦截器之间传递参数的话，您可以使用
 * {@link #setAttribute(String, Object)}和 {@link #getAttribute(String)}方法。
 * <p>
 * 
 * {@link Invocation}实例。
 * <p>
 * 
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface Invocation {

    /**
     * 将要调用的控制器对象(可能已经是一个Proxy、CGlib等等后的对象，不是原控制器的直接实例对象)
     * 
     * @return
     */
    public Object getController();

    /**
     * 将要调用的控制器的类名，这个类名就是编写控制器类时的那个类
     * <p>
     * 他不总是和本次调用的控制器对象的getClass()相同，
     * 
     * @return
     */
    public Class<?> getControllerClass();

    /**
     * 将要调用的方法
     * 
     * @return
     */
    public Method getMethod();

    /**
     * 将要调用的方法的参数的名字(这个名字不是String id的userId这样的名字，而是@Param("userId") String
     * id的userId)
     * 
     * @see Param
     * @return
     */
    public String[] getMethodParameterNames();

    /**
     * 将要调用的方法的参数值
     * 
     * @return
     */
    public Object[] getMethodParameters();

    /**
     * 获取在URI、flash信息、请求查询串(即问号后的xx=yyy)中所带的参数值
     * <p>
     * URI中的参数需要通过在action方法中通过类似@ReqMapping(path="user_${name}")进行声明，
     * 才可以获取name的参数<br>
     * 对于id=3&name=5这样的参数如果绑定到一个bean中(比如@Param("user") User
     * user)，那么getParameter("user")将返回给bean对象
     * <p>
     * 如果这个参数在action方法中声明了参数，则返回该参数，即不再是String类型的。<br>
     * 如果这个参数没有在action方法中声明，则返回的是普通String类型<br>
     * 如果没有存在给定的参数，返回null
     * <p>
     * 
     * @param name
     * @return
     */
    public Object getParameter(String name);

    /**
     * 获取在URI、flash信息、请求查询串(即问号后的xx=yyy)中所带的参数值
     * <p>
     * URI中的参数需要通过在action方法中通过类似@ReqMapping(path="user_${name}")进行声明，
     * 才可以获取name的参数<br>
     * <p>
     * 这个参数总是返回String类型<br>
     * 如果没有存在给定的参数，返回null
     * <p>
     * 
     * @param name
     * @return
     */
    public String getRawParameter(String name);

    /**
     * 返回给定名字的方法参数。
     * <p>
     * 
     * @return
     */
    public Object getMethodParameter(String name);

    /**
     * 
     * @param index
     * @param value
     */
    public void changeMethodParameter(int index, Object value);

    /**
     * 
     * @param name
     * @param value
     */
    public void changeMethodParameter(String name, Object value);

    /**
     * 将对象(object,array,collection等)加入到MVC中的Model中作为一个属性，通过它传递给View
     * <p>
     * 将使用该对象的类名头字母小写的字符串作为名字；<br>
     * 如果对象是数组，去数组元素的类的类名字头字母小写加上"List"作为名字<br>
     * 如果对象是集合元素，取其第一个元素的类的类名字头字母小写加上"List"作为名字<br>
     * 如果该值为空或者其集合长度为0的话，将被忽略<br>
     * 
     * @param value 可以是普通对象，数组对象，集合对象；<br>
     *        可以为null，如果对象为null或集合长度为0直接被忽略掉
     * @see Model#add(Object)
     * @see #getModel()
     */
    public void addModel(Object value);

    /**
     * 将对象(object,array,collection等)加入到MVC中的Model中作为一个属性，通过它传递给View
     * 
     * @param name 在view中这个字符串将作为该对象的名字被使用；非空
     * @param value 可以是普通对象，数组对象，集合对象；<br>
     *        可以为null，如果对象为null直接被忽略掉
     * @see Model#add(String, Object)
     * @see #getModel()
     */
    public void addModel(String name, Object value);

    /**
     * 返回Model接口，通过这个设置对象给view渲染
     * 
     * @return
     */
    public Model getModel();

    /**
     * 设置一个和本次调用关联的属性。这个属性可以在多个拦截器中共享。
     * 
     * @param name
     * @param value
     * @return
     */
    public Invocation setAttribute(String name, Object value);

    /**
     * 获取前面拦截器或代码设置的，和本次调用相关的属性
     * 
     * @param name
     * @return
     */
    public Object getAttribute(String name);

    /**
     * 
     * @param name
     * @param value
     * @return
     */
    public Invocation setOncePerRequestAttribute(String name, Object value);

    /**
     * 获取前面拦截器或代码设置的，和本次调用相关的属性
     * 
     * @param name
     * @return
     */
    public Object getOncePerRequestAttribute(String name);

    /**
     * 
     * @param name
     */
    public void removeAttribute(String name);

    /**
     * 返回本次调用相关的所有属性名字
     * 
     * @return
     */
    public Set<String> getAttributeNames();

    /**
     * 用于向重定向跳转后的页面传递参数，比如提示信息
     * 
     * @param name
     * @param msg
     */
    public void addFlash(String name, String msg);

    /**
     * 
     * @return
     */
    public Flash getFlash();

    /**
     * 
     * @return
     */
    public RequestPath getRequestPath();

    /**
     * 返回本次调用的 {@link HttpServletRequest}对象
     * 
     * @return
     */
    public HttpServletRequest getRequest();

    /**
     * 返回本次调用的 {@link HttpServletResponse}对象
     * 
     * @return
     */
    public HttpServletResponse getResponse();

    /**
     * 
     * @return
     */
    public WebApplicationContext getApplicationContext();

    /**
     * 
     * @return
     */
    public ServletContext getServletContext();

    /**
     * 
     * @return
     */
    public List<String> getBindingResultNames();

    /**
     * 
     * @return
     */
    public List<BindingResult> getBindingResults();

    /**
     * 获取控制器action方法普通参数的绑定结果
     * 
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     */
    public BindingResult getParameterBindingResult();

    /**
     * 获取控制器action方法各个bean的绑定结果
     * 
     * @param bean bean实体对象或bindingResult的名字
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     */
    public BindingResult getBindingResult(Object bean);

    /**
     * 
     * @param request
     */
    public void setRequest(HttpServletRequest request);

}
