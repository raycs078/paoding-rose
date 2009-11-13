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
package net.paoding.rose.web;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ControllerInterceptorAdapter implements NamedControllerInterceptor,
        ControllerInterceptor {

    protected Log logger = LogFactory.getLog(getClass());

    private String name;

    private int priority;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public final boolean isForAction(Class<?> controllerClazz, Method actionMethod) {
        // 返回false，表示控制器或其方法没有标注本拦截器所要求的注解
        if (!checkRequiredAnnotations(controllerClazz, actionMethod)) {
            return false;
        }
        // 返回true，表示控制器或其方法标注了“拒绝”注解
        if (checkDenyAnnotations(controllerClazz, actionMethod)) {
            return false;
        }
        return isForAction(actionMethod, controllerClazz);
    }

    protected boolean isForAction(Method actionMethod, Class<?> controllerClazz) {
        return true;
    }

    @Override
    public boolean isForDispatcher(Dispatcher dispatcher) {
        return true;
    }

    @Override
    public Object before(Invocation inv) throws Exception {
        return true;
    }

    @Override
    public Object after(Invocation inv, Object instruction) throws Exception {
        return instruction;
    }

    @Override
    public void afterCompletion(Invocation inv, Throwable ex) throws Exception {
    }

    @Override
    public String toString() {
        return getName();
    }

    protected static ListBuilder createList(int size) {
        return new ListBuilder(size);
    }

    public static class ListBuilder {

        List<Class<? extends Annotation>> list;

        ListBuilder(int size) {
            list = new ArrayList<Class<? extends Annotation>>(size);
        }

        public ListBuilder add(Class<? extends Annotation> a) {
            list.add(a);
            return this;
        }

        public List<Class<? extends Annotation>> getList() {
            return list;
        }
    }

    /**
     * 返回false，表示控制器或其方法没有标注本拦截器所要求的注解
     * 
     * @param controllerClazz 控制器类
     * @param actionMethod 控制器处理方法
     * @return
     */
    protected final boolean checkRequiredAnnotations(Class<?> controllerClazz, Method actionMethod) {
        List<Class<? extends Annotation>> annotations = getRequiredAnnotationClasses();
        if (annotations == null || annotations.size() == 0) {
            return true;
        }
        for (Class<? extends Annotation> annotation : annotations) {
            if (annotation == null) {
                continue;
            }
            if (actionMethod.isAnnotationPresent(annotation)
                    || controllerClazz.isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回true，表示控制器或其方法标注了“拒绝”注解
     * 
     * @param controllerClazz
     * @param actionMethod
     * @return
     */
    protected final boolean checkDenyAnnotations(Class<?> controllerClazz, Method actionMethod) {
        List<Class<? extends Annotation>> annotations = getDenyAnnotationClasses();
        if (annotations == null || annotations.size() == 0) {
            return false;
        }
        for (Class<? extends Annotation> annotation : annotations) {
            if (annotation == null) {
                continue;
            }
            if (actionMethod.isAnnotationPresent(annotation)
                    || controllerClazz.isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }

    protected List<Class<? extends Annotation>> getRequiredAnnotationClasses() {
        Class<? extends Annotation> clazz = getRequiredAnnotationClass();
        if (clazz == null) {
            clazz = getAnnotationClass();
            if (clazz != null) {
                throw new IllegalStateException("please change method name from"
                        + this.getClass().getName()
                        + ".getAnnotationClass to getRequiredAnnotationClasses");
            }
        }
        if (clazz != null) {
            List<Class<? extends Annotation>> list = new ArrayList<Class<? extends Annotation>>();
            list.add(clazz);
            return list;
        }
        return Collections.emptyList();
    }

    protected Class<? extends Annotation> getRequiredAnnotationClass() {
        return null;
    }

    /**
     * 请改实现方法： {@link #getRequiredAnnotationClass()}
     * 
     * @return
     */
    @Deprecated
    protected Class<? extends Annotation> getAnnotationClass() {
        return null;
    }

    protected List<Class<? extends Annotation>> getDenyAnnotationClasses() {
        Class<? extends Annotation> clazz = getDenyAnnotationClass();
        if (clazz != null) {
            List<Class<? extends Annotation>> list = new ArrayList<Class<? extends Annotation>>();
            list.add(clazz);
            return list;
        }
        return Collections.emptyList();
    }

    protected Class<? extends Annotation> getDenyAnnotationClass() {
        return null;
    }

}
