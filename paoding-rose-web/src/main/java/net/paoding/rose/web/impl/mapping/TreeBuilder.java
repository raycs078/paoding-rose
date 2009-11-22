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
package net.paoding.rose.web.impl.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.module.ControllerRef;
import net.paoding.rose.web.impl.module.MethodRef;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.thread.ActionEngine;
import net.paoding.rose.web.impl.thread.ControllerEngine;
import net.paoding.rose.web.impl.thread.Engine;
import net.paoding.rose.web.impl.thread.ModuleEngine;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */

// FIXME: 不要使用ModuleBuilder,而直接使用new ModuleImpl()实现?
public class TreeBuilder {

    /*
     * 构造一个树，树的结点是地址-资源映射，每个结点都能回答是否匹配一个字符串，每个匹配的节点都知道如何执行对该资源的操作.
     * 构造树的过程：
     *   识别组件==>求得他的资源定义==>判断是否已经创建了==>未创建的创建一个树结点==>已创建的找出这个结点
     *   ==>在这个资源增加相应的操作以及逻辑==>若是新建的结点把它加到树中，同时满足遍历、匹配顺序要求
     */
    public void create(MappingNode tree, List<Module> modules) {
        root(tree, modules);
    }

    private void root(MappingNode rootNode, List<Module> modules) {
        for (Module module : modules) {
            module(rootNode, module);
        }
    }

    private void module(final MappingNode rootNode, Module module) {
        WebResource moduleResource;
        MappingImpl mapping = new MappingImpl(module.getMappingPath(), MatchMode.STARTS_WITH);
        MappingNode moduleNode = rootNode.getChild(mapping);
        if (moduleNode == null) {
            moduleResource = new WebResource(rootNode.getResource(), mapping.getPath());
            mapping.setResource(moduleResource);
            moduleNode = new MappingNode(mapping, rootNode);
        } else {
            moduleResource = moduleNode.getResource();
        }
        moduleResource.addEngine(ReqMethod.ALL, new ModuleEngine(module));

        // controllers
        List<ControllerRef> controllers = module.getControllers();
        for (ControllerRef controller : controllers) {
            controller(module, moduleNode, controller);
        }

        // defaults

        WebResource defResource;
        MappingImpl defMapping = new MappingImpl("", MatchMode.STARTS_WITH, null);
        MappingNode defNode = moduleNode.getChild(defMapping);
        List<ReqMethod> allowed = null;
        if (defNode == null) {
            allowed = Collections.emptyList();
            defResource = new WebResource(moduleResource, "");
        } else {
            defResource = defNode.getResource();
            allowed = defResource.getAllowedMethods();
        }
        defMapping.setResource(defResource);
        MappingNode defTargetNode = null;
        ReqMethod reqMethod = ReqMethod.GET; // controller的，只对GET做default
        if (!allowed.contains(reqMethod)) {
            String[] candidates = getControllerCandidates(reqMethod);
            for (String candidate : candidates) {
                WebResource tempResource = null;
                MappingImpl tempMapping = new MappingImpl(candidate, MatchMode.STARTS_WITH);
                MappingNode tempNode = moduleNode.getChild(tempMapping);
                if (tempNode != null) {
                    defTargetNode = tempNode;
                    tempResource = tempNode.getResource();
                    tempMapping.setResource(tempResource);
                    defResource.addEngine(reqMethod, tempResource.getEngine(reqMethod));
                    break;
                }
            }
        }
        if (defNode == null && defResource.getAllowedMethods().size() > 0) {
            defNode = new MappingNode(defMapping, moduleNode);
            List<ReqMethod> filters = new ArrayList<ReqMethod>(1);
            filters.add(reqMethod);
            defTargetNode.copyTo(defNode, filters);
        }
    }

    private void controller(Module module, MappingNode moduleNode, ControllerRef controller) {
        for (String mappingPath : controller.getMappingPaths()) {
            MappingImpl controllerMapping = new MappingImpl(mappingPath, MatchMode.STARTS_WITH);
            MappingNode controllerNode = moduleNode.getChild(controllerMapping);
            WebResource controllerResource;
            if (controllerNode == null) {
                controllerResource = new WebResource(moduleNode.getResource(), controllerMapping
                        .getPath());
                controllerMapping.setResource(controllerResource);
                controllerNode = new MappingNode(controllerMapping, moduleNode);
            } else {
                controllerResource = controllerNode.getResource();
            }
            Engine controllerEngine = new ControllerEngine(module, mappingPath, controller);
            controllerResource.addEngine(ReqMethod.ALL, controllerEngine);

            // actions
            MethodRef[] actions = controller.getActions();
            for (MethodRef action : actions) {
                action(module, controller, action, controllerNode);
            }

            // defaults
            WebResource defResource;
            MappingImpl defMapping = new MappingImpl("", MatchMode.EQUALS);
            MappingNode defNode = controllerNode.getChild(defMapping);
            List<ReqMethod> allowed = null;
            if (defNode == null) {
                allowed = Collections.emptyList();
                defResource = new WebResource(controllerResource, "");
            } else {
                defResource = defNode.getResource();
                allowed = defResource.getAllowedMethods();
            }
            defMapping.setResource(defResource);
            for (ReqMethod reqMethod : ReqMethod.ALL.parse()) {
                if (!allowed.contains(reqMethod)) {
                    String[] candidates = getActionCandidates(reqMethod);
                    for (String candidate : candidates) {
                        MappingImpl tempMapping = new MappingImpl(candidate, MatchMode.EQUALS, null);
                        MappingNode tempNode = controllerNode.getChild(tempMapping);
                        if (tempNode != null) {
                            WebResource tempResource = tempNode.getResource();
                            tempMapping.setResource(tempResource);
                            defResource.addEngine(reqMethod, tempResource.getEngine(reqMethod));
                            break;
                        }
                    }
                }
            }
            if (defNode == null && defResource.getAllowedMethods().size() > 0) {
                new MappingNode(defMapping, controllerNode);
            }
        }
    }

    private String[] getControllerCandidates(ReqMethod reqMethod) {
        if (reqMethod == ReqMethod.GET) {
            return new String[] { "/index", "/home", "/welcome" };
        }
        return new String[] {};
    }

    private String[] getActionCandidates(ReqMethod reqMethod) {
        if (reqMethod == ReqMethod.GET) {
            return new String[] { "/index", "/get", "/render" };
        }
        if (reqMethod == ReqMethod.POST) {
            return new String[] { "/post", "/add", "/create", "/update" };
        }
        if (reqMethod == ReqMethod.PUT) {
            return new String[] { "/put", "/update" };
        }
        return new String[] { "/" + reqMethod.toString().toLowerCase() };
    }

    private void action(Module module, ControllerRef controller, MethodRef action,
            MappingNode controllerNode) {
        MappingImpl mapping;
        Map<String, Set<ReqMethod>> mappings = action.getMappings();
        if (mappings.size() == 0) {
            mappings = new HashMap<String, Set<ReqMethod>>();
            Set<ReqMethod> methods = Collections.emptySet();
            mappings.put("", methods);
        }
        for (String mappingPath : mappings.keySet()) {
            mapping = new MappingImpl(mappingPath, MatchMode.EQUALS);
            MappingNode actionNode = controllerNode.getChild(mapping);
            WebResource resource = null;
            if (actionNode == null) {
                resource = new WebResource(controllerNode.getResource(), mapping.getPath());
                mapping.setResource(resource);
                actionNode = new MappingNode(mapping, controllerNode);
            } else {
                resource = actionNode.getResource();
            }
            Set<ReqMethod> methods = mappings.get(mappingPath);
            if (methods.size() > 0) {
                Engine actionEngine = new ActionEngine(module, controller.getControllerClass(),
                        controller.getControllerObject(), action.getMethod());
                for (ReqMethod reqMethod : methods) {
                    resource.addEngine(reqMethod, actionEngine);
                }
            }
        }

    }
}
