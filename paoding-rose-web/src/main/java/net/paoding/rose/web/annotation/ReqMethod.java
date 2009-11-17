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
package net.paoding.rose.web.annotation;

/**
 * 用于辅助设置一个控制器action方法映射，声明只有指定的http请求方法才由 {@link ReqMapping}作标注的action方法处理
 * 
 * @see ReqMapping
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public enum ReqMethod {

    ALL, GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE;

    public static ReqMethod map(String method) {
        method = method.toUpperCase();
        if ("*".equals(method)) {
            return ALL;
        }
        if ("GET".equals(method)) {
            return GET;
        }
        if ("HEAD".equals(method)) {
            return HEAD;
        }
        if ("POST".equals(method)) {
            return POST;
        }
        if ("PUT".equals(method)) {
            return PUT;
        }
        if ("DELETE".equals(method)) {
            return DELETE;
        }
        if ("OPTIONS".equals(method)) {
            return OPTIONS;
        }
        if ("TRACE".equals(method)) {
            return TRACE;
        }
        throw new IllegalArgumentException(method);
    }

}
