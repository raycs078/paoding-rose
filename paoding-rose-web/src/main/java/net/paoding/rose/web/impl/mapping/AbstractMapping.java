/*
* Copyright 2007-2009 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package net.paoding.rose.web.impl.mapping;

import java.util.Arrays;
import java.util.Set;

import net.paoding.rose.web.annotation.ReqMethod;

import org.apache.commons.lang.ArrayUtils;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public abstract class AbstractMapping<T> implements Mapping<T> {

    protected final String path;

    protected final ReqMethod[] methods;

    protected final String requestMethods; // 由methods转变而来

    protected final T target;

    private Set<ReqMethod> resourceMethods;

    public AbstractMapping(String path, T target) {
        this(path, new ReqMethod[] { ReqMethod.ALL }, target);
    }

    public AbstractMapping(String path, ReqMethod[] methods, T target) {
        this.path = normalized(path);
        this.target = target;
        this.methods = methods;
        this.requestMethods = initMethods(methods);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public T getTarget() {
        return target;
    }

    public boolean isRequestMethodSupported(String requestMethod) {
        if (!requestMethods.equals("*")) {
            requestMethod = requestMethod.toUpperCase();
            if (requestMethods.indexOf(requestMethod) == -1) {
                return false;
            }
        }
        return true;
    }

    public boolean isMethodReplicated(AbstractMapping<?> obj) {
        String[] methods = new String[] { "GET", "POST", "DELETE", "OPTIONS", "PUT", "HEAD" };
        for (String method : methods) {
            if (this.isRequestMethodSupported(method) && obj.isRequestMethodSupported(method)) {
                return true;
            }
        }
        return false;
    }

    protected String normalized(String path) {
        if (path.length() > 0 && path.charAt(0) != '/') {
            path = '/' + path;
        }
        if (path.equals("/")) {
            path = "";
        }
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    protected String initMethods(ReqMethod[] methods) {
        if (methods.length == 0) {
            return "";
        }
        if (ArrayUtils.contains(methods, ReqMethod.ALL)) {
            return "*";
        }
        String requestMethods = "";
        for (int i = 0; i < methods.length; i++) {
            if (i == 0) {
                requestMethods = methods[i].toString();
            } else {
                requestMethods += "," + methods[i].toString().toUpperCase();
            }
        }
        return requestMethods;
    }

    public ReqMethod[] getMethods() {
        return Arrays.copyOf(methods, methods.length);
    }

    @Override
    public Set<ReqMethod> getResourceMethods() {
        return resourceMethods;
    }

    public void setResourceMethods(Set<ReqMethod> resourceMethods) {
        this.resourceMethods = resourceMethods;
    }

    protected int compareMethods(AbstractMapping<?> so) {
        boolean e = "*".equals(this.requestMethods);
        boolean n = "*".equals(so.requestMethods);
        if (e && n) {
            return 0;
        } else if (!e && !n) {
            return isMethodReplicated(so) ? 0 : this.requestMethods.length()
                    - so.requestMethods.length();
        } else {
            return e ? 1 : -1;
        }
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public String toString() {
        return path + "->" + target;
    }

}
