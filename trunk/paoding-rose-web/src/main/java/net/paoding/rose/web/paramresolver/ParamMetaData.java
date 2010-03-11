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
package net.paoding.rose.web.paramresolver;

import java.lang.reflect.Method;

import net.paoding.rose.web.annotation.DefValue;
import net.paoding.rose.web.annotation.FlashParam;
import net.paoding.rose.web.annotation.Param;

/**
 * {@link ParamMetaData} 封装对一个控制器方法的某个参数的描述
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */

public interface ParamMetaData {

    /**
     * 所在的控制器类
     * 
     * @return
     */
    public Class<?> getControllerClass();

    /**
     * 所在的方法
     * 
     * @return
     */
    public Method getMethod();

    /**
     * 该参数的声明类型
     * 
     * @return
     */
    public Class<?> getParamType();

    /**
     * 该参数的名字
     * 
     * @return
     */
    public String getParamName();

    /**
     * 返回对该参数的 {@link Param} 注解；可能为null
     * 
     * @return
     */
    public Param getParamAnnotation();

    public DefValue getDefaultValue();

    /**
     * 返回对该参数的 {@link FlashParam} 注解；可能为null
     * 
     * @return
     */
    public FlashParam getFlashParamAnnotation();

    public int getIndex();

    public void setUserObject(Object key, Object userObject);

    public Object getUserObject(Object key);

}
