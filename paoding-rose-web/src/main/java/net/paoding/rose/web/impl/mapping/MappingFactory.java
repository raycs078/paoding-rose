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
package net.paoding.rose.web.impl.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class MappingFactory {

    // ad${da}afd
    // $xxxx$yyyy
    // {}        
    public static List<Mapping> parse(String path) {

        if (path.length() == 0) {
            return Collections.emptyList();
        }

        List<Mapping> mappings = new ArrayList<Mapping>(8);
        if (path.charAt(0) != '/') {
            path = "/" + path;
        }

        char[] chars = new char[path.length()];
        path.getChars(0, path.length(), chars, 0);
        int paramBegin = -1;
        int paramNameEnd = -1;
        int constantBegin = -1;
        boolean inScope = false; // true表示要把{}视为普通括号，而非param声明
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '$':
                    if (i + 1 >= chars.length) {
                        throw new IllegalArgumentException(//
                                "invalid string '" + path + "', don't end with '$'");
                    }
                    // 针对constains$name的"constains"的
                    if (constantBegin >= 0) {
                        mappings.add(createConstantMapping(path, constantBegin, i));
                        constantBegin = -1;
                    }
                    // 针对$name$xyz的"$name"的
                    if (paramBegin >= 0) {
                        mappings.add(createRegexMapping(path, paramBegin, paramNameEnd, i));
                        paramBegin = -1;
                        paramNameEnd = -1;
                    }
                    if (chars[i + 1] != '{') {
                        paramBegin = i + 1;
                    }
                    break;
                case '{':
                    if (paramBegin < 0) {
                        paramBegin = i + 1;
                        inScope = true;
                    }
                    // 针对$name{xyz}的"$name"的
                    else {
                        mappings.add(createRegexMapping(path, paramBegin, paramNameEnd, i));
                        paramBegin = -1;
                        paramNameEnd = -1;
                    }
                    // 针对constains{name}的"constains"的
                    if (constantBegin >= 0) {
                        mappings.add(createConstantMapping(path, constantBegin, i));
                        constantBegin = -1;
                    }
                    break;
                case ':':
                    if (paramBegin < 0) {
                        throw new IllegalArgumentException(//
                                "invalid string '" + path + "', wrong ':' at position " + i);
                    } else if (paramNameEnd > 0) {
                        throw new IllegalArgumentException(//
                                "invalid string '" + path + "', duplicated ':' at position " + i);
                    } else {
                        paramNameEnd = i;
                    }
                    break;
                case '}':
                    if (paramBegin < 0) {
                        throw new IllegalArgumentException(//
                                "invalid string '" + path + "', wrong '}' at position " + i);
                    } else {
                        mappings.add(createRegexMapping(path, paramBegin, paramNameEnd, i));
                        inScope = false;
                        paramBegin = -1;
                        paramNameEnd = -1;
                    }
                    break;
                case '/':
                    if (paramBegin < 0) {
                        if (constantBegin >= 0) {
                            mappings.add(createConstantMapping(path, constantBegin, i));
                        }
                        constantBegin = i;
                        break;
                    } else if (!inScope) {
                        mappings.add(createRegexMapping(path, paramBegin, paramNameEnd, i));
                        paramBegin = -1;
                        constantBegin = i;
                        paramNameEnd = -1;
                        break;
                    }

                default:
                    if (constantBegin == -1 && paramBegin == -1) {
                        constantBegin = i;
                    }
                    break;
            }
        }
        if (constantBegin >= 0) {
            mappings.add(createConstantMapping(path, constantBegin, chars.length));
            constantBegin = -1;
        }
        if (paramBegin >= 0) {
            mappings.add(createRegexMapping(path, paramBegin, paramNameEnd, chars.length));
            paramBegin = -1;
            paramNameEnd = -1;
        }
        return mappings;
    }

    private static Mapping createConstantMapping(//
            String userDefinedMapping, int constantBegin, int i) {
        if (constantBegin == i) {
            throw new IllegalArgumentException(userDefinedMapping + "  constantBegin="
                    + constantBegin);
        }
        return new ConstantMapping(userDefinedMapping.substring(constantBegin, i));
    }

    private static Mapping createRegexMapping(//
            String userDefinedMapping, int paramBegin, int paramNameEnd, int i) {
        final String rawName = userDefinedMapping.substring(paramBegin,
                paramNameEnd >= 0 ? paramNameEnd : i);
        if (rawName.length() == 0) {
            throw new IllegalArgumentException(//
                    "invalid string '" + userDefinedMapping + "', wrong paramName at position "
                            + paramBegin);
        }
        String name = rawName;
        if (org.apache.commons.lang.math.NumberUtils.isDigits(rawName)) {
            name = "$" + rawName;
        }
        String regex = "";
        if (paramNameEnd >= 0) {
            regex = userDefinedMapping.substring(paramNameEnd + 1, i);
        }
        return new RegexMapping("{" + rawName + "}", name, regex);
    }

}
