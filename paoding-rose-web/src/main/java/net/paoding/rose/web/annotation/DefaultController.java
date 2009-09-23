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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 将 {@link DefaultController}标注在控制器上，这个控制器将是所在模块的默认控制器。
 * <p>
 * 当HTTP请求没有指定哪个控制器时，此时请求将被指派到这个默认的控制器处理
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DefaultController {
}
