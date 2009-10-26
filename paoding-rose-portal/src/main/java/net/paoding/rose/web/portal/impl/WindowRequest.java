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
package net.paoding.rose.web.portal.impl;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import net.paoding.rose.web.portal.util.Enumerator;

/**
 * 封装窗口请求，使每个窗口都有自己的独立属性空间，同时又能共享共同的portal请求对象的属性
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class WindowRequest extends HttpServletRequestWrapper {

    /**
     * 窗口请求对象私有的、有别于其他窗口的属性
     */
    private Map<String, Object> privateAttributes = Collections
            .synchronizedMap(new HashMap<String, Object>());

    /**
     * 那些属性是这个窗口所不要的，在此标志
     */
    private Set<String> deleteAttributes = Collections.synchronizedSet(new HashSet<String>(4));

    public WindowRequest(HttpServletRequest request) {
        super(request);
    }

    // ------------------------------------------------- ServletRequest Methods

    /**
     * 返回这个窗口的私有属性或portal主控请求对象的共同属性
     * 
     * @param name Name of the attribute to retrieve
     */
    public Object getAttribute(String name) {
        if (deleteAttributes.contains(name)) {
            return null;
        }
        Object value = privateAttributes.get(name);
        if (value == null) {
            value = super.getAttribute(name);
        }
        return value;
    }

    /**
     * 返回这个窗口的私有属性名加portal主控请求对象共同属性的属性名
     */
    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames() {
        HashSet<String> keys;
        synchronized (privateAttributes) {
            keys = new HashSet<String>(privateAttributes.keySet());
        }
        Enumeration<String> names = super.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            if (!deleteAttributes.contains(name)) {
                keys.add(name);
            }
        }
        return new Enumerator(keys);
    }

    /**
     * 实际删除私有属性，如果是窗口共有的portal属性，只是在本窗口中做删除标志，其他窗口还能正常获取
     * 
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {
        privateAttributes.remove(name);
        deleteAttributes.add(name);
    }

    /**
     * 
     * 设置窗口私有属性
     * 
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set
     */
    public void setAttribute(String name, Object value) {
        privateAttributes.put(name, value);
        if (value == null) {
            deleteAttributes.add(name);
        } else {
            deleteAttributes.remove(name);
        }
    }

}
