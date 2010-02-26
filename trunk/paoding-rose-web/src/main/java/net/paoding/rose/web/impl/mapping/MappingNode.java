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

import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.annotation.ReqMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 构造一个树，树的结点是"地址-资源"映射，每个结点都能回答是否匹配一个字符串，每个匹配的结点都知道如何执行对该资源的操作
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class MappingNode implements Comparable<MappingNode>, Iterable<MappingNode> {

    protected static final Log logger = LogFactory.getLog(MappingNode.class);

    /** 所使用的“地址-资源”映射 */
    private Mapping mapping;

    /** 父结点 */
    private MappingNode parent;

    /** 父亲资源 */
    private WebResource parentResource;

    /** 最左子结点 */
    private MappingNode leftMostChild;

    /** 同级兄弟结点 */
    private MappingNode sibling;

    /** 后序遍历的后继结点 */
    private MappingNode successor;

    private static final WebResource[] EMPTY = new WebResource[0];

    private WebResource[] resources = EMPTY;

    private transient String pathCache;

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
    public MappingNode(Mapping mapping, MappingNode parent, WebResource parentResource) {
        if (mapping == null) {
            throw new NullPointerException("mapping");
        }
        this.setMapping(mapping);
        this.setParent(parent);
        if (parent != null) {
            assert parentResource != null;
            this.parentResource = parentResource;
        }
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

    public WebResource getParentResource() {
        return parentResource;
    }

    public MappingNode getSibling() {
        return sibling;
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

    public WebResource[] getResources() {
        return resources;
    }

    public void setResources(WebResource[] resources) {
        this.resources = resources;
    }

    public void addResource(WebResource resource) {
        assert resource != null;
        if (resources.length == 0) {
            resources = new WebResource[] { resource };
        } else {
            resources = Arrays.copyOf(resources, resources.length + 1);
            resources[resources.length - 1] = resource;
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
            newNode.parentResource = child.parentResource;
            newNode.resources = Arrays.copyOf(child.resources, child.resources.length);
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
                    assert mappingNode == newNode;
                }

                @Override
                public MatchResult match(String path) {
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

    public ArrayList<MatchResult> match(RequestPath requestPath) {
        String rosePath = requestPath.getRosePath();
        String path = rosePath;
        ArrayList<MatchResult> matchResults = new ArrayList<MatchResult>(4);

        MappingNode cur = this;
        MatchResult mrIngoresRequestMethod = null;
        while (true) {
            MatchResult mr = cur.getMapping().match(path);
            if (mr != null) {
                // 设置上一级的resource [因上级node如果包含了多个resource当时留空]
                if (matchResults.size() > 0) {
                    MatchResult prev = matchResults.get(matchResults.size() - 1);
                    if (prev.getResource() == null) {
                        prev.setResource(cur.getParentResource());
                    }
                }
                if (cur.leftMostChild == null) {
                    mrIngoresRequestMethod = mr;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("matching [" + (matchResults.size() + 1) + "] '" + path
                            + "'; resource=" + mr.getResource());
                }
            }
            if (mr == null
                    || (mr.getResource() != null && !mr.getResource().isMethodAllowed(
                            requestPath.getMethod()))) {
                if (cur.sibling != null) {
                    cur = cur.sibling;
                } else {
                    while (true) {
                        MatchResult last = matchResults.size() == 0 ? null : matchResults
                                .get(matchResults.size() - 1);
                        if (last != null) {
                            if (last.getValue().length() > 0) {
                                path = last.getValue() + path;
                            }
                        }
                        if (matchResults.size() > 0) {
                            matchResults.remove(matchResults.size() - 1);
                            logger.debug("backward");
                        }
                        cur = cur.parent;
                        if (cur == null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("not matched: " + rosePath);
                            }
                            if (mrIngoresRequestMethod != null) {
                                matchResults.add(mrIngoresRequestMethod);
                            }
                            return matchResults;
                        } else {
                            if (cur.sibling != null) {
                                cur = cur.sibling;
                                break;
                            }
                        }
                    }
                }
            } else {
                matchResults.add(mr);
                logger.debug("forward");
                path = path.substring(mr.getValue().length());
                if (cur.leftMostChild != null) {
                    cur = cur.leftMostChild;
                } else {
                    if (logger.isDebugEnabled()) {
                        // FIXME: mr.getResource可能为null，需要做什么特别标注没？
                        logger.debug("matched '" + rosePath + "': target=" + mr.getResource());
                    }
                    return matchResults;
                }
            }
        }
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
