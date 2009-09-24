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

import java.util.ArrayList;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.thread.ActionEngine;
import net.paoding.rose.web.impl.thread.ControllerEngine;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.impl.thread.ModuleEngine;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class MappingController {

    @Get
    public String list(Invocation inv) throws Exception {
        InvocationBean invimpl = ((InvocationBean) inv);
        Mapping<ModuleEngine>[] modules = invimpl.getRoseEngine().getModuleMappings();
        StringBuilder sb = new StringBuilder(2048);
        for (int i = 0; i < modules.length; i++) {
            Mapping<ControllerEngine>[] controllers = modules[i].getTarget()
                    .getControllerMappings();
            for (Mapping<ControllerEngine> controller : controllers) {
                ArrayList<Mapping<ActionEngine>> actions = controller.getTarget()
                        .getActionMappings();
                sb.append("<div><table border=\"1\">");
                for (Mapping<ActionEngine> action : actions) {
                    sb.append("<tr><td width=\"250\">").append(modules[i].getPath());
                    sb.append(controller.getPath());
                    sb.append(action.getPath()).append("</td><td>》");
                    sb.append(action.getTarget().getControllerEngine().getControllerClass()
                            .getName());
                    sb.append(".");
                    sb.append(action.getTarget().getMethod().getName()).append("</td></tr>");
                }
                sb.append("</table></div>");
            }
        }
        return Frame.wrap(sb.toString());
    }
}
