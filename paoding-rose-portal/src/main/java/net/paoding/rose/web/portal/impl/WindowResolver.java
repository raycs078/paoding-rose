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
package net.paoding.rose.web.portal.impl;

import java.lang.reflect.Method;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.paramresolver.ParamResolverBean;
import net.paoding.rose.web.portal.PortalUtils;
import net.paoding.rose.web.portal.Window;

/**
 * 解析声明在窗口控制器中的Window参数
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class WindowResolver implements ParamResolverBean {

    @Override
    public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
        return Window.class == parameterType;
    }

    @Override
    public Object resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
            Invocation inv, String parameterName, Param paramAnnotation) throws Exception {
        return PortalUtils.getWindow(inv);
    }
}
