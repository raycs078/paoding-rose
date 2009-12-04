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
package net.paoding.rose.web.impl.module;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.paoding.rose.web.annotation.AsSuperController;
import net.paoding.rose.web.annotation.Ignored;
import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.annotation.rest.Delete;
import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.annotation.rest.Head;
import net.paoding.rose.web.annotation.rest.Options;
import net.paoding.rose.web.annotation.rest.Post;
import net.paoding.rose.web.annotation.rest.Put;
import net.paoding.rose.web.annotation.rest.Trace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ControllerRef {

    private static Log logger = LogFactory.getLog(ControllerRef.class);

    private String[] mappingPaths;

    // e.g. UserController, UserInfoController的controllerName分别是user和userInfo
    private String controllerName;

    private Class<?> controllerClass;

    private Object controllerObject;

    public ControllerRef() {
    }

    public ControllerRef(String[] mappingPaths, String controllerName, Object controllerObject,
            Class<?> controllerClass) {
        setMappingPaths(mappingPaths);
        setControllerName(controllerName);
        setControllerObject(controllerObject);
        setControllerClass(controllerClass);
    }

    public MethodRef[] getActions() {
        if (this.actions == null) {
            init();
        }
        return actions.toArray(new MethodRef[0]);
    }

    List<MethodRef> actions;

    private void init() {
        actions = new LinkedList<MethodRef>();
        Class<?> clz = controllerClass;
        //
        List<Method> pastMethods = new LinkedList<Method>();
        while (true) {
            Method[] declaredMethods = clz.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (isMethodIgnored(pastMethods, method, controllerClass)) {
                    continue;
                }

                Map<ReqMethod, String[]> shotcutMappings = collectsShotcutMappings(method);
                String methodPath = "/" + method.getName();

                ReqMapping reqMappingAnnotation = method.getAnnotation(ReqMapping.class);
                ReqMethod[] methods = new ReqMethod[0];
                String[] mappingPaths = new String[0];
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
                } else if (shotcutMappings.size() == 0) {
                    methods = new ReqMethod[] { ReqMethod.GET, ReqMethod.POST };
                    mappingPaths = new String[] { methodPath };
                }
                if (mappingPaths.length > 0 || methods.length > 0 || shotcutMappings.size() > 0) {
                    MethodRef methodRef = new MethodRef();

                    for (Map.Entry<ReqMethod, String[]> entry : shotcutMappings.entrySet()) {
                        methodRef.setReqMapping(entry.getValue(),
                                new ReqMethod[] { entry.getKey() });
                    }
                    methodRef.setReqMapping(mappingPaths, methods);
                    methodRef.setMethod(method);
                    this.actions.add(methodRef);
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
        if (actions.size() == 0) {
            MethodRef denyAction = new MethodRef();
            denyAction.setReqMapping(new String[] { "" }, new ReqMethod[0]);
            this.actions.add(denyAction);
        }
    }

    private Map<ReqMethod, String[]> collectsShotcutMappings(Method method) {
        Map<ReqMethod, String[]> restMethods = new HashMap<ReqMethod, String[]>();
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Delete) {
                restMethods.put(ReqMethod.DELETE, ((Delete) annotation).value());
            } else if (annotation instanceof Get) {
                restMethods.put(ReqMethod.GET, ((Get) annotation).value());
            } else if (annotation instanceof Head) {
                restMethods.put(ReqMethod.HEAD, ((Head) annotation).value());
            } else if (annotation instanceof Options) {
                restMethods.put(ReqMethod.OPTIONS, ((Options) annotation).value());
            } else if (annotation instanceof Post) {
                restMethods.put(ReqMethod.POST, ((Post) annotation).value());
            } else if (annotation instanceof Put) {
                restMethods.put(ReqMethod.PUT, ((Put) annotation).value());
            } else if (annotation instanceof Trace) {
                restMethods.put(ReqMethod.TRACE, ((Trace) annotation).value());
            } else {}
        }
        return restMethods;
    }

    private boolean isMethodIgnored(List<Method> pastMethods, Method method,
            Class<?> controllerClass) {
        // public, not static, not abstract, @Ignored
        if (!Modifier.isPublic(method.getModifiers()) || Modifier.isAbstract(method.getModifiers())
                || Modifier.isStatic(method.getModifiers())
                || method.isAnnotationPresent(Ignored.class)) {
            if (logger.isDebugEnabled()) {
                logger.debug("ignores methods of controller " + controllerClass.getName() + "."
                        + method.getName() + "  [@ignored?not public?abstract?static?]");
            }
            return true;
        }
        if (ignoresCommonMethod(method)) {
            if (logger.isDebugEnabled()) {
                logger.debug("ignores common methods of controller " + controllerClass.getName()
                        + "." + method.getName());
            }
            return true;
        }
        // 刚才在继承类(子类)已经声明的方法，不必重复处理了
        for (Method past : pastMethods) {
            if (past.getName().equals(method.getName())) {
                if (Arrays.equals(past.getParameterTypes(), method.getParameterTypes())) {
                    return true;
                }
            }
        }
        return false;
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

    public void setMappingPaths(String[] mappingPaths) {
        this.mappingPaths = mappingPaths;
    }

    public String[] getMappingPaths() {
        return mappingPaths;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public Object getControllerObject() {
        return controllerObject;
    }

    public void setControllerObject(Object controllerObject) {
        this.controllerObject = controllerObject;
    }

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    @Override
    public String toString() {
        return controllerClass.getName();
    }

}
