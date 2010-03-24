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
package net.paoding.rose.web.paramresolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author 王志亮 [qieqie.wang@opi-corp.com]
 * 
 */
class ParamMetaDataImpl implements ParamMetaData {

    private Class<?> controllerClass;

    private Method method;

    private Class<?> paramType;

    private String paramName;

    private String secondParamName;

    private Map<Object, Object> userObjectMap;

    private Annotation[] annotations;

    private int index;

    public ParamMetaDataImpl(Class<?> controllerClass, Method method, Class<?> paramType,
            String paramName, int index) {
        this.controllerClass = controllerClass;
        this.method = method;
        this.paramName = paramName;
        this.paramType = paramType;
        this.index = index;
        this.annotations = method.getParameterAnnotations()[index];
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotationClass.isInstance(annotation)) {
                return annotationClass.cast(annotation);
            }
        }
        return null;
    }

    @Override
    public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    @Override
    public synchronized void setUserObject(Object key, Object userObject) {
        if (this.userObjectMap == null) {
            this.userObjectMap = new HashMap<Object, Object>();
        }
        if (userObject == null) {
            this.userObjectMap.remove(key);
        } else {
            this.userObjectMap.put(key, userObject);
        }
    }

    @Override
    public synchronized Object getUserObject(Object key) {
        return userObjectMap == null ? null : userObjectMap.get(key);
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?> getParamType() {
        return paramType;
    }

    public void setParamType(Class<?> paramType) {
        this.paramType = paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public void setSecondParamName(String secondParamName) {
        this.secondParamName = secondParamName;
    }

    @Override
    public String[] getParamNames() {
        return new String[] { paramName, secondParamName };
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

}
