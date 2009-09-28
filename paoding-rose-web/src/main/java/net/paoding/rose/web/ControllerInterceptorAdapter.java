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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ControllerInterceptorAdapter implements ControllerInterceptor,
        NamedControllerInterceptor {

    protected Log logger = LogFactory.getLog(getClass());

    private int priority;

    private String name;

    @Override
    public void setName(String name) {
        this.name = name;

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isSupportDispatcher(Dispatcher dispatcher) {
        return true;
    }

    @Override
    public List<Class<? extends Annotation>> getAnnotationClasses() {
        Class<? extends Annotation> clazz = getAnnotationClass();
        if (clazz != null) {
            List<Class<? extends Annotation>> list = new ArrayList<Class<? extends Annotation>>();
            list.add(clazz);
            return list;
        }
        return Collections.emptyList();
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return null;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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

}
