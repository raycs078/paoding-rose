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

import net.paoding.rose.web.annotation.Intercepted;
import net.paoding.rose.web.impl.thread.tree.AfterCompletion;
import net.paoding.rose.web.instruction.Instruction;

/**
 * 实现{@link ControllerInterceptor}用于拦截整个MVC流程。(通常则是实现
 * {@link ControllerInterceptorAdapter})。
 * <p>
 * 如果你要实现的拦截器是模块特有的，可以直接在控制器所在package中实现它，并以Interceptor作为命名的结尾，
 * Rose会自动把它load到module中，使得控制器能够被该拦截器拦截。<br>
 * 同时因为某种原因，想暂时禁止掉这个module中的某个拦截器又不想删除它或者去除implements
 * ControllerInterceptor, 此时把类标注成@Ignored即可
 * <p>
 * 如果拦截器的实现是公有的(特别是已经打包成jar的拦截器)或者其他package的拦截器，则需要先把它配置在/WEB-INF/
 * applicationContext
 * *.xml或者某个jar包下applicationContext*.xml中，这样则能够拦截到所有模块的Controller。<br>
 * 如果不想让拦截器拦截到某些控制器，配置控制器@Intercepted的allow和deny属性， 或者通过使拦截器实现{
 * {@link #getAnnotationClasses()}
 * 明确要求只有标注了指定的该annotation的控制器或方法才可以被该拦截器拦截到
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @see Intercepted
 * @see ControllerInterceptorAdapter
 */
public interface ControllerInterceptor extends AfterCompletion {

    /**
     * 作为候选拦截器，这个拦截器是否应拦截所指的控制器或其方法？
     * 
     * @param controllerClazz
     * @param actionMethod
     * @return
     */
    public boolean isForAction(Class<?> controllerClazz, Method actionMethod);

    /**
     * 当所分配请求是所给的类型时，是否执行此拦截器？
     * 
     * @return
     */
    boolean isForDispatcher(Dispatcher dispatcher);

    /**
     * 返回一个数字，值大的具有最高优先拦截权
     * 
     * @return
     */
    public int getPriority();

    /**
     * 在调用控制器方法前调用。如果返回true(或者null)表示继续下一个拦截器；<br>
     * 返回其他的表示不再调用剩余的拦截器以及action，并按返回的指示执行结果(进行页面渲染或其他 )
     * {@link Instruction}向请求发送响应(可以是用字符串表示的指示对象)。
     * <p>
     * 在一个拦截器链条中，如果某一个拦截器拒绝了整个调用链条，其它还没拦截的拦截器将不再会进行拦截，但是之前已经拦截过的拦截器，
     * 还将分别调用它们的{@link #after(Invocation, Object)}和
     * {@link #afterCompletion(Invocation, Throwable)}方法拦截。
     * <p>
     * 
     * @param inv
     * @return
     * @throws Exception
     */
    Object before(Invocation inv) throws Exception;

    /**
     * 在调用拦截器方法后调用。也有可能是之后拦截器拒绝了该流程，回退过来调用到先前调用的拦截器的本方法。
     * <p>
     * 返回null或原来的instruction表示不改变控制器的返回结果。<br>
     * 返回另外的对象表示改变这个返回行为。这非常有用，或许通过拦截器能够将一个返回的对象转化为另外的对象以输出给请求着
     * 
     * @param inv
     * @param instruction
     * @return
     * @throws Exception
     */
    Object after(Invocation inv, Object instruction) throws Exception;

    /**
     * 整个流程(包括页面render流程)结束时调用，不管是否发生过异常。如果发生了异常，则将传送一个非空的Throwable对象到该方法。
     * <p>
     * 只有之前调用before时返回true时才会调用到它的afterRender方法
     * 
     * @param inv
     * @param ex
     * @throws Exception
     */
    void afterCompletion(Invocation inv, Throwable ex) throws Exception;

}
