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

import java.util.Set;

import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.thread.MatchResult;
import net.paoding.rose.web.impl.thread.tree.MappingNode;

/**
 * {@link Mapping}用于封装表示一个地址到一个目标的映射。
 * <p>
 * 
 * {@link Mapping}对象是有序的，框架将正序优先使用匹配成功的{@link Mapping}
 * 对象。(但框架没有保证匹配判断一定是按顺序的)
 * <p>
 * 
 * {@link Mapping}对象的排序比较和目标对象无关，只应关心地址串和请求方法。不同的 {@link Mapping}
 * 实现的对象要能够互相比较。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 * @param <T> 映射的目标
 * @see ReqMapping
 */
public interface Mapping<T> extends Comparable<Mapping<?>> {

    /**
     * 返回定义给该匹配的地址字符串
     * 
     * @return
     */
    public String getPath();

    /**
     * 
     * @return
     */
    public int getConstantCount();

    /**
     * 
     * @return
     */
    public int getParameterCount();

    /**
     * 返回定义给该匹配的请求方法
     * 
     * @return
     */
    public ReqMethod[] getMethods();

    // TODO: 应该抽象为一个对象处理
    public Set<ReqMethod> getResourceMethods();

    /**
     * 返回该匹配的目标对象
     * 
     * @return
     */
    public T getTarget();

    /**
     * 判断给定的请求的地址<code>path</code>以及请求方法是否能够和本 {@link Mapping}对象相匹配。
     * <p>
     * 若能够匹配，返回非null的 {@link MatchResult}对象；否则返回null。
     * 
     * @param path
     * @param requestMethod 请求的方法，应为大写
     * @return
     */
    public MatchResult<T> match(String path, String requestMethod, MappingNode node);
}
