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

import java.lang.reflect.Method;

import net.paoding.rose.web.annotation.HttpFeatures;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.impl.mapping.EngineGroup;
import net.paoding.rose.web.impl.mapping.MappingNode;
import net.paoding.rose.web.impl.thread.ActionEngine;
import net.paoding.rose.web.impl.thread.LinkedEngine;
import net.paoding.rose.web.impl.thread.Rose;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@Path( { "tree.xml", "tree" })
public class TreeController {

    @Get
    @HttpFeatures(contentType = "application/xml")
    public String list(Rose rose) throws Exception {
        MappingNode root = rose.getMappingTree();
        StringBuilder sb = new StringBuilder(2048);
        sb.append("@<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<rose-web>");
        println(root, "", sb);
        sb.append("</rose-web>");
        return sb.toString();
    }

    private void println(final MappingNode node, String prefix, StringBuilder sb) {

        sb.append("<node path=\"").append(prefix + node.getMappingPath()).append("\">");
        //
        EngineGroup leaf = node.getLeafEngines();
        if (leaf.size() > 0) {
            for (ReqMethod method : leaf.getAllowedMethods()) {
                for (LinkedEngine engine : leaf.getEngines(method)) {
                    ActionEngine action = (ActionEngine) engine.getTarget();
                    Method m = action.getMethod();
                    Class<?> cc = action.getControllerClass();
                    String rm = method.toString();
                    sb.append("<allowed ");
                    sb.append(rm + "=\"" + cc.getSimpleName() + "#" + m.getName() + "\" ");
                    sb.append("package=\"" + m.getDeclaringClass().getPackage().getName() + "\" ");
                    sb.append(" />");
                }
            }
        }

        MappingNode child = node.getLeftMostChild();
        while (child != null) {
            println(child, prefix + node.getMappingPath(), sb);
            child = child.getSibling();
        }
        sb.append("</node>");
    }
}
