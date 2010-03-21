/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.jadeinterface.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 用此{@link Dao}注解标注在一个符合Jade编写规范的DAO接口类上，明确标注这是Jade DAO接口。
 * 
 * <p>
 * 被Jade识别的DAO接口必须符合3个原则：<br>
 * <ul>
 * <li>DAO接口类声明在dao包下，即yourcampany.yourapp.dao的package下或子package下；</li>
 * <li>DAO接口名称必须以DAO或Dao结尾；</li>
 * <li>DAO接口必须标注{@link Dao}注解；</li>
 * </ul>
 * <p>
 * 
 * 如果DAO接口被打包成为一个jar的，为了要被Jade识别，必须在这个jar的MANIFEST.MF文件中包含"Rose: DAO"的标识(
 * 标识Rose: *亦可以，但建议开发者们精确标志DAO而非*，这有助于提高启动速度)。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Dao {

    /**
     * 指定此所标注的 DAO 所要使用的数据源名称。
     * 
     * @return 使用的数据源
     */
    String catalog();
}
