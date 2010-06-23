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
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.web.impl.mapping;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import net.paoding.rose.web.RequestPath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link MappingNode}代表匹配树的一个结点，树的结点能够包含一个或多个被称为资源的 {@link EngineGroup} 对象
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class MappingNode implements Comparable<MappingNode> {

    protected static final Log logger = LogFactory.getLog(MappingNode.class);

    /** 所使用的映射 */
    private final Mapping mapping;

    /** 最左子结点 */
    private MappingNode leftMostChild;

    /** 右兄弟结点 */
    private MappingNode sibling;

    /** 叶子引擎: 只有含有叶子引擎的结点才能处理对应地址的请求 */
    private final EngineGroup leafEngines = new EngineGroupImpl();

    /** 中间引擎： */
    private final EngineGroup middleEngines = new EngineGroupImpl();

    /**
     * 
     * @param mapping
     */
    public MappingNode(Mapping mapping) {
        this.mapping = mapping;
        this.mapping.setMappingNode(this);
    }

    public Mapping getMapping() {
        return mapping;
    }

    public String getMappingPath() {
        return this.mapping.getDefinition();
    }

    public MappingNode getLeftMostChild() {
        return leftMostChild;
    }

    public MappingNode getSibling() {
        return sibling;
    }

    public final boolean isLeaf() {
        return leftMostChild == null;
    }

    public void linkAsChild(final MappingNode child) {
        if (this.leftMostChild == null) {
            this.leftMostChild = child;
        } else {
            MappingNode prev = null;
            MappingNode position = this.leftMostChild;
            while (true) {
                if (position == null) {
                    prev.sibling = child;
                    break;
                }
                if (child.getMapping().compareTo(position.getMapping()) < 0) {
                    child.sibling = position;
                    if (prev == null) {
                        this.leftMostChild = child;
                    } else {
                        prev.sibling = child;
                    }
                    break;
                } else {
                    prev = position;
                    position = position.sibling;
                }
            }
        }
    }

    public MappingNode getChild(String mapping) {
        MappingNode sibling = this.leftMostChild;
        while (sibling != null) {
            if (sibling.getMapping().getDefinition().equals(mapping)) {
                return sibling;
            } else {
                sibling = sibling.sibling;
            }
        }
        return null;
    }

    public EngineGroup getLeafEngines() {
        return leafEngines;
    }

    public EngineGroup getMiddleEngines() {
        return middleEngines;
    }

    public ArrayList<MatchResult> match(HttpServletRequest request, RequestPath requestPath) {

        // 用来储存并返回的匹配结果集合
        ArrayList<MatchResult> matchResults = new ArrayList<MatchResult>(16);

        final boolean debugEnabled = logger.isDebugEnabled();

        // 当前判断结点
        MappingNode curNode = this;

        // 给当前判断结点判断的path
        String remaining = requestPath.getRosePath();

        // mark代表一个已经匹配了/结尾的路径了，但是还想去看看有没有更能处理/结尾的，在去看看之前做一个记号，万一不成功要退回来。
        // int mark = -1;

        // 最后一次匹配结果
        MatchResult last = null;

        // 开始匹配，直至成功或失败
        while (true) {
            //
            if (remaining.length() == 0 && curNode != this) {
                /*if (mark >= 0 && last.getMappingNode().getLeafEngines().size() == 0) {
                    // block a
                    while (mark < matchResults.size()) {
                        matchResults.remove(matchResults.size() - 1);
                    }
                }
                */
                if (debugEnabled) {
                    logger.debug("['" + requestPath.getRosePath() + "'] matched over.");
                }
                return matchResults;
            }
            /*
             if (remaining.equals("/")) {
                mark = matchResults.size();
            }*/
            if (curNode == null) {
                /*if (mark >= 0) {
                    // block b=block a
                    while (mark < matchResults.size()) {
                        matchResults.remove(matchResults.size() - 1);
                    }
                    if (debugEnabled) {
                        logger.debug("['" + requestPath.getRosePath() + "'] matched over.");
                    }
                    return matchResults;
                }*/
                if (debugEnabled) {
                    logger.debug("['" + requestPath.getRosePath() + "'] not matched");
                }
                return null;
            }
            // 当前结点的匹配结果result: 如果能够匹配path成功，一定会返回一个非空的result
            // 一旦result非空，这个请求只能在这个结点中处理了，不可能再由其它结点处理，
            // 即，如果因为某些原因本结点无法处理此请求，可以直接得出结论：这个请求不能被处理了
            last = curNode.getMapping().match(remaining);

            // 当前结点打不赢 
            if (last == null) {
                // 兄弟，你上!
                curNode = curNode.sibling;
                continue;
            }

            if (debugEnabled) {
                logger.debug("['" + requestPath.getRosePath() + "'] matched(" //
                        + (matchResults.size() + 1) + "): '" + last.getValue() + "'");
            }

            // add to results for return
            matchResults.add(last);
            int len = last.getValue().length();
            if (len == 0 && curNode != this) {
                throw new IllegalArgumentException("empty got by mapping " + curNode.getMapping());
            }
            remaining = remaining.substring(len);
            curNode = curNode.leftMostChild;
        }
    }

    @Override
    public int compareTo(MappingNode target) {
        return this.getMapping().compareTo(target.getMapping());
    }

    /**
     * 
     */
    public void destroy() {
        this.leafEngines.destroy();
        MappingNode c = leftMostChild;
        while (c != null) {
            c.destroy();
            c = c.sibling;
        }
        this.middleEngines.destroy();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MappingNode)) {
            return false;
        }
        MappingNode target = (MappingNode) obj;
        return this.compareTo(target) == 0;
    }

    @Override
    public int hashCode() {
        return getMapping().hashCode();
    }

    @Override
    public String toString() {
        return getMappingPath();
    }

}
