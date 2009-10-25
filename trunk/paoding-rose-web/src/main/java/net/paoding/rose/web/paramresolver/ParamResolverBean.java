/*
 * $Id$
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
package net.paoding.rose.web.paramresolver;

import java.lang.reflect.Method;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @param <T>
 */
public interface ParamResolverBean {

    /**
     * 返回true表示是由本解析器负责解析这种类型的参数.
     * 
     * @param parameterType
     * @param method
     * @return
     */
    public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method);

    /**
     * @param parameterType
     * @param replicatedCount 在本次 {@link Invocation}中，使用此 {@link ParamResolverBean}
     *        对象的总个数
     * @param indexOfReplicated 此次resolve调用是对本对象的resolve的第几次调用(0,1,2,3...)
     * @param inv
     * @param parameterName 这个参数所使用的名称，同时也表示这个参数的值应该从所给名的请求数据中获取
     * @param paramAnnotation 可能为null，如果控制器没有标注的话
     * @return
     * @throws Exception
     */
    public Object resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
            Invocation inv,//
            String parameterName, Param paramAnnotation) throws Exception;
}
