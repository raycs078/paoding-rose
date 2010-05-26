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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.thread.Engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * {@link MappingNode}代表匹配树的一个结点，树的结点能够包含一个或多个被称为资源的 {@link EngineGroup} 对象
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class MappingNode implements Comparable<MappingNode>, Iterable<MappingNode> {

    protected static final Log logger = LogFactory.getLog(MappingNode.class);

    /** 所使用的映射 */
    private Mapping mapping;

    /** 父结点 */
    private MappingNode parent;

    /** 父engine group */
    private EngineGroup parentEngineGroup;

    /** 最左子结点 */
    private MappingNode leftMostChild;

    /** 右兄弟结点 */
    private MappingNode sibling;

    /** 后序遍历的后继结点 */
    private MappingNode successor;

    private static final EngineGroup[] EMPTY = new EngineGroup[0];

    private EngineGroup[] engineGroups = EMPTY;

    private transient String pathCache;

    private int deep;

    /**
     * 
     * @param mapping
     */
    public MappingNode(Mapping mapping) {
        this(mapping, null, null);
    }

    private MappingNode() {

    }

    /**
     * 
     * @param mapping
     * @param parent
     */
    public MappingNode(Mapping mapping, MappingNode parent, EngineGroup parentEngines) {
        if (mapping == null) {
            throw new NullPointerException("mapping");
        }
        this.setMapping(mapping);
        this.setParent(parent);
        if (parent != null) {
            Assert.notNull(parentEngines);
            this.parentEngineGroup = parentEngines;
        }
    }

    public int getDeep() {
        return deep;
    }

    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
        this.mapping.setMappingNode(this);
    }

    public Mapping getMapping() {
        return mapping;
    }

    public String getPath() {
        if (pathCache == null) {
            if (parent == null) {
                pathCache = this.mapping.getPath();
            } else {
                pathCache = parent.getPath() + this.mapping.getPath();
            }
        }
        return pathCache;
    }

    public MappingNode getLeftMostChild() {
        return leftMostChild;
    }

    public MappingNode getParent() {
        return parent;
    }

    public MappingNode getSibling() {
        return sibling;
    }

    public final boolean isLeaf() {
        return leftMostChild == null;
    }

    /**
     * 后序遍历
     */
    @Override
    public Iterator<MappingNode> iterator() {
        return new Iterator<MappingNode>() {

            private boolean end = false;

            MappingNode endNode = MappingNode.this;

            MappingNode next = getThroughtStart();

            @Override
            public boolean hasNext() {
                return !end;
            }

            @Override
            public MappingNode next() {
                MappingNode returned = next;
                next = next.successor;
                if (returned == endNode) {
                    end = true;
                }
                return returned;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }

        };
    }

    public EngineGroup[] getEngineGroups() {
        return engineGroups;
    }

    public void setEngineGroups(EngineGroup[] engineGroups) {
        this.engineGroups = engineGroups;
    }

    public void addEngineGroup(EngineGroup engineGroup) {
        Assert.isTrue(engineGroup != null);
        if (engineGroups.length == 0) {
            engineGroups = new EngineGroup[] { engineGroup };
        } else {
            engineGroups = Arrays.copyOf(engineGroups, engineGroups.length + 1);
            engineGroups[engineGroups.length - 1] = engineGroup;
        }
    }

    /**
     * 
     * @param parent
     * @param filter
     */
    void copyChildrenTo(MappingNode parent, List<ReqMethod> filter) {
        MappingNode child = this.leftMostChild;
        while (child != null) {
            final Mapping toCopy = child.getMapping();
            final MappingNode newNode = new MappingNode();
            newNode.parentEngineGroup = child.parentEngineGroup;
            newNode.engineGroups = Arrays.copyOf(child.engineGroups, child.engineGroups.length);
            Mapping copiedMapping = new Mapping() {

                @Override
                public String getPath() {
                    return toCopy.getPath();
                }

                @Override
                public MappingNode getMappingNode() {
                    return newNode;
                }

                @Override
                public void setMappingNode(MappingNode mappingNode) {
                    Assert.isTrue(mappingNode == newNode);
                }

                @Override
                public MatchResult match(CharSequence path) {
                    return toCopy.match(path);
                }

                @Override
                public int compareTo(Mapping o) {
                    return toCopy.compareTo(o);
                }

            };
            newNode.setMapping(copiedMapping);
            newNode.setParent(parent);
            child.copyChildrenTo(newNode, filter);
            child = child.sibling;
        }
    }

    private MappingNode getThroughtStart() {
        MappingNode node = this;
        while (node.leftMostChild != null) {
            node = node.leftMostChild;
        }
        return node;
    }

    public void setParent(MappingNode parentNode) {
        if (this.parent != null) {
            throw new IllegalStateException("the node's parent cann't be changed after setting.");
        }
        this.parent = parentNode;
        if (this.parent != null) {
            this.deep = this.parent.deep + 1;
        }
        if (this.parent != null) {
            //
            if (parentNode.leftMostChild == null) {
                // 后序遍历
                this.successor = parentNode;
                MappingNode n = getPredecessor(parentNode.getThroughtStart());
                if (n != null) n.successor = this.getThroughtStart();
                // 左儿子有兄弟
                parentNode.leftMostChild = this;
            } else if (this.compareTo(parentNode.leftMostChild) < 0) {
                // 后序遍历
                this.successor = parentNode.leftMostChild.getThroughtStart();
                MappingNode n = getPredecessor(parentNode.getThroughtStart());
                if (n != null) n.successor = this.getThroughtStart();
                // 左儿子有兄弟
                this.sibling = parentNode.leftMostChild;
                parentNode.leftMostChild = this;
            } else {
                MappingNode leftNode = parent.leftMostChild;
                while (leftNode.sibling != null && this.compareTo(leftNode.sibling) > 0) {
                    leftNode = leftNode.sibling;
                }
                // 后序遍历
                this.successor = leftNode.successor;
                leftNode.successor = this.getThroughtStart();
                // 左儿子有兄弟
                this.sibling = leftNode.sibling;
                leftNode.sibling = this;
            }

        }
    }

    /**
     * 返回后序遍历排在node的前驱
     * 
     * @param node
     * @return
     */
    private MappingNode getPredecessor(MappingNode node) {
        if (node.leftMostChild != null) {
            MappingNode t = node.leftMostChild;
            while (t.sibling != null) {
                t = t.sibling;
            }
            return t;
        }
        MappingNode _parent = node.parent;
        MappingNode _this = node;
        while (true) {
            if (_parent == null) {
                return null;
            }
            if (_parent.leftMostChild == _this) {
                _parent = _parent.parent;
                _this = _this.parent;
            } else {
                MappingNode n = _parent.leftMostChild;
                while (n.sibling != _this) {
                    n = n.sibling;
                }
                return n;
            }
        }
    }

    public MappingNode getChild(Mapping mapping) {
        MappingNode sibling = this.leftMostChild;
        while (sibling != null) {
            if (sibling.getMapping().compareTo(mapping) == 0) {
                return sibling;
            } else {
                sibling = sibling.sibling;
            }
        }
        return null;
    }

    public ArrayList<MatchResult> match(HttpServletRequest request, RequestPath requestPath) {

        // 用来储存并返回的匹配结果集合
        final ArrayList<MatchResult> matchResults = new ArrayList<MatchResult>(4);

        final boolean debugEnabled = logger.isDebugEnabled();

        // 当前判断结点
        MappingNode curNode = this;

        // 给当前判断结点判断的path
        String remaining = requestPath.getRosePath();

        // 开始匹配，直至成功或失败
        while (true) {

            // 当前结点的匹配结果result: 如果能够匹配path成功，一定会返回一个非空的result
            // 一旦result非空，这个请求只能在这个结点中处理了，不可能再由其它结点处理，
            // 即，如果因为某些原因本结点无法处理此请求，可以直接得出结论：这个请求不能被处理了
            final MatchResult result = curNode.getMapping().match(remaining);

            // 当前结点打不赢 
            if (result == null) {
                // 兄弟，你上!
                if (curNode.sibling != null) {
                    curNode = curNode.sibling;
                    continue;
                } else {
                    // 都牺牲了? 
                    return matchResults;
                }
            }

            if (debugEnabled) {
                logger.debug("['" + requestPath.getRosePath() + "'] matched(" //
                        + (matchResults.size() + 1) + "): '" + result.getValue() + "'");
            }

            // @label BIND_ENGINE_IMMEDIATELY

            if (curNode.getEngineGroups().length == 1) {
                // 处理当前请求的engineGroup对象: 因为同一个path可能有多个engineGroup可以提供服务，所以即使result非null，engineGroup也可能为null
                EngineGroup engineGroup = curNode.getEngineGroups()[0];
                // leaf结点才有设置allowedMethods的必要
                if (curNode.isLeaf()) {
                    result.setAllowedMethods(engineGroup.getAllowedMethods());
                }

                // 处理当前请求的engine对象: 一个engineGroup可以处理不同请求方法，GET和POST可能是由不同的engine来处理的
                Engine engine = getEngine(engineGroup, request, requestPath);

                // 既然当前就能知道engine是谁？那就直接设置给 result； 如果还不能知道，等等即可
                // @see BIND_PARENT_ENGINE

                if (engine != null) {
                    result.setEngine(engine);
                    if (debugEnabled) {
                        logger.debug("bind to " + engine.getClass().getSimpleName() + ": '"
                                + engine + "'");
                    }
                } else {
                    // 只有最后的结点（即方法结点）才有资格返回405
                    if (!curNode.isLeaf()) {
                        throw new Error("non-leaf nodes shall not deny request by http method.");
                    }
                }
            } else {
                Assert.isTrue(curNode.getEngineGroups().length > 0);
                if (curNode.isLeaf()) {
                    throw new AssertionError(
                            "leaf nodes should not have more than one engineGroup.");
                }
            }

            // @label BIND_PARENT_ENGINE

            // 上级匹配结果对象当时无法知道应该由哪个engineGroup处理? 现在可以知道了!
            if (matchResults.size() > 0) {
                MatchResult parentResult = matchResults.get(matchResults.size() - 1);
                if (parentResult.getEngine() == null) {
                    // 
                    Engine parentEngine = getEngine(curNode.parentEngineGroup, request, requestPath);

                    // 只有最后的结点才有资格在engineGroup非空的情况下拒绝服务
                    if (parentEngine == null) {
                        throw new AssertionError(
                                "non-leaf nodes shall not deny request by http method.");
                    }
                    parentResult.setEngine(parentEngine);
                }
            }

            // add to results for return
            matchResults.add(result);
            remaining = remaining.substring(result.getValue().length());

            if (!curNode.isLeaf()) {
                curNode = curNode.leftMostChild;
            } else {
                return matchResults;
            }

        }
    }

    private Engine getEngine(EngineGroup engineGroup, HttpServletRequest request,
            RequestPath requestPath) {

        Engine selectedEngine = null;
        int score = 0;

        for (Engine engine : engineGroup.getEngines(requestPath.getMethod())) {
            int candidate = engine.isAccepted(request);
            if (candidate > score) {
                selectedEngine = engine;
                score = candidate;
            } else if (candidate <= 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("['" + requestPath.getRosePath()
                            + "'] it's not accepted by engine: " + engine);
                }
            }
        }
        return selectedEngine;
    }

    @Override
    public int compareTo(MappingNode target) {
        return this.getMapping().compareTo(target.getMapping());
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
        return getPath();
    }

}
