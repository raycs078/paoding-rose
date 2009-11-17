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

import java.util.Arrays;
import java.util.List;

import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.module.ControllerInfo;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.thread.tree.Rose;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ModuleController implements BaseController {

    @Get
    public Object list(Rose rose) throws Exception {
        List<Module> modules = rose.getModules();
        StringBuilder sb = new StringBuilder(2048);
        int i = 1;
        for (Module module : modules) {
            sb.append("<div style=\"margin-top:5px;border-top:solid black 1px;\"><table>");
            // number
            sb.append("<tr valign=\"top\"><td>").append(i++).append("</td><td></td></tr>");
            // mappingPath
            sb.append("<tr><td>mappingPath</td><td>").append(module.getMappingPath()).append(
                    "</td></tr>");
            // relativePackagePath
            sb.append("<tr><td>relativePackagePath</td><td>").append(
                    module.getRelativePackagePath()).append("</td></tr>");
            // url
            sb.append("<tr valign=\"top\"><td>url</td><td>").append(module.getUrl()).append(
                    "</td></tr>");
            sb.append("<tr valign=\"top\"><td>controllers</td><td>");
            for (Mapping<ControllerInfo> info : module.getControllerMappings()) {
                sb.append("'").append(info.getPath()).append("'=").append(
                        info.getTarget().getControllerClass().getName()).append(";<br>");
            }
            sb.append("</td></tr>");
            // resolvers
            sb.append("<tr><td valign=\"top\">resolvers</td><td>").append(
                    Arrays.toString(module.getCustomerResolvers().toArray())).append("</td></tr>");
            // validators
            sb.append("<tr><td valign=\"top\">validators</td><td>").append(
                    Arrays.toString(module.getValidators().toArray())).append("</td></tr>");
            // interceptors
            sb.append("<tr><td valign=\"top\">interceptors</td><td>").append(
                    Arrays.toString(module.getInterceptors().toArray())).append("</td></tr>");
            // errorhandler
            sb.append("<tr><td>errorHanlder</td><td>").append(
                    module.getErrorHandler() == null ? "" : module.getErrorHandler()).append(
                    "</td></tr valign=\"top\">");
            // defaultHanlder
            Mapping<ControllerInfo> defaultController = module.getDefaultController();
            sb.append("<tr valign=\"top\"><td>defaultController</td><td>").append(
                    defaultController == null ? "" : defaultController.getTarget()
                            .getControllerClass().getName()).append("</td></tr>");
            // end
            sb.append("</table></div>");
        }
        return Frame.wrap(sb.toString());
    }
}
