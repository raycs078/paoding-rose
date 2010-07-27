/*
 * Copyright 2007-2010 the original author or authors.
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

import java.io.IOException;

import net.paoding.rose.web.portal.Window;

public interface Pipe {

    public void register(Window window);

    public void setup() throws IOException;

    // 实现上要注意：并发控制的问题；另外一定要在主portal已经flush的情况下才能真正输出Window
    // 要把window的内容加工为script
    public void fire(Window window);

    public void await(long timeout);

    public void close();

}
