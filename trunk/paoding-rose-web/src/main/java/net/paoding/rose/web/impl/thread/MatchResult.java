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
package net.paoding.rose.web.impl.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.thread.tree.MappingNode;

/**
 * 控制器的action path参数映射结果
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class MatchResult<T> {

    private String matchedString;

    private Properties properties = new Properties();

    private ArrayList<String> names = new ArrayList<String>(2);

    private Mapping<T> mapping;

    private MappingNode node;

    private boolean requestMethodSupported = true;

    protected MatchResult() {
    }

    public MatchResult(String matchedString, Mapping<T> mapping, MappingNode node) {
        this.matchedString = matchedString;
        this.mapping = mapping;
        this.node = node;
    }

    public MappingNode getNode() {
        return node;
    }

    public static <T> MatchResult<T> changeMapping(MatchResult<?> src, Mapping<T> newMapping) {
        if (src == null) {
            return null;
        }
        MatchResult<T> ret = new MatchResult<T>(src.getMatchedString(), newMapping, src.node);
        ret.names = src.names;
        ret.properties = src.properties;
        return ret;
    }

    public static <T> MatchResult<T> unmodifiable(String matchedString, Mapping<T> mapping, MappingNode node) {
        return new MatchResult<T>(matchedString, mapping, node) {

            @Override
            public void setMapping(Mapping<T> mapping) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setMatchedString(String matchedString) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putParameter(String name, String value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * 
     * 
     * @param name
     * @param value
     */
    public void putParameter(String name, String value) {
        properties.put(name, value);
        names.add(name);
    }

    public List<String> getParameterNames() {
        return names;
    }

    public String getParameter(String name) {
        return properties.getProperty(name);
    }

    /**
     * @param position 该参数是所有参数中从左到右的第几个; 0,1,2,3,...(从0开始)
     * @return
     */
    public String getParameter(int position) {
        return position < names.size() ? getParameter(names.get(position)) : null;
    }

    public int getParameterCount() {
        return names.size();
    }

    public void setMatchedString(String matchedString) {
        this.matchedString = matchedString;
    }

    public String getMatchedString() {
        return matchedString;
    }

    public void setMapping(Mapping<T> mapping) {
        this.mapping = mapping;
    }

    public Mapping<T> getMapping() {
        return mapping;
    }

    public void setRequestMethodSupported(boolean requestMethodSupported) {
        this.requestMethodSupported = requestMethodSupported;
    }

    public boolean isRequestMethodSupported() {
        return requestMethodSupported;
    }

}
