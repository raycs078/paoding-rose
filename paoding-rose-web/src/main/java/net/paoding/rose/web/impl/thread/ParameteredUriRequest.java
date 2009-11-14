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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequestWrapper;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ParameteredUriRequest extends HttpServletRequestWrapper {

    private final List<String> parameterNames;

    private final InvocationBean inv;

    public ParameteredUriRequest(InvocationBean inv, List<String> parameterNames) {
        super(inv.getRequest());
        this.inv = inv;
        this.parameterNames = parameterNames;
    }

    // 优先获取queryString或forward之后的请求的参数，只有获取不到时，才从URI里获取
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value == null) {
            value = inv.getMatchResultParameter(name);
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map getParameterMap() {
        Map<String, String[]> map = new HashMap<String, String[]>(super.getParameterMap());
        for (String name : parameterNames) {
            if (!map.containsKey(name)) {
                map.put(name, inv.getMethodParameterNames());
            }
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] value = super.getParameterValues(name);
        // javadoc: 
        // Returns an array of String objects containing all of the values the given request parameter has,
        // or null if the parameter does not exist.
        if (value == null || value.length == 0) {
            value = inv.getMatchResultParameterValues(name);
        }
        return value == null || value.length == 0 ? null : value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getParameterNames() {
        final Enumeration<String> requestParamNames = super.getParameterNames();
        return new Enumeration<String>() {

            final Iterator<String> matchResultParamNames = parameterNames.iterator();

            @Override
            public boolean hasMoreElements() {
                return matchResultParamNames.hasNext() || requestParamNames.hasMoreElements();
            }

            @Override
            public String nextElement() {
                if (matchResultParamNames.hasNext()) {
                    return matchResultParamNames.next();
                }
                if (requestParamNames.hasMoreElements()) {
                    return requestParamNames.nextElement();
                }
                throw new NoSuchElementException();
            }

        };
    }
}
