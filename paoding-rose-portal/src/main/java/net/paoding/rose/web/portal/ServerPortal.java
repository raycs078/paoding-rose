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
package net.paoding.rose.web.portal;

import javax.servlet.http.HttpServletRequest;

/**
 * 一个 {@link ServerPortal} 对象邦定在 {@link HttpServletRequest} 之上，提供了 portal
 * 框架的编程接口。通过多次调用 {@link #addWindow(String, String)} 接口来为一个 portal 增加窗口。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface ServerPortal extends Portal {

}
