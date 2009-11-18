package net.paoding.rose.web.impl.mapping;

import java.util.ArrayList;

import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.impl.thread.Engine;
import net.paoding.rose.web.impl.thread.MatchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MappingNode {

    protected static final Log logger = LogFactory.getLog(MappingNode.class);

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

    public ArrayList<MatchResult<? extends Engine>> match(RequestPath requestPath) {
        String rosePath = requestPath.getRosePath();
        String method = requestPath.getMethod();
        String path = rosePath;
        ArrayList<MatchResult<? extends Engine>> matchResults = new ArrayList<MatchResult<? extends Engine>>(
                4);

        MappingNode cur = this;
        MatchResult<? extends Engine> mrIngoresRequestMethod = null;
        while (true) {
            MatchResult<? extends Engine> mr = cur.mapping.match(path, method);
            if (mr != null) {
                mr.setNode(cur);
                if (cur.leftMostChild == null) {
                    mrIngoresRequestMethod = mr;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("matching [" + (matchResults.size() + 1) + "] '" + path
                            + "': rule='" + mr.getNode().mapping.getPath() + "'; target="
                            + mr.getNode().mapping.getTarget());
                }
            }
            if (mr == null || !mr.isRequestMethodSupported()) {
                if (cur.sibling != null) {
                    cur = cur.sibling;
                } else {
                    while (true) {
                        MatchResult<? extends Engine> last = matchResults.size() == 0 ? null
                                : matchResults.get(matchResults.size() - 1);
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
                    logger.debug("matched '" + rosePath + "': target="
                            + mr.getNode().mapping.getTarget());
                    return matchResults;
                }
            }
        }
    }

}
