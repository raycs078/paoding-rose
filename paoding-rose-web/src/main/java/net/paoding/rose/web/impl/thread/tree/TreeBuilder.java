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
package net.paoding.rose.web.impl.thread.tree;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.paoding.rose.scanner.ModuleResource;
import net.paoding.rose.scanner.RoseModuleInfos;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.mapping.MappingImpl;
import net.paoding.rose.web.impl.mapping.MappingNode;
import net.paoding.rose.web.impl.mapping.MatchMode;
import net.paoding.rose.web.impl.module.ControllerRef;
import net.paoding.rose.web.impl.module.MethodRef;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.module.ModulesBuilder;
import net.paoding.rose.web.impl.resource.WebResource;
import net.paoding.rose.web.impl.thread.ActionEngine;
import net.paoding.rose.web.impl.thread.ControllerEngine;
import net.paoding.rose.web.impl.thread.Engine;
import net.paoding.rose.web.impl.thread.ModuleEngine;
import net.paoding.rose.web.impl.thread.WebEngine;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */

// FIXME: 不要使用ModuleBuilder,而直接使用new ModuleImpl()实现?
public class TreeBuilder {

    private static void println(MappingNode node, PrintWriter sb) {
        if (node.leftMostChild == null) {
            sb.append("<resource path=\"").append(node.getResource().getAbsolutePath()).append(
                    "\">");
        }
        MappingNode si = node.leftMostChild;
        if (si != null) {
            println(si, sb);
            while ((si = si.sibling) != null) {
                println(si, sb);
            }
        } else {
            for (ReqMethod method : node.getResource().getAllowedMethods()) {
                Engine engine = node.getResource().getEngine(method);
                ActionEngine action = (ActionEngine) engine;
                Method m = action.getMethod();
                Class<?> cc = action.getControllerClass();
                String rm = method.toString();
                sb.append("<allowed ");
                sb.append(rm + "=\"" + cc.getSimpleName() + " ." + m.getName() + "\" ");
                sb.append("package=\"" + m.getDeclaringClass().getPackage().getName() + "\" ");
                sb.append(" />");
            }
        }
        if (node.leftMostChild == null) {
            sb.append("</resource>");
        }
    }

    public static void main(String[] args) throws Exception {
        WebEngine rootEngine = new WebEngine(null);
        WebResource rootResource = new WebResource(null, "");
        rootResource.setEndResource(false);
        rootResource.addEngine(ReqMethod.ALL, rootEngine);
        Mapping rootMapping = new MappingImpl("", MatchMode.PATH_STARTS_WITH);
        MappingNode head = new MappingNode(rootMapping, rootResource);

        // 自动扫描识别web层对象，纳入Rose管理
        List<ModuleResource> moduleInfoList = new RoseModuleInfos().findModuleInfos();

        System.out.println("=============1=================");
        List<Module> modules = new ModulesBuilder().build(null, moduleInfoList);
        System.out.println("=============1.1=================");
        new TreeBuilder().create(head, modules);

        System.out.println("=============2=================");
        File f = new File("E:/my_documents/xml.xml");
        PrintWriter out = new PrintWriter(f, "UTF-8");
        out.append("<rose-web>");
        println(head, out);
        out.append("</rose-web>");
        out.flush();
        System.out.println("=============3=================");
    }

    /*
     * 构造一个树，树的结点是资源，每个结点都能回答是否匹配一个字符串，每个匹配的节点都知道如何执行对该资源的操作.
     * 构造树的过程：
     *   识别组件==>求得他的资源定义==>判断是否已经创建了==>未创建的创建一个树结点==>已创建的找出这个结点
     *   ==>在这个资源增加相应的操作以及逻辑
     *   ==>把整个树都构造完成之后对每个结点的儿子进行重排序
     */
    public void create(MappingNode tree, List<Module> modules) {
        root(tree, modules);
        tree.sorts();
    }

    private void root(MappingNode rootNode, List<Module> modules) {
        for (Module module : modules) {
            module(rootNode, module);
        }
    }

    private void module(final MappingNode rootNode, Module module) {
        WebResource moduleResource;
        Mapping mapping = new MappingImpl(module.getMappingPath(), MatchMode.PATH_STARTS_WITH);
        MappingNode moduleNode = rootNode.getChild(mapping);
        if (moduleNode == null) {
            moduleResource = new WebResource(rootNode.getResource(), mapping.getPath());
            moduleNode = new MappingNode(mapping, moduleResource, rootNode);
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
        Mapping defMapping = new MappingImpl("", MatchMode.PATH_STARTS_WITH);
        MappingNode defNode = moduleNode.getChild(defMapping);
        List<ReqMethod> allowed = null;
        if (defNode == null) {
            allowed = Collections.emptyList();
            defResource = new WebResource(moduleResource, "");
        } else {
            defResource = defNode.getResource();
            allowed = defResource.getAllowedMethods();
        }
        MappingNode defTargetNode = null;
        ReqMethod reqMethod = ReqMethod.GET; // controller的，只对GET做default
        if (!allowed.contains(reqMethod)) {
            String[] candidates = getControllerCandidates(reqMethod);
            for (String candidate : candidates) {
                WebResource tempResource = null;
                Mapping tempMapping = new MappingImpl(candidate, MatchMode.PATH_STARTS_WITH);
                MappingNode tempNode = moduleNode.getChild(tempMapping);
                if (tempNode != null) {
                    defTargetNode = tempNode;
                    tempResource = tempNode.getResource();
                    defResource.addEngine(reqMethod, tempResource.getEngine(reqMethod));
                    break;
                }
            }
        }
        if (defNode == null && defResource.getAllowedMethods().size() > 0) {
            defNode = new MappingNode(defMapping, defResource, moduleNode);
            List<ReqMethod> filters = new ArrayList<ReqMethod>(1);
            filters.add(reqMethod);
            defTargetNode.copyTo(defNode, filters);
        }
    }

    private void controller(Module module, MappingNode moduleNode, ControllerRef controller) {
        for (String mappingPath : controller.getMappingPaths()) {
            Mapping controllerMapping = new MappingImpl(mappingPath, MatchMode.PATH_STARTS_WITH);
            MappingNode controllerNode = moduleNode.getChild(controllerMapping);
            WebResource controllerResource;
            if (controllerNode == null) {
                controllerResource = new WebResource(moduleNode.getResource(), controllerMapping
                        .getPath());
                controllerNode = new MappingNode(controllerMapping, controllerResource, moduleNode);
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
            Mapping defMapping = new MappingImpl("", MatchMode.PATH_EQUALS);
            MappingNode defNode = controllerNode.getChild(defMapping);
            List<ReqMethod> allowed = null;
            if (defNode == null) {
                allowed = Collections.emptyList();
                defResource = new WebResource(controllerResource, "");
            } else {
                defResource = defNode.getResource();
                allowed = defResource.getAllowedMethods();
            }
            for (ReqMethod reqMethod : ReqMethod.ALL.parse()) {
                if (!allowed.contains(reqMethod)) {
                    String[] candidates = getActionCandidates(reqMethod);
                    for (String candidate : candidates) {
                        Mapping tempMapping = new MappingImpl(candidate, MatchMode.PATH_EQUALS);
                        MappingNode tempNode = controllerNode.getChild(tempMapping);
                        if (tempNode != null) {
                            WebResource tempResource = tempNode.getResource();
                            defResource.addEngine(reqMethod, tempResource.getEngine(reqMethod));
                            break;
                        }
                    }
                }
            }
            if (defNode == null && defResource.getAllowedMethods().size() > 0) {
                new MappingNode(defMapping, defResource, controllerNode);
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
        Mapping mapping;
        Map<String, Set<ReqMethod>> mappings = action.getMappings();
        if (mappings.size() == 0) {
            mappings = new HashMap<String, Set<ReqMethod>>();
            Set<ReqMethod> methods = Collections.emptySet();
            mappings.put("", methods);
        }
        for (String mappingPath : mappings.keySet()) {
            mapping = new MappingImpl(mappingPath, MatchMode.PATH_EQUALS);
            MappingNode actionNode = controllerNode.getChild(mapping);
            WebResource resource = null;
            if (actionNode == null) {
                resource = new WebResource(controllerNode.getResource(), mapping.getPath());
                actionNode = new MappingNode(mapping, resource, controllerNode);
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

    //    public void createModuleMappingNodes2(MappingNode rootNode, List<Module> modules) {
    //        List<MappingImpl> mappings = new ArrayList<MappingImpl>(modules.size());
    //        for (Module module : modules) {
    //            if (module == null) {
    //                continue;
    //            }
    //            String path = module.getMappingPath();
    //            MappingImpl mapping = new MappingImpl(path, PATH_STARTS_WITH);
    //            mapping.setTarget(module);
    //            mappings.add(mapping);
    //        }
    //        //
    //        Collections.sort(mappings);
    //        //
    //        MappingNode lastModuleNode = null;
    //        for (MappingImpl mapping : mappings) {
    //            Module module = (Module) mapping.getTarget();
    //            // 
    //            ModuleEngine moduleEngine = new ModuleEngine(module);
    //            WebResource moduleResource = new WebResource(mapping.getPath());
    //            moduleResource.addEngine(ReqMethod.ALL, moduleEngine);
    //            MappingNode moduleNode = new MappingNode(mapping, moduleResource, rootNode);
    //            if (rootNode.leftMostChild == null) {
    //                rootNode.leftMostChild = moduleNode;
    //            } else {
    //                lastModuleNode.sibling = moduleNode;
    //            }
    //            lastModuleNode = moduleNode;
    //            // 
    //            createControllerMappingNodes(moduleNode, module);
    //        }
    //    }

    //    private void createControllerMappingNodes(MappingNode parentNode, Module module) {
    //        List<ControllerRef> controllers = module.getControllers();
    //        List<MappingImpl> mappings = new ArrayList<MappingImpl>(controllers.size());
    //        for (ControllerRef controller : controllers) {
    //            if (controller == null) {
    //                continue;
    //            }
    //            String[] mappingPaths = controller.getMappingPaths();
    //            for (String mappingPath : mappingPaths) {
    //                MappingImpl mapping = new MappingImpl(mappingPath, PATH_STARTS_WITH);
    //                mapping.setTarget(controller);
    //                mappings.add(mapping);
    //            }
    //        }
    //        //
    //        Collections.sort(mappings);
    //        //
    //        MappingNode lastControllerNode = null;
    //        for (MappingImpl mapping : mappings) {
    //            ControllerRef controller = (ControllerRef) mapping.getTarget();
    //            //
    //            ControllerEngine controllerEngine = new ControllerEngine(module, mapping.getPath(),
    //                    controller);
    //            WebResource controllerResource = new WebResource(mapping.getPath());
    //            for (ReqMethod method : controller.getReqMethods()) {
    //                controllerResource.addEngine(method, controllerEngine);
    //            }
    //            MappingNode controllerNode = new MappingNode(mapping, controllerResource, parentNode);
    //            if (parentNode.leftMostChild == null) {
    //                parentNode.leftMostChild = controllerNode;
    //            } else {
    //                lastControllerNode.sibling = controllerNode;
    //            }
    //            lastControllerNode = controllerNode;
    //            //
    //            createActionMappingNodes(controllerNode, module, controller);
    //        }
    //    }
    //
    //    private void createActionMappingNodes(MappingNode controllerNode, Module module,
    //            ControllerRef controller) {
    //        WebResource restResource = new WebResource("");
    //
    //        ArrayList<MappingImpl> actionMappings = new ArrayList<MappingImpl>();
    //
    //        Class<?> controllerClass = controller.getControllerClass();
    //        Class<?> clz = controllerClass;
    //        //
    //        List<Method> pastMethods = new LinkedList<Method>();
    //        while (true) {
    //            Method[] declaredMethods = clz.getDeclaredMethods();
    //            for (Method method : declaredMethods) {
    //                if (isMethodIgnored(pastMethods, method, controllerClass)) {
    //                    continue;
    //                }
    //                ActionEngine actionEngine = new ActionEngine(module, controllerClass, controller,
    //                        method);
    //
    //                int collected = collectsRESTAnnocations(restResource, method, actionEngine);
    //
    //                String methodPath = "/" + method.getName();
    //
    //                ReqMapping reqMappingAnnotation = method.getAnnotation(ReqMapping.class);
    //                ReqMethod[] methods = null;
    //                String[] mappingPaths = null;
    //                if (reqMappingAnnotation != null) {
    //                    methods = reqMappingAnnotation.methods();
    //                    mappingPaths = reqMappingAnnotation.path();
    //                    // 如果mappingPaths.length==0，表示没有任何path可以映射到这个action了
    //                    for (int i = 0; i < mappingPaths.length; i++) {
    //                        if (ReqMapping.DEFAULT_PATH.equals(mappingPaths[i])) {
    //                            mappingPaths[i] = methodPath;
    //                        } else if (mappingPaths[i].length() > 0 && mappingPaths[i].charAt(0) != '/') {
    //                            mappingPaths[i] = '/' + mappingPaths[i];
    //                        } else if (mappingPaths[i].equals("/")) {
    //                            mappingPaths[i] = "";
    //                        }
    //                    }
    //                } else if (collected == 0) {
    //                    methods = new ReqMethod[] { ReqMethod.ALL };
    //                    mappingPaths = new String[] { methodPath };
    //                }
    //                if (!ArrayUtils.isEmpty(mappingPaths) && !ArrayUtils.isEmpty(methods)) {
    //                    for (int i = 0; i < mappingPaths.length; i++) {
    //                        WebResource resource = new WebResource(mappingPaths[i]);
    //                        for (ReqMethod reqMethod : methods) {
    //                            resource.addEngine(reqMethod, actionEngine);
    //                        }
    //                        MappingImpl mapping = new MappingImpl(mappingPaths[i],
    //                                MatchMode.PATH_EQUALS);
    //                        mapping.setTarget(actionEngine);
    //                        actionMappings.add(mapping);
    //                    }
    //                }
    //            }
    //            for (int i = 0; i < declaredMethods.length; i++) {
    //                pastMethods.add(declaredMethods[i]);
    //            }
    //            clz = clz.getSuperclass();
    //            if (clz == null || clz.getAnnotation(AsSuperController.class) == null) {
    //                break;
    //            }
    //        }
    //        boolean showIdExists = false;
    //        for (int i = 1; i < actionMappings.size(); i++) {
    //            Mapping mapping = actionMappings.get(i);
    //            MatchResult mr;
    //            if (!showIdExists && (mr = mapping.match("/198107")) != null
    //                    && mr.isMethodAllowed(ReqMethod.GET)) {
    //                showIdExists = true;
    //            }
    //        }
    //        // /user/123456自动映射到UserController的show()方法
    //        if (!showIdExists) {
    //            for (int i = 1; i < actionMappings.size(); i++) {
    //                MappingImpl pathMapping = actionMappings.get(i);
    //                ActionEngine engine = (ActionEngine) pathMapping.getTarget();
    //                Method method = engine.getMethod();
    //                if ("show".equals(method.getName())
    //                        && method.getAnnotation(ReqMapping.class) == null) {
    //                    String[] paths = new String[] { "/show/{id:[0-9]+}", "/{id:[0-9]+}" };
    //                    for (String path : paths) {
    //                        WebResource resourceAdded = new WebResource(path);
    //                        resourceAdded.addEngine(ReqMethod.GET, engine);
    //                        MappingImpl mappingAdded = new MappingImpl(path, MatchMode.PATH_EQUALS);
    //                        actionMappings.add(mappingAdded);
    //                    }
    //                    break;
    //                }
    //            }
    //        }
    //        Collections.sort(actionMappings);
    //
    //        // URI没有提供action的处理(REST)
    //        REST rest = controllerClass.getAnnotation(REST.class);
    //        Map<ReqMethod, String[]> restSetting = new HashMap<ReqMethod, String[]>();
    //        if (rest != null) {
    //            if (logger.isDebugEnabled()) {
    //                logger.debug(controllerClass.getName() + "@REST is present; use it;");
    //            }
    //            Method[] restMethods = rest.getClass().getMethods();
    //            for (Method restMethod : restMethods) {
    //                if (restMethod.getParameterTypes().length != 0) {
    //                    continue;
    //                }
    //                if (restMethod.getReturnType() != String[].class) {
    //                    continue;
    //                }
    //                String[] candidates;
    //                try {
    //                    candidates = (String[]) restMethod.invoke(rest);
    //                    for (int i = 0; i < candidates.length; i++) {
    //                        if (candidates[i].length() > 0 && candidates[i].charAt(0) != '/') {
    //                            candidates[i] = '/' + candidates[i];
    //                        }
    //                    }
    //                    if (logger.isDebugEnabled()) {
    //                        logger.debug(controllerClass.getName() + "@REST." + restMethod.getName()
    //                                + "=" + Arrays.toString(candidates));
    //                    }
    //                    restSetting.put(ReqMethod.parse(restMethod.getName()), candidates);
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                }
    //            }
    //        } else {
    //            if (logger.isDebugEnabled()) {
    //                logger.debug(controllerClass.getName() + "@REST is not present; use default");
    //            }
    //            restSetting.put(ReqMethod.GET, new String[] { "/index", "/get", "/render" });
    //            restSetting
    //                    .put(ReqMethod.POST, new String[] { "/post", "/add", "/create", "/update", });
    //            restSetting.put(ReqMethod.PUT, new String[] { "/put", "/update", });
    //            restSetting.put(ReqMethod.DELETE, new String[] { "/delete", });
    //            restSetting.put(ReqMethod.OPTIONS, new String[] { "/options", });
    //            restSetting.put(ReqMethod.TRACE, new String[] { "/trace", });
    //            restSetting.put(ReqMethod.HEAD, new String[] { "/head", });
    //        }
    //        //
    //        for (Map.Entry<ReqMethod, String[]> entry : restSetting.entrySet()) {
    //            ReqMethod reqMethod = entry.getKey();
    //            MatchResult matchResult = null;
    //            for (Mapping actionMapping : actionMappings) {
    //                String[] candidates = entry.getValue();
    //                for (String candidate : candidates) {
    //                    matchResult = actionMapping.match(candidate);
    //                    if (matchResult != null && matchResult.isMethodAllowed(reqMethod)) {
    //                        if (matchResult.getMatchedString().length() > 0) {
    //                            MappingImpl mapping = new MappingImpl("", MatchMode.PATH_EQUALS);
    //                            actionMappings.add(mapping);
    //                            if (logger.isDebugEnabled()) {
    //                                logger.debug(controllerClass.getName() + "add @REST Mapping: "
    //                                        + entry.getKey() + "=" + Arrays.toString(candidates));
    //                            }
    //                        }
    //                        break;
    //                    }
    //                }
    //                if (matchResult != null && matchResult.isMethodAllowed(reqMethod)) {
    //                    break;
    //                }
    //            }
    //        }
    //        //
    //    }
    //
    //    private boolean isMethodIgnored(List<Method> pastMethods, Method method,
    //            Class<?> controllerClass) {
    //        // public, not static, not abstract, @Ignored
    //        if (!Modifier.isPublic(method.getModifiers()) || Modifier.isAbstract(method.getModifiers())
    //                || Modifier.isStatic(method.getModifiers())
    //                || method.isAnnotationPresent(Ignored.class)) {
    //            if (logger.isDebugEnabled()) {
    //                logger.debug("ignores methods of controller " + controllerClass.getName() + "."
    //                        + method.getName() + "  [@ignored?not public?abstract?static?]");
    //            }
    //            return true;
    //        }
    //        if (ignoresCommonMethod(method)) {
    //            if (logger.isDebugEnabled()) {
    //                logger.debug("ignores common methods of controller " + controllerClass.getName()
    //                        + "." + method.getName());
    //            }
    //            return true;
    //        }
    //        // 刚才在继承类(子类)已经声明的方法，不必重复处理了
    //        for (Method past : pastMethods) {
    //            if (past.getName().equals(method.getName())) {
    //                if (Arrays.equals(past.getParameterTypes(), method.getParameterTypes())) {
    //                    return true;
    //                }
    //            }
    //        }
    //        return false;
    //    }
    //
    //    private boolean ignoresCommonMethod(Method method) {
    //        String name = method.getName();
    //        if (name.equals("toString") || name.equals("hashCode") || name.equals("equals")
    //                || name.equals("wait") || name.equals("getClass") || name.equals("clone")
    //                || name.equals("notify") || name.equals("notifyAll") || name.equals("finalize")) {
    //            if (null == method.getAnnotation(ReqMapping.class)
    //                    && null == method.getAnnotation(Get.class)) {
    //                return true;
    //            }
    //        }
    //        if (name.startsWith("get") && name.length() > 3
    //                && Character.isUpperCase(name.charAt("get".length()))
    //                && method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
    //            if (null == method.getAnnotation(ReqMapping.class)
    //                    && null == method.getAnnotation(Get.class)) {
    //                return true;
    //            }
    //        }
    //        if (name.startsWith("is")
    //                && name.length() > 3
    //                && Character.isUpperCase(name.charAt("is".length()))
    //                && method.getParameterTypes().length == 0
    //                && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
    //            if (null == method.getAnnotation(ReqMapping.class)
    //                    && null == method.getAnnotation(Get.class)) {
    //                return true;
    //            }
    //        }
    //        if (name.startsWith("set") && name.length() > 3
    //                && Character.isUpperCase(name.charAt("set".length()))
    //                && method.getParameterTypes().length == 1 && method.getReturnType() == void.class) {
    //            if (null == method.getAnnotation(ReqMapping.class)
    //                    && null == method.getAnnotation(Post.class)) {
    //                return true;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    /**
    //     * 识别定义在method上的rest相关的标签
    //     * 
    //     * @param method
    //     * @param actionEngine
    //     */
    //    private int collectsRESTAnnocations(WebResource resource, Method method, Engine engine) {
    //        int collected = 0;
    //        Annotation[] annotations = method.getAnnotations();
    //        for (Annotation annotation : annotations) {
    //            if (annotation instanceof Delete) {
    //                resource.addEngine(ReqMethod.DELETE, engine);
    //            } else if (annotation instanceof Get) {
    //                resource.addEngine(ReqMethod.GET, engine);
    //            } else if (annotation instanceof Head) {
    //                resource.addEngine(ReqMethod.HEAD, engine);
    //            } else if (annotation instanceof Options) {
    //                resource.addEngine(ReqMethod.OPTIONS, engine);
    //            } else if (annotation instanceof Post) {
    //                resource.addEngine(ReqMethod.POST, engine);
    //            } else if (annotation instanceof Put) {
    //                resource.addEngine(ReqMethod.PUT, engine);
    //            } else if (annotation instanceof Trace) {
    //                resource.addEngine(ReqMethod.TRACE, engine);
    //            } else {
    //                collected--;
    //            }
    //            collected++;
    //        }
    //        return collected;
    //    }
    //    private List<Mapping<ActionEngine>> createActionMappings(Module module,
    //            final Class<?> controllerClass, Object controller) {
    //        if (logger.isDebugEnabled()) {
    //            logger.debug("creating action mappings for controller: " + controllerClass.getName());
    //        }
    //        ArrayList<Mapping<ActionEngine>> actionMappings = new ArrayList<Mapping<ActionEngine>>();
    //        Class<?> clz = controllerClass;
    //        //
    //        List<Method> pastMethods = new LinkedList<Method>();
    //        while (true) {
    //            Method[] declaredMethods = clz.getDeclaredMethods();
    //            for (Method method : declaredMethods) {
    //                // public, not static, not abstract, @Ignored
    //                if (!Modifier.isPublic(method.getModifiers())
    //                        || Modifier.isAbstract(method.getModifiers())
    //                        || Modifier.isStatic(method.getModifiers())
    //                        || method.isAnnotationPresent(Ignored.class)) {
    //                    if (logger.isDebugEnabled()) {
    //                        logger.debug("ignores methods of controller " + controllerClass.getName()
    //                                + "." + method.getName()
    //                                + "  [@ignored?not public?abstract?static?]");
    //                    }
    //                    continue;
    //                }
    //                if (ignoresCommonMethod(method)) {
    //                    if (logger.isDebugEnabled()) {
    //                        logger.debug("ignores common methods of controller "
    //                                + controllerClass.getName() + "." + method.getName());
    //                    }
    //                    continue;
    //                }
    //                // 刚才在继承类(子类)已经声明的方法，不必重复处理了
    //                boolean beanAddBySubClass = false;
    //                for (Method past : pastMethods) {
    //                    if (past.getName().equals(method.getName())) {
    //                        if (Arrays.equals(past.getParameterTypes(), method.getParameterTypes())) {
    //                            beanAddBySubClass = true;
    //                            break;
    //                        }
    //                    }
    //                }
    //                if (beanAddBySubClass) {
    //                    continue;
    //                }
    //                String methodPath = "/" + method.getName();
    //                ActionEngine actionEngine = new ActionEngine(module, controllerClass, controller,
    //                        method);
    //                // 解析@ReqMapping
    //                int beginSize = actionMappings.size();
    //                collectRestMappingsByMethodAnnotation(actionMappings, method, actionEngine);
    //                int restCount = actionMappings.size() - beginSize;
    //                ReqMapping reqMappingAnnotation = method.getAnnotation(ReqMapping.class);
    //                ReqMethod[] methods = null;
    //                String[] mappingPaths = null;
    //                if (reqMappingAnnotation != null) {
    //                    methods = reqMappingAnnotation.methods();
    //                    mappingPaths = reqMappingAnnotation.path();
    //                    // 如果mappingPaths.length==0，表示没有任何path可以映射到这个action了
    //                    for (int i = 0; i < mappingPaths.length; i++) {
    //                        if (ReqMapping.DEFAULT_PATH.equals(mappingPaths[i])) {
    //                            mappingPaths[i] = methodPath;
    //                        } else if (mappingPaths[i].length() > 0 && mappingPaths[i].charAt(0) != '/') {
    //                            mappingPaths[i] = '/' + mappingPaths[i];
    //                        } else if (mappingPaths[i].equals("/")) {
    //                            mappingPaths[i] = "";
    //                        }
    //                    }
    //                } else if (restCount == 0) {
    //                    methods = new ReqMethod[] { ReqMethod.ALL };
    //                    mappingPaths = new String[] { methodPath };
    //                }
    //                if (!ArrayUtils.isEmpty(mappingPaths) && !ArrayUtils.isEmpty(methods)) {
    //                    for (int i = 0; i < mappingPaths.length; i++) {
    //                        actionMappings.add(new MappingImpl<ActionEngine>(mappingPaths[i],
    //                                MatchMode.PATH_EQUALS, methods, actionEngine));
    //                    }
    //                }
    //            }
    //            for (int i = 0; i < declaredMethods.length; i++) {
    //                pastMethods.add(declaredMethods[i]);
    //            }
    //            clz = clz.getSuperclass();
    //            if (clz == null || clz.getAnnotation(AsSuperController.class) == null) {
    //                break;
    //            }
    //        }
    //        boolean showIdExists = false;
    //        for (int i = 1; i < actionMappings.size(); i++) {
    //            Mapping<ActionEngine> mapping = actionMappings.get(i);
    //            MatchResult mr;
    //            if (!showIdExists && (mr = mapping.match("/198107", "GET")) != null
    //                    && mr.isMethodAllowed()) {
    //                showIdExists = true;
    //            }
    //        }
    //        // /user/123456自动映射到UserController的show()方法
    //        if (!showIdExists) {
    //            for (int i = 1; i < actionMappings.size(); i++) {
    //                Mapping<ActionEngine> pathMapping = actionMappings.get(i);
    //                Method method = pathMapping.getTarget().getMethod();
    //                if ("show".equals(method.getName())
    //                        && method.getAnnotation(ReqMapping.class) == null) {
    //                    actionMappings.add(new MappingImpl<ActionEngine>("/show/{id:[0-9]+}",
    //                            MatchMode.PATH_EQUALS, pathMapping.getMethods(), pathMapping
    //                                    .getTarget()));
    //                    actionMappings.add(new MappingImpl<ActionEngine>("/{id:[0-9]+}",
    //                            MatchMode.PATH_EQUALS, pathMapping.getMethods(), pathMapping
    //                                    .getTarget()));
    //                    break;
    //                }
    //            }
    //        }
    //        Collections.sort(actionMappings);
    //
    //        // URI没有提供action的处理(REST)
    //        REST rest = controllerClass.getAnnotation(REST.class);
    //        Map<String, String[]> restSetting = new HashMap<String, String[]>();
    //        if (rest != null) {
    //            if (logger.isDebugEnabled()) {
    //                logger.debug(controllerClass.getName() + "@REST is present; use it;");
    //            }
    //            Method[] restMethods = rest.getClass().getMethods();
    //            for (Method restMethod : restMethods) {
    //                if (restMethod.getParameterTypes().length != 0) {
    //                    continue;
    //                }
    //                if (restMethod.getReturnType() != String[].class) {
    //                    continue;
    //                }
    //                String[] candidates;
    //                try {
    //                    candidates = (String[]) restMethod.invoke(rest);
    //                    for (int i = 0; i < candidates.length; i++) {
    //                        if (candidates[i].length() > 0 && candidates[i].charAt(0) != '/') {
    //                            candidates[i] = '/' + candidates[i];
    //                        }
    //                    }
    //                    if (logger.isDebugEnabled()) {
    //                        logger.debug(controllerClass.getName() + "@REST." + restMethod.getName()
    //                                + "=" + Arrays.toString(candidates));
    //                    }
    //                    restSetting.put(restMethod.getName().toUpperCase(), candidates);
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                }
    //            }
    //        } else {
    //            if (logger.isDebugEnabled()) {
    //                logger.debug(controllerClass.getName() + "@REST is not present; use default");
    //            }
    //            restSetting.put("GET", new String[] { "/index", "/get", "/render" });
    //            restSetting.put("POST", new String[] { "/post", "/add", "/create", "/update", });
    //            restSetting.put("PUT", new String[] { "/put", "/update", });
    //            restSetting.put("DELETE", new String[] { "/delete", });
    //            restSetting.put("OPTIONS", new String[] { "/options", });
    //            restSetting.put("TRACE", new String[] { "/trace", });
    //            restSetting.put("HEAD", new String[] { "/head", });
    //        }
    //        //
    //        for (Map.Entry<String, String[]> entry : restSetting.entrySet()) {
    //            MatchResult matchResult = null;
    //            for (Mapping<ActionEngine> actionMapping : actionMappings) {
    //                String[] candidates = entry.getValue();
    //                for (String candidate : candidates) {
    //                    matchResult = actionMapping.match(candidate, entry.getKey());
    //                    if (matchResult != null && matchResult.isMethodAllowed()) {
    //                        if (matchResult.getMatchedString().length() > 0) {
    //                            actionMappings.add(new MappingImpl<ActionEngine>("",
    //                                    MatchMode.PATH_EQUALS, new ReqMethod[] { ReqMethod.parse(entry
    //                                            .getKey()) }, actionMapping.getTarget()));
    //                            if (logger.isDebugEnabled()) {
    //                                logger.debug(controllerClass.getName() + "add @REST Mapping: "
    //                                        + entry.getKey() + "=" + Arrays.toString(candidates));
    //                            }
    //                        }
    //                        break;
    //                    }
    //                }
    //                if (matchResult != null && matchResult.isMethodAllowed()) {
    //                    break;
    //                }
    //            }
    //        }
    //        //
    //        HashMap<String, Set<ReqMethod>> resouceMethodsList = new HashMap<String, Set<ReqMethod>>();
    //        for (Mapping<ActionEngine> actionMapping : actionMappings) {
    //            Set<ReqMethod> resouceMethods = resouceMethodsList.get(actionMapping.getPath());
    //            if (resouceMethods == null) {
    //                resouceMethods = new HashSet<ReqMethod>(8);
    //                resouceMethodsList.put(actionMapping.getPath(), resouceMethods);
    //            }
    //            for (ReqMethod reqMethod : actionMapping.getMethods()) {
    //                if (reqMethod.equals(ReqMethod.ALL)) {
    //                    ReqMethod[] terms = new ReqMethod[] { ReqMethod.GET, ReqMethod.HEAD,
    //                            ReqMethod.POST, ReqMethod.PUT, ReqMethod.DELETE, ReqMethod.OPTIONS,
    //                            ReqMethod.TRACE };
    //                    for (ReqMethod term : terms) {
    //                        resouceMethods.add(term);
    //                    }
    //                } else {
    //                    resouceMethods.add(reqMethod);
    //                }
    //            }
    //            ((MappingImpl<?>) actionMapping).setResourceMethods(resouceMethods);
    //        }
    //        return actionMappings;
    //    }
    //
    //    protected List<Mapping<ControllerEngine>> createControllerMappings(Module module) {
    //        // module返回的mappings
    //        List<Mapping<Controller>> rawMappings = module.getControllers();
    //
    //        // module定义的default mapping. rawDefMapping可能因为存在path=""的控制器，而被重置为空
    //        Mapping<Controller> rawDefMapping = module.getDefaultController();
    //
    //        // 改target对象为engine后的mappings
    //        List<Mapping<ControllerEngine>> yesMappings = new ArrayList<Mapping<ControllerEngine>>(
    //                rawMappings.size());
    //
    //        // 仅当最后rawDefMapping仍未非空时有效; 而且只能放置到mappings的最后
    //        Mapping<ControllerEngine> defMapping = null;
    //        for (Mapping<Controller> rawMapping : rawMappings) {
    //            // 将rawMapping转为target是ControllerEngine的mapping，放入到rightMappings中
    //            String path = rawMapping.getPath();
    //            Controller controllerInfo = rawMapping.getTarget();
    //            ControllerEngine controllerEngine = new ControllerEngine(module, path, controllerInfo);
    //
    //            Mapping<ControllerEngine> rightMapping = changeTarget(rawMapping, controllerEngine);
    //            yesMappings.add(rightMapping);
    //
    //            // 如果已经有定义path=""的控制器，可省去构造下面的lastMapping对象
    //            // 另，这个代码位置放在这，其正确性建立在rawMappings已经是排序的基础上：path=""如果存在，则一定是在最后
    //            if (StringUtils.isEmpty(rawMapping.getPath())) {
    //                defMapping = null;
    //            } else if (rawMapping == rawDefMapping) {
    //                defMapping = rightMapping;
    //            }
    //        }
    //        if (defMapping != null) {
    //            Mapping<ControllerEngine> lastMapping = new MappingImpl<ControllerEngine>(//NL
    //                    "", PATH_STARTS_WITH, defMapping.getTarget());
    //            yesMappings.add(lastMapping);
    //        }
    //        return yesMappings;
    //    }
    //

    //    public MappingNode create(final List<Module> modules, InstructionExecutor instructionExecutor) {
    //        WebEngine roseEngine = new WebEngine(instructionExecutor);
    //        MappingNode head = new MappingNode(new MappingImpl<WebEngine>("",
    //                MatchMode.PATH_STARTS_WITH, roseEngine), null);
    //        createModuleMappingNodes(head, modules);
    //        File f = new File("E:/my_documents/xml.xml");
    //        PrintWriter out = null;
    //        try {
    //            out = new PrintWriter(f, "UTF-8");
    //        } catch (FileNotFoundException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        } catch (UnsupportedEncodingException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        }
    //        println(head, out);
    //        out.flush();
    //        return head;
    //    }
    //
    //    private void println(MappingNode node, StringBuilder sb) {
    //        sb.append("<node path=\"").append(node.mapping.getPath());
    //        sb.append("\" target=\"").append(node.mapping.getTarget()).append("\">");
    //        MappingNode si = node.leftMostChild;
    //        if (si != null) {
    //            println(si, sb);
    //            while ((si = si.sibling) != null) {
    //                println(si, sb);
    //            }
    //        }
    //        sb.append("</node>");
    //    }

    //    private static void println(MappingNode node, PrintWriter sb) {
    //        sb.append("<node path=\"");
    //        ReqMethod[] methods = node.mapping.getMethods();
    //        for (int i = 0; i < methods.length; i++) {
    //            if (i > 0) {
    //                sb.append("/");
    //            }
    //            sb.append(String.valueOf(methods[i]));
    //        }
    //        sb.append("  ").append(node.mapping.getPath());
    //        sb.append("\" target=\"").append(node.mapping.getTarget().toString()).append("\">");
    //        MappingNode si = node.leftMostChild;
    //        if (si != null) {
    //            println(si, sb);
    //            while ((si = si.sibling) != null) {
    //                println(si, sb);
    //            }
    //        }
    //        sb.append("</node>");
    //    }
    //
    //    public static void main(String[] args) throws Exception {
    //        List<ModuleInfo> moduleInfos = new RoseModuleInfos().findModuleInfos();
    //        List<Module> modules = new ModulesBuilder().build(null, moduleInfos);
    //        WebEngine roseEngine = new WebEngine(new InstructionExecutorImpl());
    //        MappingNode head = new MappingNode(new MappingImpl<WebEngine>("",
    //                MatchMode.PATH_STARTS_WITH, roseEngine), null);
    //        new TreeBuilder().create(head, modules);
    //        File f = new File("E:/my_documents/xml.xml");
    //        PrintWriter out = new PrintWriter(f, "UTF-8");
    //        println(head, out);
    //        out.flush();
    //    }
}
