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

import javax.servlet.http.HttpServletRequestWrapper;

import net.paoding.rose.web.portal.Window;
import net.paoding.rose.web.portal.util.Enumerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class WindowRequest extends HttpServletRequestWrapper {

    private static final Log logger = LogFactory.getLog(WindowRequest.class);

    /**
     * The request attributes for this request. This is initialized from
     * the wrapped request, but updates are allowed.
     */
    protected Map<String, Object> attributes = Collections
            .synchronizedMap(new HashMap<String, Object>());

    public WindowRequest(Window window) {
        super(window.getPortal().getRequest());
    }

    // ------------------------------------------------- ServletRequest Methods

    /**
     * Override the <code>getAttribute()</code> method of the wrapped
     * request.
     * 
     * @param name Name of the attribute to retrieve
     */
    public Object getAttribute(String name) {
        Object value = attributes.get(name);
        if (value == null) {
            value = super.getAttribute(name);
            if (value != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("get attribute '%s' from portal request ('%s')",
                            name, value));
                }
            }
        }
        return value;
    }

    /**
     * Override the <code>getAttributeNames()</code> method of the wrapped
     * request.
     */
    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames() {
        HashSet<String> keys = new HashSet<String>(attributes.keySet());
        Enumeration<String> names = super.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            keys.add(name);
        }
        return new Enumerator(keys);
    }

    /**
     * Override the <code>removeAttribute()</code> method of the wrapped
     * request.
     * 
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * Override the <code>setAttribute()</code> method of the wrapped
     * request.
     * 
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

}
