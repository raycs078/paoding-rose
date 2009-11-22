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

/**
 * {@link Mapping}用于封装地址到资源的一个映射，给定一个地址字符串，它能够判断其是否匹配。
 * <p>
 * 
 * {@link Mapping}对象是有序的，对给定的一个地址，排序在前的 {@link Mapping} 匹配成功后，整个匹配过程将中止。
 * <p>
 * 
 * {@link Mapping}对象的排序比较只和定义该映射的地址有关，和所绑定的 {@link WebResource} 无关。不同的
 * {@link Mapping}实现都支持之间的互相比较。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface Mapping extends Comparable<Mapping> {

    /**
     * 返回规范化后的的地址定义,如: <br>
     * /blog/$userId-$blogId/list，/application/$appName
     * 
     * @return
     */
    public String getPath();

    /**
     * 返回该地址所映射的资源，不同的地址所代表的资源不一样，此处将返回不同的资源。
     * 
     * @return
     */
    public WebResource getResource();

    /**
     * 判断给定的请求的地址<code>path</code>是否能够和本 {@link Mapping}对象相匹配。
     * <p>
     * 若能够匹配，返回非null的 {@link MatchResult}对象，并把该映射的资源绑定到匹配结果中返回。
     * 
     * @param path
     * @return
     */
    public MatchResult match(String path);

    /**
     * 返回该映射的地址定义以及匹配规则(比如正则表达式)
     * 
     * @return
     */
    public String toString();
}
