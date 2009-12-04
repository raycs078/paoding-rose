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
package net.paoding.rose.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注 {@link ReqMapping}设置对控制器、action方法的自定义映射规则。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@Target( { ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReqMapping {

    static String DEFAULT_PATH = "#";

    /**
     * 设定哪些路径应由所在的action方法、控制器处理，可以设置多个。
     * <p>
     * 可以在设置中使用不限制个数的${xx}，并结合 {@link Param}标注使用 。 路径是否以'/'开头不做区别。
     * <p>
     * 对action方法，如果设置了一个空串路径，这等价于标注了 {@link REST}并设置所有的HTTP请求方法都由这个action处理
     * <br>
     * 不同的action方法一般应设置可以相互区分的path，
     * 但是对于像表单的展现以及提交往往设置成相同的path，只是通过http请求方法区分，此时你应该设置methods。
     * <p>
     * 对controller，如果设置了一个空串路径，等价于@DefaultController，同时原来的默认映射不再生效<br>
     * 比如对UserController设置了@ReqMapping(path="")那么通过/user这个URI不再能够访问这个控制器
     * <p>
     * 
     * 特别的，如果不想让一个控制器或action方法生效，除了使用@Ignored外(推荐)，还可以使用path={}0长度数组来实现.
     * 
     * @return
     */
    String[] path() default { DEFAULT_PATH };

    /**
     * 设置什么样的http请求方法的请求由这个标注所在的action方法处理
     * <p>
     * 
     * @return
     */
    ReqMethod[] methods() default { ReqMethod.GET, ReqMethod.POST };


}
