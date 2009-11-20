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
package net.paoding.rose.web.impl.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.thread.Engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link WebResource} 代表一个资源及其支持的操作逻辑
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class WebResource {

    private static final Log logger = LogFactory.getLog(WebResource.class);

    /** ARRAY_SIZE 代表用于存放 Engine 的数组的大小 */
    private static final int ARRAY_SIZE = ReqMethod.ALL.parse().length + 1;

    private String name = "unNamed";

    private boolean isEndResource = true;

    private transient String toStringCache;

    private transient List<ReqMethod> allowedMethodsCacheUnmodifiable;

    /**
     * 该资源支持的操作逻辑，如果不支持某种操作对应位置的元素为null
     * <p>
     * 没种操作逻辑存放于该数组的唯一位置，即 {@link ReqMethod#ordinal()} 值所指向的位置
     */
    private Engine[] engines = new Engine[ARRAY_SIZE];

    private WebResource parent;

    public WebResource(WebResource parent) {
        setParent(parent);
    }

    public WebResource(WebResource parent, String name) {
        setName(name);
        setParent(parent);
    }

    public void setParent(WebResource parent) {
        this.parent = parent;
        if (this.parent != null) {
            parent.setEndResource(false);
        }
    }

    public String getName() {
        return name;
    }

    public String getAbsolutePath() {
        if (parent == null) {
            return getName();
        } else {
            return parent.getAbsolutePath() + getName();
        }
    }

    public void setName(String name) {
        this.name = name;
        clearCache();
    }

    /**
     * 添加该资源的某种操作，如果所给的 method 是 {@link ReqMethod#ALL}
     * ，则不覆盖之前设置操作，只影响那些还没有设置的操作。
     * 
     * @param method
     * @param engine
     */
    public void addEngine(ReqMethod method, Engine engine) {
        ReqMethod[] methods = method.parse();
        for (ReqMethod md : methods) {
            if (method == ReqMethod.ALL && engines[md.ordinal()] != null) {
                continue;
            }
            engines[md.ordinal()] = engine;
            clearCache();
        }
    }

    /**
     * 返回处理这个资源的处理逻辑，如果该资源不支持该操作方法返回null。
     * 
     * @param method 除 {@link ReqMethod#ALL} 外的其他 {@link ReqMethod} 实例
     * @return
     */
    public Engine getEngine(ReqMethod method) {
        if (method == ReqMethod.ALL) {
            throw new IllegalArgumentException("method");
        }
        return engines[method.ordinal()];
    }

    /**
     * 本资源是否支持此操作?
     * 
     * @param method
     * @return
     */
    public boolean isMethodAllowed(ReqMethod method) {
        return engines[method.ordinal()] != null;
    }

    public boolean isEndResource() {
        return isEndResource;
    }

    public void setEndResource(boolean isEndResource) {
        this.isEndResource = isEndResource;
    }

    protected void clearCache() {
        allowedMethodsCacheUnmodifiable = null;
        toStringCache = null;
    }

    public List<ReqMethod> getAllowedMethods() {
        if (allowedMethodsCacheUnmodifiable == null) {
            List<ReqMethod> allowedMethods = new ArrayList<ReqMethod>();
            for (ReqMethod method : ReqMethod.ALL.parse()) {
                if (getEngine(method) != null) {
                    allowedMethods.add(method);
                }
            }
            allowedMethodsCacheUnmodifiable = Collections.unmodifiableList(allowedMethods);
        }
        return allowedMethodsCacheUnmodifiable;
    }

    /**
     * 销毁该资源
     */
    public void destroy() {
        RuntimeException error = null;
        for (Engine engine : engines) {
            if (engine == null) {
                continue;
            }
            try {
                engine.destroy();
            } catch (RuntimeException e) {
                logger.error("", e);
                error = e;
            }
        }
        if (error != null) {
            throw error;
        }
    }

    @Override
    public String toString() {
        if (this.toStringCache == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getName()).append(" [");
            int oriLen = sb.length();
            for (ReqMethod method : getAllowedMethods()) {
                sb.append(method.toString()).append(", ");
            }
            if (sb.length() > oriLen) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("]");

            this.toStringCache = sb.toString();
        }
        return this.toStringCache;
    }

}
