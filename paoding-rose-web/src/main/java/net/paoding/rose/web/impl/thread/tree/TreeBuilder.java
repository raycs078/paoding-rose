package net.paoding.rose.web.impl.thread.tree;

import static net.paoding.rose.web.impl.mapping.MatchMode.PATH_STARTS_WITH;
import static net.paoding.rose.web.impl.mapping.ModifiedMapping.changeTarget;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.paoding.rose.web.annotation.AsSuperController;
import net.paoding.rose.web.annotation.Ignored;
import net.paoding.rose.web.annotation.REST;
import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.annotation.rest.Delete;
import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.annotation.rest.Head;
import net.paoding.rose.web.annotation.rest.Options;
import net.paoding.rose.web.annotation.rest.Post;
import net.paoding.rose.web.annotation.rest.Put;
import net.paoding.rose.web.annotation.rest.Trace;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.mapping.MappingImpl;
import net.paoding.rose.web.impl.mapping.MatchMode;
import net.paoding.rose.web.impl.module.ControllerInfo;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.thread.ActionEngine;
import net.paoding.rose.web.impl.thread.ControllerEngine;
import net.paoding.rose.web.impl.thread.MatchResult;
import net.paoding.rose.web.impl.thread.ModuleEngine;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class TreeBuilder {

    public void create(MappingNode head, List<Module> modules) {
        createModuleMappingNodes(head, modules);
    }

    private void createModuleMappingNodes(MappingNode head, List<Module> modules) {
        List<Mapping<ModuleEngine>> mappings = new ArrayList<Mapping<ModuleEngine>>(modules.size());
        for (Module module : modules) {
            if (module == null) {
                continue;
            }
            String path = module.getMappingPath();
            ModuleEngine engine = new ModuleEngine(module);
            Mapping<ModuleEngine> mapping = new MappingImpl<ModuleEngine>( // NL
                    path, PATH_STARTS_WITH, engine);
            mappings.add(mapping);
        }
        Collections.sort(mappings);
        MappingNode lastModuleNode = null;
        for (Mapping<ModuleEngine> moduleMapping : mappings) {
            MappingNode moduleNode = new MappingNode(moduleMapping, head);
            if (head.leftMostChild == null) {
                head.leftMostChild = moduleNode;
            } else {
                lastModuleNode.sibling = moduleNode;
            }
            lastModuleNode = moduleNode;
            //
            ModuleEngine moduleEngine = moduleMapping.getTarget();
            Module module = moduleEngine.getModule();
            List<Mapping<ControllerEngine>> controllerMappings = createControllerMappings(module);
            MappingNode lastControllerNode = null;
            for (Mapping<ControllerEngine> controllerMapping : controllerMappings) {
                MappingNode controllerNode = new MappingNode(controllerMapping, moduleNode);
                if (moduleNode.leftMostChild == null) {
                    moduleNode.leftMostChild = controllerNode;
                } else {
                    lastControllerNode.sibling = controllerNode;
                }
                lastControllerNode = controllerNode;
                //
                ControllerEngine controllerEngine = controllerMapping.getTarget();
                List<Mapping<ActionEngine>> actionMappings = createActionMappings(module,
                        controllerEngine.getControllerClass(), controllerEngine.getController());
                MappingNode lastActionNode = null;
                for (Mapping<ActionEngine> actionMapping : actionMappings) {
                    MappingNode actionNode = new MappingNode(actionMapping, controllerNode);
                    if (controllerNode.leftMostChild == null) {
                        controllerNode.leftMostChild = actionNode;
                    } else {
                        lastActionNode.sibling = actionNode;
                    }
                    lastActionNode = actionNode;
                }
            }
        }
    }

    private List<Mapping<ActionEngine>> createActionMappings(Module module,
            Class<?> controllerClass, Object controller) {
        ArrayList<Mapping<ActionEngine>> actionMappings = new ArrayList<Mapping<ActionEngine>>();
        //        Class<?> clz = controllerClass;// 从clz得到的method不是aop后controller的clz阿!!!
        //
        List<Method> pastMethods = new LinkedList<Method>();
        while (true) {
            Method[] declaredMethods = controllerClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                // public, not static, not abstract
                if (!Modifier.isPublic(method.getModifiers())
                        || Modifier.isAbstract(method.getModifiers())
                        || Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                // 去除已经标志不作为接口的方法
                if (method.getAnnotation(Ignored.class) != null) {
                    continue;
                }
                if (ignoresCommonMethod(method)) {
                    continue;
                }
                // 刚才在继承类(子类)已经声明的方法，不必重复处理了
                boolean broken = false;
                for (Method past : pastMethods) {
                    if (past.getName().equals(method.getName())) {
                        if (Arrays.equals(past.getParameterTypes(), method.getParameterTypes())) {
                            broken = true;
                            break;
                        }
                    }
                }
                if (broken) {
                    continue;
                }
                String methodPath = "/" + method.getName();
                ActionEngine actionEngine = new ActionEngine(module, controllerClass, controller,
                        method);
                // 解析@ReqMapping
                int beginSize = actionMappings.size();
                collectRestMappingsByMethodAnnotation(actionMappings, method, actionEngine);
                int restCount = actionMappings.size() - beginSize;
                ReqMapping reqMappingAnnotation = method.getAnnotation(ReqMapping.class);
                ReqMethod[] methods = null;
                String[] mappingPaths = null;
                if (reqMappingAnnotation != null) {
                    methods = reqMappingAnnotation.methods();
                    mappingPaths = reqMappingAnnotation.path();
                    // 如果mappingPaths.length==0，表示没有任何path可以映射到这个action了
                    for (int i = 0; i < mappingPaths.length; i++) {
                        if (ReqMapping.DEFAULT_PATH.equals(mappingPaths[i])) {
                            mappingPaths[i] = methodPath;
                        } else if (mappingPaths[i].length() > 0 && mappingPaths[i].charAt(0) != '/') {
                            mappingPaths[i] = '/' + mappingPaths[i];
                        } else if (mappingPaths[i].equals("/")) {
                            mappingPaths[i] = "";
                        }
                    }
                } else if (restCount == 0) {
                    methods = new ReqMethod[] { ReqMethod.ALL };
                    mappingPaths = new String[] { methodPath };
                }
                if (!ArrayUtils.isEmpty(mappingPaths) && !ArrayUtils.isEmpty(methods)) {
                    for (int i = 0; i < mappingPaths.length; i++) {
                        actionMappings.add(new MappingImpl<ActionEngine>(mappingPaths[i],
                                MatchMode.PATH_EQUALS, methods, actionEngine));
                    }
                }
            }
            for (int i = 0; i < declaredMethods.length; i++) {
                pastMethods.add(declaredMethods[i]);
            }
            controllerClass = controllerClass.getSuperclass();
            if (controllerClass == null
                    || controllerClass.getAnnotation(AsSuperController.class) == null) {
                break;
            }
        }
        boolean showIdExists = false;
        for (int i = 1; i < actionMappings.size(); i++) {
            Mapping<ActionEngine> mapping = actionMappings.get(i);
            MatchResult<?> mr;
            if (!showIdExists && (mr = mapping.match("/198107", "GET", null)) != null
                    && mr.isRequestMethodSupported()) {
                showIdExists = true;
            }
        }
        // /user/123456自动映射到UserController的show()方法
        if (!showIdExists) {
            for (int i = 1; i < actionMappings.size(); i++) {
                Mapping<ActionEngine> pathMapping = actionMappings.get(i);
                Method method = pathMapping.getTarget().getMethod();
                if ("show".equals(method.getName())
                        && method.getAnnotation(ReqMapping.class) == null) {
                    actionMappings.add(new MappingImpl<ActionEngine>("/show/{id:[0-9]+}",
                            MatchMode.PATH_EQUALS, pathMapping.getMethods(), pathMapping
                                    .getTarget()));
                    actionMappings.add(new MappingImpl<ActionEngine>("/{id:[0-9]+}",
                            MatchMode.PATH_EQUALS, pathMapping.getMethods(), pathMapping
                                    .getTarget()));
                    break;
                }
            }
        }
        Collections.sort(actionMappings);

        // URI没有提供action的处理(REST)
        REST rest = controllerClass.getAnnotation(REST.class);
        Map<String, String[]> restSetting = new HashMap<String, String[]>();
        if (rest != null) {
            Method[] restMethods = rest.getClass().getMethods();
            for (Method restMethod : restMethods) {
                if (!ArrayUtils.contains(new String[] { "get", "post", "put", "options", "trace",
                        "delete", "head" }, restMethod.getName())) {
                    continue;
                }
                if (restMethod.getParameterTypes().length != 0) {
                    continue;
                }
                if (restMethod.getReturnType() != String[].class) {
                    continue;
                }
                String[] candidates;
                try {
                    candidates = (String[]) restMethod.invoke(rest);
                    for (int i = 0; i < candidates.length; i++) {
                        if (candidates[i].length() > 0 && candidates[i].charAt(0) != '/') {
                            candidates[i] = '/' + candidates[i];
                        }
                    }
                    restSetting.put(restMethod.getName().toUpperCase(), candidates);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            restSetting.put("GET", new String[] { "/index", "/get", "/render" });
            restSetting.put("POST", new String[] { "/post", "/add", "/create", "/update", });
            restSetting.put("PUT", new String[] { "/put", "/update", });
            restSetting.put("DELETE", new String[] { "/delete", });
            restSetting.put("OPTIONS", new String[] { "/options", });
            restSetting.put("TRACE", new String[] { "/trace", });
            restSetting.put("HEAD", new String[] { "/head", });
        }
        //
        for (Map.Entry<String, String[]> entry : restSetting.entrySet()) {
            MatchResult<?> matchResult = null;
            for (Mapping<ActionEngine> actionMapping : actionMappings) {
                String[] candidates = entry.getValue();
                for (String candidate : candidates) {
                    matchResult = actionMapping.match(candidate, entry.getKey(), null);
                    if (matchResult != null && matchResult.isRequestMethodSupported()) {
                        if (matchResult.getMatchedString().length() > 0) {
                            actionMappings.add(new MappingImpl<ActionEngine>("",
                                    MatchMode.PATH_EQUALS, new ReqMethod[] { ReqMethod.map(entry
                                            .getKey()) }, actionMapping.getTarget()));
                        }
                        break;
                    }
                }
                if (matchResult != null) {
                    break;
                }
            }
        }
        //
        HashMap<String, Set<ReqMethod>> resouceMethodsList = new HashMap<String, Set<ReqMethod>>();
        for (Mapping<ActionEngine> actionMapping : actionMappings) {
            Set<ReqMethod> resouceMethods = resouceMethodsList.get(actionMapping.getPath());
            if (resouceMethods == null) {
                resouceMethods = new HashSet<ReqMethod>(8);
                resouceMethodsList.put(actionMapping.getPath(), resouceMethods);
            }
            for (ReqMethod reqMethod : actionMapping.getMethods()) {
                if (reqMethod.equals(ReqMethod.ALL)) {
                    ReqMethod[] terms = new ReqMethod[] { ReqMethod.GET, ReqMethod.HEAD,
                            ReqMethod.POST, ReqMethod.PUT, ReqMethod.DELETE, ReqMethod.OPTIONS,
                            ReqMethod.TRACE };
                    for (ReqMethod term : terms) {
                        resouceMethods.add(term);
                    }
                } else {
                    resouceMethods.add(reqMethod);
                }
            }
            ((MappingImpl<?>) actionMapping).setResourceMethods(resouceMethods);
        }
        return actionMappings;
    }

    private boolean ignoresCommonMethod(Method method) {
        String name = method.getName();
        if (name.equals("toString") || name.equals("hashCode") || name.equals("equals")
                || name.equals("wait") || name.equals("getClass") || name.equals("clone")
                || name.equals("notify") || name.equals("notifyAll") || name.equals("finalize")) {
            if (null == method.getAnnotation(ReqMapping.class)
                    && null == method.getAnnotation(Get.class)) {
                return true;
            }
        }
        if (name.startsWith("get") && name.length() > 3
                && Character.isUpperCase(name.charAt("get".length()))
                && method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
            if (null == method.getAnnotation(ReqMapping.class)
                    && null == method.getAnnotation(Get.class)) {
                return true;
            }
        }
        if (name.startsWith("is")
                && name.length() > 3
                && Character.isUpperCase(name.charAt("is".length()))
                && method.getParameterTypes().length == 0
                && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
            if (null == method.getAnnotation(ReqMapping.class)
                    && null == method.getAnnotation(Get.class)) {
                return true;
            }
        }
        if (name.startsWith("set") && name.length() > 3
                && Character.isUpperCase(name.charAt("set".length()))
                && method.getParameterTypes().length == 1 && method.getReturnType() == void.class) {
            if (null == method.getAnnotation(ReqMapping.class)
                    && null == method.getAnnotation(Post.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 识别定义在method上的rest相关的标签
     * 
     * @param method
     * @param actionEngine
     */
    private void collectRestMappingsByMethodAnnotation(List<Mapping<ActionEngine>> actionMappings,
            Method method, ActionEngine actionEngine) {
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Delete) {
                actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.DELETE }, actionEngine));
            } else if (annotation instanceof Get) {
                actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.GET }, actionEngine));
            } else if (annotation instanceof Head) {
                actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.HEAD }, actionEngine));
            } else if (annotation instanceof Options) {
                actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.OPTIONS }, actionEngine));
            } else if (annotation instanceof Post) {
                actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.POST }, actionEngine));
            } else if (annotation instanceof Put) {
                actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.PUT }, actionEngine));
            } else if (annotation instanceof Trace) {
                actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.TRACE }, actionEngine));
            }
        }
    }

    protected List<Mapping<ControllerEngine>> createControllerMappings(Module module) {
        // module返回的mappings
        List<Mapping<ControllerInfo>> rawMappings = module.getControllerMappings();

        // module定义的default mapping. rawDefMapping可能因为存在path=""的控制器，而被重置为空
        Mapping<ControllerInfo> rawDefMapping = module.getDefaultController();

        // 改target对象为engine后的mappings
        List<Mapping<ControllerEngine>> yesMappings = new ArrayList<Mapping<ControllerEngine>>(
                rawMappings.size());

        // 仅当最后rawDefMapping仍未非空时有效; 而且只能放置到mappings的最后
        Mapping<ControllerEngine> defMapping = null;
        for (Mapping<ControllerInfo> rawMapping : rawMappings) {
            // 将rawMapping转为target是ControllerEngine的mapping，放入到rightMappings中
            String path = rawMapping.getPath();
            ControllerInfo controllerInfo = rawMapping.getTarget();
            ControllerEngine controllerEngine = new ControllerEngine(module, path, controllerInfo);

            Mapping<ControllerEngine> rightMapping = changeTarget(rawMapping, controllerEngine);
            yesMappings.add(rightMapping);

            // 如果已经有定义path=""的控制器，可省去构造下面的lastMapping对象
            // 另，这个代码位置放在这，其正确性建立在rawMappings已经是排序的基础上：path=""如果存在，则一定是在最后
            if (StringUtils.isEmpty(rawMapping.getPath())) {
                defMapping = null;
            } else if (rawMapping == rawDefMapping) {
                defMapping = rightMapping;
            }
        }
        if (defMapping != null) {
            Mapping<ControllerEngine> lastMapping = new MappingImpl<ControllerEngine>(//NL
                    "", PATH_STARTS_WITH, defMapping.getTarget());
            yesMappings.add(lastMapping);
        }
        return yesMappings;
    }

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
