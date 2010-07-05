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
package net.paoding.rose.web.controllers.roseInfo;

import net.paoding.rose.RoseVersion;
import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;

import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class AccessControlInterceptor extends ControllerInterceptorAdapter {

    @Override
    public Object before(Invocation inv) throws Exception {
        if (!LogFactory.getLog(inv.getControllerClass()).isDebugEnabled()) {
            inv.getResponse().setContentType("text/html;encoding=utf-8");
            return Frame.wrap(String.format(
                    "Forbidden [DEBUG NOT ENABLED %s]<br> Rose-Version: %s", inv
                            .getControllerClass().getName(), RoseVersion.getVersion()));
        }
        return true;
    }
}
