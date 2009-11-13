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
package net.paoding.rose.web.impl.thread;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.paoding.rose.web.RequestPath;
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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ControllerEngine implements Engine {

    private static Log logger = LogFactory.getLog(ControllerEngine.class);

    private final Module module;

    private final String controllerPath;

    private final Object controller;

    private final boolean proxiedController;

    private final Class<?> controllerClass;

    private final String viewPrefix;

    private final ArrayList<Mapping<ActionEngine>> actionMappings = new ArrayList<Mapping<ActionEngine>>();

    private final Map<String, String[]> restSetting = new HashMap<String, String[]>();

    public ControllerEngine(Module module, String controllerPath, ControllerInfo controller) {
        this.module = module;
        this.controllerPath = controllerPath;
        this.controller = controller.getControllerObject();
        this.controllerClass = controller.getControllerClass();
        this.viewPrefix = controller.getControllerName() + "-";
        this.proxiedController = Proxy.isProxyClass(this.controller.getClass());
        if (proxiedController && logger.isDebugEnabled()) {
            logger.debug("it's a proxied controller: " + controllerClass.getName());
        }
        init();
    }

    public Module getModule() {
        return module;
    }

    public String getViewPrefix() {
        return viewPrefix;
    }

    public String getControllerPath() {
        return controllerPath;
    }

    public Object getController() {
        return controller;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public boolean isProxiedController() {
        return proxiedController;
    }

    public ArrayList<Mapping<ActionEngine>> getActionMappings() {
        return new ArrayList<Mapping<ActionEngine>>(actionMappings);
    }

    protected void init() {
        Class<?> clz = controllerClass;// 从clz得到的method不是aop后controller的clz阿!!!
        //
        List<Method> pastMethods = new LinkedList<Method>();
        while (true) {
            Method[] declaredMethods = clz.getDeclaredMethods();
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
                ActionEngine actionEngine = new ActionEngine(this, method);
                // 解析@ReqMapping
                int beginSize = actionMappings.size();
                collectRestMappingsByMethodAnnotation(method, actionEngine);
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
                        this.actionMappings.add(new MappingImpl<ActionEngine>(mappingPaths[i],
                                MatchMode.PATH_EQUALS, methods, actionEngine));
                    }
                }
            }
            for (int i = 0; i < declaredMethods.length; i++) {
                pastMethods.add(declaredMethods[i]);
            }
            clz = clz.getSuperclass();
            if (clz == null || clz.getAnnotation(AsSuperController.class) == null) {
                break;
            }
        }
        boolean showIdExists = false;
        for (int i = 1; i < actionMappings.size(); i++) {
            Mapping<ActionEngine> mapping = actionMappings.get(i);
            if (logger.isDebugEnabled()) {
                logger.debug("binding: " + module.getMappingPath() + mapping);
            }
            if (!showIdExists && mapping.match("/198107", "GET") != null) {
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
                    this.actionMappings.add(new MappingImpl<ActionEngine>("/show/{id:[0-9]+}",
                            MatchMode.PATH_EQUALS, pathMapping.getMethods(), pathMapping
                                    .getTarget()));
                    this.actionMappings.add(new MappingImpl<ActionEngine>("/{id:[0-9]+}",
                            MatchMode.PATH_EQUALS, pathMapping.getMethods(), pathMapping
                                    .getTarget()));
                    break;
                }
            }
        }
        Collections.sort(actionMappings);

        // URI没有提供action的处理(REST)
        REST rest = controllerClass.getAnnotation(REST.class);
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
                    if (logger.isDebugEnabled()) {
                        logger.debug(restMethod.getName().toUpperCase() + " "
                                + module.getMappingPath() + controllerPath + "="
                                + Arrays.toString(candidates));
                    }
                } catch (Exception e) {
                    logger.error("error happened when invoking " + restMethod.getName(), e);
                }
            }
        } else {
            restSetting.put("GET", new String[] { "/index", "/get", "/render" });
            restSetting.put("POST", new String[] { "/post", "/add", "/create", "/update", });
            restSetting.put("PUT", new String[] { "/put", "/update", });
        }
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

    public boolean match(final InvocationBean inv) {
        final HttpServletRequest request = inv.getRequest();
        final RequestPath requestPath = inv.getRequestPath();
        String actionPath;
        String requstMethod = request.getMethod();
        String controllerPathInfo = requestPath.getControllerPathInfo();

        // 计算actionPath，用于定位所要处理的actionEngine
        if (controllerPathInfo.startsWith(requestPath.getControllerPath())
                && (controllerPathInfo.length() == requestPath.getControllerPath().length() || controllerPathInfo
                        .charAt(requestPath.getControllerPath().length()) == '/')) {
            // controllerPath是controllerMappingPath的一部分(大部分情况)！
            actionPath = controllerPathInfo.substring(requestPath.getControllerPath().length());
        } else {
            // 使用了defautlController的，此时整个controllerMappingPath都是actionPath
            actionPath = controllerPathInfo;
        }

        // 通过path找相应的ActionEngine
        MatchResult<ActionEngine> matchResult = null;
        for (Mapping<ActionEngine> m : actionMappings) {
            if (logger.isDebugEnabled()) {
                logger.debug("try matching(" + actionPath + "): " + m);
            }
            matchResult = m.match(actionPath, requstMethod);
            if (matchResult != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("matched: " + m);
                    logger.debug("matchResult.matchedString= " + matchResult.getMatchedString());
                }
                requestPath.setActionPath(matchResult.getMatchedString());
                break;
            }
        }
        // REST请求的?
        if (matchResult == null && actionPath.length() <= 1) {
            String[] candidates = restSetting.get(requstMethod);
            if (candidates == null) {
                candidates = new String[] { "/" + requstMethod.toLowerCase() };
                restSetting.put(requstMethod, candidates); // 自学习
            }
            if (logger.isDebugEnabled()) {
                logger.debug("actionPath is empty, try to use @REST candidates="
                        + Arrays.toString(candidates) + " (" + requestPath.getUri() + ")");
            }
            for (Mapping<ActionEngine> actionMapping : actionMappings) {
                if (logger.isDebugEnabled()) {
                    logger.debug("try matching(" + actionPath + "): " + actionMapping);
                }
                for (String candidate : candidates) {
                    matchResult = actionMapping.match(candidate, requstMethod);
                    if (matchResult != null) {
                        break;
                    }
                }
                if (matchResult != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("matched(" + actionPath + "): " + actionMapping);
                        logger
                                .debug("matchResult.matchedString= "
                                        + matchResult.getMatchedString());
                    }
                    requestPath.setActionPath(matchResult.getMatchedString());
                    break;
                }
            }
        }

        // 从这个Controller实在找不到对应的actionEngine，返回CONTINUE，获取由其它的Controller还能处理，或许吧!
        if (matchResult == null) {
            return false;
        }

        // 好！到此确认，这个请求应由Rose来处理，而且是这个Controller的这个action来处理
        inv.setActionMatchResult(matchResult);
        return true;
    }

    public Object invoke(final InvocationBean inv) throws Throwable {
        for (String matchResultParam : inv.getControllerMatchResult().getParameterNames()) {
            inv.addModel(matchResultParam, inv.getControllerMatchResult().getParameter(
                    matchResultParam));
        }
        return inv.getActionEngine().invoke(inv);
    }

    public void destroy() {
        for (Mapping<ActionEngine> mapping : actionMappings) {
            try {
                mapping.getTarget().destroy();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public String toString() {
        return getControllerClass().getName();
    }

    /**
     * 识别定义在method上的rest相关的标签
     * 
     * @param method
     * @param actionEngine
     */
    private void collectRestMappingsByMethodAnnotation(Method method, ActionEngine actionEngine) {
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Delete) {
                this.actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.DELETE }, actionEngine));
            } else if (annotation instanceof Get) {
                this.actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.GET }, actionEngine));
            } else if (annotation instanceof Head) {
                this.actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.HEAD }, actionEngine));
            } else if (annotation instanceof Options) {
                this.actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.OPTIONS }, actionEngine));
            } else if (annotation instanceof Post) {
                this.actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.POST }, actionEngine));
            } else if (annotation instanceof Put) {
                this.actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.PUT }, actionEngine));
            } else if (annotation instanceof Trace) {
                this.actionMappings.add(new MappingImpl<ActionEngine>("", MatchMode.PATH_EQUALS,
                        new ReqMethod[] { ReqMethod.TRACE }, actionEngine));
            }
        }
    }

}
