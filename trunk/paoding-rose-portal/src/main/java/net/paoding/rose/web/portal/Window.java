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

import java.util.concurrent.Future;


/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface Window {

    public Portal getPortal();

    public Future<?> getFuture();

    public void set(String key, Object value);

    public Object get(String key);

    public void setTitle(Object title);

    public Object getTitle();

    public String getContent();

    public boolean isDone();

    public String getName();

    public String getPath();

    public Throwable getThrowable();

    public void setThrowable(Throwable e);

    public int getStatusCode();

    public String getStatusMessage();

    public Window forRender(boolean forRender);

    public boolean isForRender();

    public String toString();

}
