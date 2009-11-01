/*
 * $Id$
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
package net.paoding.rose.web.paramresolver;

/**
 * 
 * 请改为实现 {@link ParamResolver}，并调整方法的参数。新的接口 {@link ParamResolver}
 * 将原方法中的众多参数封装到 {@link ParamMetaData} .
 * <p>
 * 本类将在2009年12月前删除。
 * 
 * @deprecated
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
// TODO: 2009年12月前删除此类
public interface ParamResolverBean extends ParamResolver {
}
