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
 * {@link Window}封装了一个portal下的窗口信息，包括该窗口的名字、请求地址以及处理完后最终的响应状态和页面文本内容。
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface Window {

    /**
     * 该窗口所属的portal对象，如果一个窗口要把自己的数据“透露”给其他窗口，必须通过 Portal 对象实现
     * 
     * @return
     */
    public Portal getPortal();

    /**
     * 返回该窗口请求任务的future对象，通常Portal控制器应该不需要主动使用这个方法，但在必要的情况下，
     * 可用于在portal控制器中调用它的get方法进行等待，阻塞portal控制器线程直至该窗口的请求被处理完成。
     * 
     * @return
     */
    public Future<?> getFuture();

    /**
     * 判断该窗口请求是否被处理完成了，无论是否发生了异常或因超时被取消。
     * 
     * @see Future#isDone()
     * @return
     */
    public boolean isDone();

    /**
     * 返回该窗口请求的响应状态或错误状态。如果一个请求被正确地、如期望地处理，将返回200
     * 
     * @return
     */
    public int getStatusCode();

    /**
     * 这个窗口是否已经处理完了，并且是如期望被处理的，根据业务需要，可以根据此方法的返回结果判断是否在portal页中显示该窗口的内容
     * <p>
     * <p>
     * 所谓如期望指的是：找到控制器以及方法处理、处理没有发送异常、响应状态为200.
     * 
     * @return
     */
    public boolean isSuccess();

    /**
     * 返回创建该窗口时所设置的名字，这个窗口对象将以这个名字设置在所在portal的model中
     * 
     * @see Portal#addWindow(String, String)
     * @return
     */
    public String getName();

    /**
     * 返回创建该窗口时所设置的地址，这个窗口将代表这个地址的资源/动作。
     * 
     * @see Portal#addWindow(String, String)
     * @return
     */
    public String getPath();

    /**
     * 返回该窗口的渲染结果文本，如果没有被渲染将返回null
     * 
     * @return
     */
    public String getContent();

    /**
     * 返回该窗口的渲染结果的文本大小，如果没有被渲染将返回0 (空串也返回0)
     * 
     * @return
     */
    public int getContentLength();

    /**
     * 设置窗口属性，使 {@link Window#get(String)}方法能够获取他
     * 
     * @param key
     * @param value
     */
    public void set(String key, Object value);

    /**
     * 获取设置到窗口的属性
     * 
     * @param key
     * @return
     */
    public Object get(String key);

    /**
     * 设置窗口的Title属性，实现上应保证 也设置一份到 {@link #set(String, Object)}
     * 
     * @param title
     */
    public void setTitle(Object title);

    /**
     * 返回该窗口的的Title属性
     * 
     * @return
     */
    public Object getTitle();

    /**
     * 返回执行该窗口时的程序异常对象，如果有的话。
     * 
     * @return
     */
    public Throwable getThrowable();

    /**
     * 返回该窗口请求的错误信息
     * 
     * @return
     */
    public String getStatusMessage();

    /**
     * 返回该窗口的渲染结果文本，使在视图中直接使用 ${name} 即可渲染出该内容。
     * 
     * @return
     */
    public String toString();

}
