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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class TreeBuilder {

    protected static final Log logger = LogFactory.getLog(TreeBuilder.class);

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
        Mapping moduleMapping = new MappingImpl(module.getMappingPath(), MatchMode.STARTS_WITH);
        EngineGroup moduleEngines = new EngineGroupImpl();
        MappingNode moduleNode = rootNode.getChild(moduleMapping);
        if (moduleNode == null) {
            moduleNode = new MappingNode(moduleMapping, rootNode, rootNode.getEngineGroups()[0]);
        } else {
            moduleMapping = moduleNode.getMapping();
            if (logger.isDebugEnabled()) {
                logger.debug("found multi module with same path: " + moduleMapping.getPath()
                        + " vs " + moduleNode.getMapping().getPath());
            }
        }
        moduleNode.addEngineGroup(moduleEngines);
        moduleEngines.addEngine(ReqMethod.ALL, new ModuleEngine(module));

        // controllers
        List<ControllerRef> controllers = module.getControllers();
        for (ControllerRef controller : controllers) {
            controller(module, moduleNode, moduleEngines, controller);
        }

        // defaults
        MappingImpl defMapping = new MappingImpl("", MatchMode.STARTS_WITH, null);
        MappingNode defNode = moduleNode.getChild(defMapping);
        if (defNode == null) {
            ReqMethod reqMethod = ReqMethod.GET; // controller的，只对GET做default
            String[] candidates = getControllerCandidates(reqMethod);
            MappingNode defTargetNode = null;
            for (String candidate : candidates) {
                MappingImpl tempMapping = new MappingImpl(candidate, MatchMode.STARTS_WITH);
                defTargetNode = moduleNode.getChild(tempMapping);
                if (defTargetNode != null) {
                    defNode = new MappingNode(defMapping, moduleNode, moduleEngines);
                    defNode.setEngineGroups(defTargetNode.getEngineGroups());
                    List<ReqMethod> filters = new ArrayList<ReqMethod>(1);
                    filters.add(reqMethod);
                    defTargetNode.copyChildrenTo(defNode, filters);
                    break;
                }
            }
        }
    }

    private void controller(Module module, MappingNode moduleNode, EngineGroup moduleResource,
            ControllerRef controller) {
        for (String mappingPath : controller.getMappingPaths()) {
            Mapping controllerMapping = new MappingImpl(mappingPath, MatchMode.STARTS_WITH);
            MappingNode controllerNode = moduleNode.getChild(controllerMapping);
            EngineGroup controllerEngines = new EngineGroupImpl();
            if (controllerNode == null) {
                controllerNode = new MappingNode(controllerMapping, moduleNode, moduleResource);
            } else {
                controllerMapping = controllerNode.getMapping();
                if (logger.isDebugEnabled()) {
                    logger.debug("found multi controllers with same path: " + mappingPath + " vs "
                            + controllerNode.getMapping().getPath());
                }
            }
            controllerNode.addEngineGroup(controllerEngines);
            Engine controllerEngine = new ControllerEngine(module, mappingPath, controller);
            controllerEngines.addEngine(ReqMethod.ALL, controllerEngine);

            // actions
            MethodRef[] actions = controller.getActions();
            for (MethodRef action : actions) {
                action(module, controller, action, controllerNode, controllerEngines);
            }

            // defaults
            EngineGroup defResource = new EngineGroupImpl();
            MappingImpl defMapping = new MappingImpl("", MatchMode.EQUALS);
            MappingNode defTargetNode = controllerNode.getChild(defMapping);
            MappingNode defNode = defTargetNode;
            if (defTargetNode == null) {
                for (ReqMethod reqMethod : ReqMethod.ALL.parse()) {
                    String[] candidates = getActionCandidates(reqMethod);
                    for (String candidate : candidates) {
                        MappingImpl tempMapping = new MappingImpl(candidate, MatchMode.EQUALS, null);
                        MappingNode tempNode = controllerNode.getChild(tempMapping);
                        if (tempNode != null) {
                            Engine[] defActionEngines = tempNode.getEngineGroups()[0]
                                    .getEngines(reqMethod);
                            for (Engine engine : defActionEngines) {
                                defResource.addEngine(reqMethod, engine);
                            }
                            break;
                        }
                    }
                }
            }
            if (defNode == null && defResource.getAllowedMethods().size() > 0) {
                defNode = new MappingNode(defMapping, controllerNode, controllerEngines);
                defNode.addEngineGroup(defResource);
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
            return new String[] { "/get", "/index", };
        }
        return new String[] { "/" + reqMethod.toString().toLowerCase() };
    }

    private void action(Module module, ControllerRef controller, MethodRef action,
            MappingNode controllerNode, EngineGroup controllerEngines) {
        Mapping mapping;
        Map<String, Set<ReqMethod>> mappings = action.getMappings();
        if (mappings.size() == 0) {
            mappings = new HashMap<String, Set<ReqMethod>>();
            Set<ReqMethod> methods = Collections.emptySet();
            mappings.put("", methods);
        }
        for (String mappingPath : mappings.keySet()) {
            mapping = new MappingImpl(mappingPath, MatchMode.EQUALS);
            MappingNode actionNode = controllerNode.getChild(mapping);
            EngineGroup actionEngines = new EngineGroupImpl();
            if (actionNode == null) {
                actionNode = new MappingNode(mapping, controllerNode, controllerEngines);
                actionNode.addEngineGroup(actionEngines);
            } else {
                mapping = actionNode.getMapping();
                actionEngines = actionNode.getEngineGroups()[0];
            }
            Set<ReqMethod> methods = mappings.get(mappingPath);
            if (methods.size() > 0) {
                Engine actionEngine = new ActionEngine(module, controller.getControllerClass(),
                        controller.getControllerObject(), action.getMethod());
                for (ReqMethod reqMethod : methods) {
                    actionEngines.addEngine(reqMethod, actionEngine);
                }
            }
        }

    }
}
