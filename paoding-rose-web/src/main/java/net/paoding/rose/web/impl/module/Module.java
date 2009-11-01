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

import java.net.URL;
import java.util.List;

import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.NamedValidator;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.paramresolver.ParamResolver;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartResolver;

/**
 * {@link Module}代表了在同一个web程序中的一个模块。不同的模块包含一些特定的控制器对象、控制器拦截器、控制器异常处理器。
 * 不同的模块共享了ServletContext以及整个程序的中间层、资源层。
 * <p>
 * 一个web程序的不同的模块有不同的名字和路径。作为{@link Module}接口本身并没有要求模块的名字和路径有什么关系，
 * 但在实现上模块的路径是由其名字决定的，即path=/name，比如名字为admin的模块，路径将是/admin。
 * 作为一个特例，名字为root的模块路径则只是空串。
 * <p>
 * 一个HTTP请求将根据它的URI，映射到相应的web程序中(由web容器处理)，而后又映射给具体的module模块(由Rose处理)。
 * 映射规则以模块的路径为依据(名字此时不参与这个决策)。
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public interface Module {

    /**
     * 该module负责的请求的URL(context path剔除外)的起始地址
     * 
     * @return
     */
    public String getMappingPath();

    /**
     * 
     * @return
     */
    public URL getUrl();

    /**
     * 当URI没有指明控制器名时，默认使用哪个控制器？
     * 
     * @return
     */
    public Mapping<ControllerInfo> getDefaultController();

    /**
     * @return
     */
    public List<Mapping<ControllerInfo>> getControllerMappings();

    /**
     * 返回本module所使用的拦截器。
     * 
     * @return
     */
    public List<NestedControllerInterceptorWrapper> getInterceptors();

    /**
     * 
     * @return
     */
    public List<ParamResolver> getCustomerResolvers();

    /**
     * @return
     */
    public WebApplicationContext getApplicationContext();

    /**
     * 该模块的异常处理类，这个异常处理类处理的是控制器抛出的异常，开发者可以利用这个特性记录控制器抛出的异常并重新返回一个友好的页面给用户
     * 
     * @return
     */
    public ControllerErrorHandler getErrorHandler();

    /**
     * @return
     */
    public MultipartResolver getMultipartResolver();

    /**
     * 
     * @return
     */
    public Module getParent();

    public String getRelativePackagePath();

    Module addValidator(NamedValidator validator);

    List<NamedValidator> getValidators();

}
