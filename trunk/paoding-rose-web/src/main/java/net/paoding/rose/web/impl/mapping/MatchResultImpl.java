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
package net.paoding.rose.web.impl.mapping;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.paoding.rose.web.impl.thread.Engine;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class MatchResultImpl implements MatchResult {

    /** 结果字符串 */
    private String value;

    /** 所匹配的资源 */
    private WebResource resource;

    private Engine engine;

    /** 从结果字符串中得到的资源参数值(如果该资源使用了使用了参数化的映射地址) */
    private Map<String, String> parameters;

    /**
     * 创建新的匹配结果对象
     * 
     * @param value 匹配结果字符串
     */
    public MatchResultImpl(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public WebResource getResource() {
        return resource;
    }

    public Engine getEngine() {
        return engine;
    }

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setResource(WebResource resource) {
        this.resource = resource;
    }

    public int getParameterCount() {
        return parameters == null ? 0 : parameters.size();
    }

    public Collection<String> getParameterNames() {
        List<String> empty = Collections.emptyList();
        return parameters == null ? empty : parameters.keySet();
    }

    public void putParameter(String name, String value) {
        if (parameters == null) {
            synchronized (this) {
                if (parameters == null) {
                    parameters = new Hashtable<String, String>(4);
                }
            }
        }
        parameters.put(name, value);
    }

    public String getParameter(String name) {
        return parameters == null ? null : parameters.get(name);
    }
}
