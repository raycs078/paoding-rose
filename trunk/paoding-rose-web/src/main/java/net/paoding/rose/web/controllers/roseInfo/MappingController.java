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

import net.paoding.rose.web.annotation.HttpFeatures;
import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.impl.thread.tree.MappingNode;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class MappingController {

    @Get
    @HttpFeatures(contentType = "text/xml; charset=UTF-8")
    public String list(MappingNode leaf) throws Exception {
        MappingNode root = leaf;
        while (root.parent != null) {
            root = root.parent;
        }
        StringBuilder sb = new StringBuilder(2048);
        sb.append("@<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        println(root, sb);
        return sb.toString();
    }

    private void println(MappingNode node, StringBuilder sb) {
        sb.append("<node path=\"").append(node.mapping.getPath());
        sb.append("\" target=\"").append(node.mapping.getTarget()).append("\">");
        MappingNode si = node.leftMostChild;
        if (si != null) {
            println(si, sb);
            while ((si = si.sibling) != null) {
                println(si, sb);
            }
        }
        sb.append("</node>");
    }
}
