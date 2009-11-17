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

import net.paoding.rose.scanner.ResourceInfo;
import net.paoding.rose.scanner.RoseScanner;
import net.paoding.rose.web.annotation.rest.Get;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class JarController implements BaseController {

    @Get
    public String list() throws Exception {
        List<ResourceInfo> infos = RoseScanner.getInstance().getJarResources();
        StringBuilder sb = new StringBuilder(1024).append("<ul>");
        for (ResourceInfo info : infos) {
            sb.append("<li>");
            sb.append(info.getResource().getURL());
            sb.append(Arrays.toString(info.getModifiers()));
            sb.append("</li>");
        }
        sb.append("</ul>");
        return Frame.wrap(sb.toString());
    }
}
