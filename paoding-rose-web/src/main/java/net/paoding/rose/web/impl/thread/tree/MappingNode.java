package net.paoding.rose.web.impl.thread.tree;

import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.thread.Engine;
import net.paoding.rose.web.impl.thread.MatchResult;

public class MappingNode {

    //    public MappingNode() {
    //    }

    public MappingNode(Mapping<? extends Engine> mapping, MappingNode parent) {
        if (mapping == null) {
            throw new NullPointerException("mapping");
        }
        this.mapping = mapping;
        this.parent = parent;
    }

    public Mapping<? extends Engine> mapping;

    public MappingNode parent;

    public MappingNode leftMostChild;

    public MappingNode sibling;

    public MatchResult<? extends Engine> match(String path, String method) {
        return mapping.match(path, method, this);
    }
}
