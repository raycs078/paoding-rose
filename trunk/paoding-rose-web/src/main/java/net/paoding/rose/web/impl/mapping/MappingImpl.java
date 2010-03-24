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

import static net.paoding.rose.util.PlaceHolderUtils.DOLLAR;
import static net.paoding.rose.util.PlaceHolderUtils.PLACEHOLDER_INNER_PREFIX;
import static net.paoding.rose.util.PlaceHolderUtils.PLACEHOLDER_SUFFIX_CHAR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.paoding.rose.util.Empty;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link MappingImpl}实现了使用<strong>正则表达式</strong>定义匹配字符串的 {@link Mapping}
 * <p>
 * 所给的地址字符串中被$xxx (或${xxx}、{xxx})包围的都表示要使用正则表达式替代。<br>
 * 默认使用的正则表达式使用的是"([^/]+)"，即除'/'外的任何字符。<br>
 * 自定义正则表达式通过冒号来定义，例如：{xxx:[0-9]+}，表示使用的正则表达式将是[0-9]+
 * <p>
 * 为了方便， {@link MappingImpl} 定义了一些正则快捷表述方式:
 * 
 * <pre>
 *     快捷方式    所代表的正则表达式
 *      id         [0-9a-zA-Z_-]+ 
 *     number     [0-9]+
 *     n          [0-9]+
 *     word       \\w+
 *     w          \\w+
 *     .          .*
 *     +          .+
 *     ?          .?
 * </pre>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class MappingImpl implements Mapping {

    private static final Log logger = LogFactory.getLog(MappingImpl.class);

    /** 为定义正则表达式参数所使用的规则 */
    private static final String DEFAULT_REGEX = "([^/]+)";

    /** 规范化的地址定义 */
    private String path;

    /** 映射规则 */
    private MappingPattern mappingPattern;

    /** 地址定义中包含的常量 */
    private String[] constants = Empty.STRING_ARRAY;

    /** 地址定义中的参数名 */
    private String[] paramNames = Empty.STRING_ARRAY;

    /** 该映射代表的结点 */
    private MappingNode mappingNode;

    public MappingImpl(String path, MatchMode mode) {
        this(path, mode, null);
    }

    public MappingImpl(String path, MatchMode mode, MappingNode mappingNode) {
        this.path = normalized(path);
        initPattern(mode);
        this.mappingNode = mappingNode;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public MappingNode getMappingNode() {
        return this.mappingNode;
    }

    public void setMappingNode(MappingNode mappingNode) {
        this.mappingNode = mappingNode;
    }

    public String[] getConstants() {
        return Arrays.copyOf(this.constants, constants.length);
    }

    public String[] getParamNames() {
        return Arrays.copyOf(this.paramNames, paramNames.length);
    }

    /**
     * 返回Mapping地址中含有的常量字符串数，如:<br>
     * /blog/$userId-$blogId/list的常量字符串是："/blog/"、"-"、"/list"，数目是3<br>
     * /application/$appName的常量字符串是："/applicaiton/"、""，数目是2
     * 
     * @return
     */
    public int getConstantCount() {
        return this.constants.length;
    }

    /**
     * 返回Mapping地址中含有的参数字符串数，如：<br>
     * /blog/$userId-$blogId/list的常量字符串是："userId"、"blogId"，数目是2<br>
     * /application/$appName的常量字符串是："appName"，数目是1
     * 
     * @return
     */
    public int getParameterCount() {
        return this.paramNames.length;
    }

    /**
     * 越特殊的地址比越普遍的地址要求排序在前，compare的值要为负
     */
    @Override
    public int compareTo(Mapping o) {
        if (!(o instanceof MappingImpl)) {
            return -o.compareTo(this);
        }
        MappingImpl pm = (MappingImpl) o;
        if (this.path.equals(pm.path)) {
            return 0;
        }
        // /user排在/{id}前面
        // /user_{id}排在/user前面
        // /user_排在/user_{id}前面
        // ab{id}排在a{id}前面
        for (int i = 0; i < constants.length; i++) {
            if (pm.constants.length <= i) {
                return 1;
            }
            if (this.constants[i].equals(pm.constants[i])) {
                continue;
            }
            if (this.constants[i].length() == 0) {
                return 1;
            } else if (this.constants[i].startsWith(pm.constants[i])) {
                return -1;
            } else if (pm.constants[i].startsWith(this.constants[i])) {
                return 1;
            } else {
                return this.constants[i].compareTo(pm.constants[i]);
            }
        }
        return Integer.signum(this.paramNames.length - pm.paramNames.length);
    }

    @Override
    public MatchResult match(String path) {
        java.util.regex.MatchResult regexMatchResult = mappingPattern.match(path);
        if (regexMatchResult == null) {
            return null;
        }
        String value = regexMatchResult.group(0);
        while (value.length() > 0 && value.charAt(value.length() - 1) == '/') {
            value = value.substring(0, value.length() - 1);
        }
        MatchResultImpl mr;
        if (mappingNode != null) {
            WebResource[] resources = mappingNode.getResources();
            mr = new MatchResultImpl(value, resources.length == 1 ? resources[0] : null);
        } else {
            mr = new MatchResultImpl(value, null);
        }
        if (paramNames.length != 0) {
            for (int i = 0; i < this.paramNames.length; i++) {
                mr.putParameter(paramNames[i], regexMatchResult.group(i + 1));
            }
        }
        return mr;
    }

    protected String normalized(String path) {
        if (path.length() > 0 && path.charAt(0) != '/') {
            path = '/' + path;
        }
        if (path.equals("/")) {
            path = "";
        }
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    protected void initPattern(MatchMode mode) {
        ArrayList<String> paramNames = new ArrayList<String>();
        int nextIndex = 0;
        String placeHolderPrefix = "$";
        int startIndex = path.indexOf(placeHolderPrefix);
        if (startIndex >= 0) {
            if (path.charAt(startIndex + 1) == '{') {
                placeHolderPrefix = "${";
            }
        }
        String _placeHolderPrefix = "{";
        int _startIndex = path.indexOf("{");
        if (startIndex < 0 || (_startIndex >= 0 && _startIndex < startIndex)) {
            startIndex = _startIndex;
            placeHolderPrefix = _placeHolderPrefix;
        }
        //
        List<String> constants = new ArrayList<String>(2);
        if (startIndex == -1) {
            this.mappingPattern = mode.compile(this.path, false);
        } else {
            int paramIndex = 1; // 记录参数的位置，从0开始，用于匿名参数转化为param1的形式
            StringBuilder buf = new StringBuilder(path);
            while (startIndex != -1) {
                int endIndex = -1;
                int cc = 0;
                for (int i = startIndex + placeHolderPrefix.length(); i < buf.length(); i++) {
                    char ch = buf.charAt(i);
                    if (ch == PLACEHOLDER_INNER_PREFIX) {
                        cc++;
                    } else if (ch == PLACEHOLDER_SUFFIX_CHAR) {
                        cc--;
                        if (cc < 0 && placeHolderPrefix != DOLLAR) {
                            endIndex = i;
                            break;
                        }
                    } else if (ch == '/' && placeHolderPrefix == DOLLAR) {
                        endIndex = i;
                        break;
                    }
                }
                if (endIndex == -1 && placeHolderPrefix == DOLLAR) {
                    endIndex = buf.length();
                }
                if (endIndex != -1) {
                    String paramName = null;
                    String regex = DEFAULT_REGEX;
                    for (int i = startIndex + placeHolderPrefix.length(); i < endIndex; i++) {
                        if (buf.charAt(i) == ':') {
                            paramName = buf.substring(startIndex + placeHolderPrefix.length(), i);
                            regex = buf.substring(i + 1, endIndex);
                            if (regex.length() == 0) {
                                regex = DEFAULT_REGEX;
                            } else if ("+".equals(regex)) {
                                regex = "(.+)";
                            } else if ("?".equals(regex)) {
                                regex = "(.?)";
                            } else if ("*".equals(regex)) {
                                regex = "(.*)";
                            } else if ("n".equals(regex) || "number".equals(regex)) {
                                regex = "([0-9]+)";
                            } else if ("w".equals(regex) || "word".equals(regex)) {
                                regex = "(\\w+)";
                            } else if ("id".equals(regex)) {
                                regex = "([0-9a-zA-Z_-]+)";
                            } else if (regex.charAt(0) != '(') {
                                regex = '(' + regex + ')';
                            }
                            break;
                        }
                    }
                    if (paramName == null) {
                        paramName = buf
                                .substring(startIndex + placeHolderPrefix.length(), endIndex);
                    }
                    if (paramName.length() == 0) {
                        paramName = "param" + paramIndex;
                    } else if (NumberUtils.isDigits(paramName)) {
                        paramName = "$" + paramName;
                    }
                    //
                    constants.add(buf.substring(nextIndex, startIndex));
                    paramNames.add(paramName);
                    int suffiexLen = placeHolderPrefix == DOLLAR ? 0 : 1;
                    buf.replace(startIndex, endIndex + suffiexLen, regex);
                    //
                    nextIndex = startIndex + regex.length();
                    //
                    placeHolderPrefix = "$";
                    startIndex = buf.indexOf(placeHolderPrefix, nextIndex);
                    if (startIndex >= 0) {
                        if (buf.charAt(startIndex + 1) == '{') {
                            placeHolderPrefix = "${";
                        }
                    }
                    _placeHolderPrefix = "{";
                    _startIndex = buf.indexOf("{", nextIndex);
                    if (startIndex < 0 || (_startIndex >= 0 && _startIndex < startIndex)) {
                        startIndex = _startIndex;
                        placeHolderPrefix = _placeHolderPrefix;
                    }
                    //
                    paramIndex++;
                } else {
                    startIndex = -1;
                }
            }

            // 最后的constaint, 可能为空串
            constants.add(buf.substring(nextIndex));
            this.paramNames = paramNames.toArray(new String[paramNames.size()]);
            this.mappingPattern = mode.compile(buf.toString(), true);
        }
        if (constants.size() == 0) {
            constants.add(path);
        }
        this.constants = constants.toArray(new String[constants.size()]);
        if (logger.isDebugEnabled()) {
            logger.debug("mapping: path=" + this.path + "; pattern=" + this.mappingPattern
                    + "; params=" + Arrays.toString(this.paramNames) + "; constants="
                    + Arrays.toString(this.constants));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Mapping)) {
            return false;
        }
        return this.compareTo((Mapping) obj) == 0;
    }

    @Override
    public int hashCode() {
        int result = 17;
        for (String constant : constants) {
            result += 31 * constant.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.constants.length; i++) {
            sb.append(constants[i]);
            if (i < this.paramNames.length) {
                sb.append("${").append(this.paramNames[i]).append("}");
            }
        }
        sb.append("[regex=").append(mappingPattern).append("]");
        return sb.toString();
    }

}
