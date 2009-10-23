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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class InvocationUtils {

    // 存放当前线程所处理的请求对象 
    private final static ThreadLocal<HttpServletRequest> currentRequests = new ThreadLocal<HttpServletRequest>();

    //
    public static void bindInvocationToRequest(Invocation inv, HttpServletRequestWrapper request) {
        request.setAttribute("$$paoding-rose.invocation", inv);
    }

    public static Invocation getInvocation(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return (Invocation) request.getAttribute("$$paoding-rose.invocation");
    }

    public static void unindRequestFromCurrentThread() {
        currentRequests.remove();
    }

    public static void bindRequestToCurrentThread(HttpServletRequest request) {
        if (request == null) {
            currentRequests.remove();
        } else {
            currentRequests.set(request);
        }
    }

    public static HttpServletRequest getCurrentThreadRequest() {
        return currentRequests.get();
    }

    public static Invocation getCurrentThreadInvocation() {
        return getInvocation(currentRequests.get());
    }
}
