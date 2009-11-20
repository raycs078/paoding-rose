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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.resource.WebResource;
import net.paoding.rose.web.impl.thread.MatchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 构造一个树，树的结点是资源，每个结点都能回答是否匹配一个字符串，每个匹配的节点都知道如何执行对该资源的操作
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class MappingNode implements Comparable<MappingNode> {

    protected static final Log logger = LogFactory.getLog(MappingNode.class);

    public MappingNode(Mapping mapping, WebResource resource) {
        this(mapping, resource, null);
    }

    public MappingNode(Mapping mapping, WebResource resource, MappingNode parent) {
        if (mapping == null) {
            throw new NullPointerException("mapping");
        }
        this.mapping = mapping;
        this.resource = resource;
        this.setParent(parent);
    }

    public Mapping mapping;

    public MappingNode parent;

    public MappingNode leftMostChild;

    public MappingNode sibling;

    public WebResource resource;

    public WebResource getResource() {
        return resource;
    }

    public void sorts() {
        List<MappingNode> children = new LinkedList<MappingNode>();
        MappingNode child = this.leftMostChild;
        while (child != null) {
            children.add(child);
            child = child.sibling;
        }
        Collections.sort(children);
        this.leftMostChild = null;
        MappingNode lastMappingNode = null;
        for (MappingNode mappingNode : children) {
            if (this.leftMostChild == null) {
                this.leftMostChild = mappingNode;
            } else {
                lastMappingNode.sibling = mappingNode;
            }
            lastMappingNode = mappingNode;
        }
        if (lastMappingNode != null) {
            lastMappingNode.sibling = null;
        }
        child = this.leftMostChild;
        while (child != null) {
            child.sorts();
            child = child.sibling;
        }
    }

    public void copyTo(MappingNode parent, List<ReqMethod> filter) {
        MappingNode child = this.leftMostChild;
        while (child != null) {
            WebResource resouce = new WebResource(parent.resource, child.resource.getName());
            for (ReqMethod reqMethod : child.resource.getAllowedMethods()) {
                if (filter.contains(ReqMethod.ALL) || filter.contains(reqMethod)) {
                    resouce.addEngine(reqMethod, child.resource.getEngine(reqMethod));
                }
            }
            MappingNode copied = new MappingNode(child.mapping, resouce, parent);
            child.copyTo(copied, filter);
            child = child.sibling;
        }
    }

    public void setParent(MappingNode parentNode) {
        this.parent = parentNode;
        if (this.parent != null) {
            if (parentNode.leftMostChild == null) {
                parentNode.leftMostChild = this;
            } else {
                this.sibling = parentNode.leftMostChild;
                parentNode.leftMostChild = this;
            }
        }
    }

    public MappingNode getChild(Mapping mapping) {
        MappingNode sibling = this.leftMostChild;
        while (sibling != null) {
            if (sibling.mapping.equals(mapping)) {
                return sibling;
            } else {
                sibling = sibling.sibling;
            }
        }
        return null;
    }

    public ArrayList<MatchResult> matches(RequestPath requestPath) {
        String rosePath = requestPath.getRosePath();
        String path = rosePath;
        ArrayList<MatchResult> matchResults = new ArrayList<MatchResult>(4);

        MappingNode cur = this;
        MatchResult mrIngoresRequestMethod = null;
        while (true) {
            MatchResult mr = cur.mapping.match(path);
            if (mr != null) {
                mr.setResource(cur.resource);
                if (cur.leftMostChild == null) {
                    mrIngoresRequestMethod = mr;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("matching [" + (matchResults.size() + 1) + "] '" + path
                            + "': rule='" + mr.getMapping().getPath() + "; resource="
                            + mr.getResource());
                }
            }
            if (mr == null || !mr.isMethodAllowed(requestPath.getMethod())) {
                if (cur.sibling != null) {
                    cur = cur.sibling;
                } else {
                    while (true) {
                        MatchResult last = matchResults.size() == 0 ? null : matchResults
                                .get(matchResults.size() - 1);
                        if (last != null) {
                            if (last.getMatchedString().length() > 0) {
                                path = last.getMatchedString() + path;
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
                path = path.substring(mr.getMatchedString().length());
                if (cur.leftMostChild != null) {
                    cur = cur.leftMostChild;
                } else {
                    logger.debug("matched '" + rosePath + "': target=" + mr.getResource());
                    return matchResults;
                }
            }
        }
    }

    @Override
    public int compareTo(MappingNode target) {
        return this.mapping.compareTo(target.mapping);
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
        return mapping.hashCode();
    }

}
