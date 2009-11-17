package net.paoding.rose.web.impl.thread.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.InvocationUtils;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.thread.Engine;
import net.paoding.rose.web.impl.thread.EngineChain;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.impl.thread.MatchResult;
import net.paoding.rose.web.impl.thread.ParameteredUriRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Rose implements EngineChain {

    protected static final Log logger = LogFactory.getLog(Rose.class);

    private List<Module> modules;

    private InvocationBean invocation;

    private ArrayList<MatchResult<? extends Engine>> matchResults = new ArrayList<MatchResult<? extends Engine>>(
            4);

    private MappingNode mappingTree;

    private int nextIndexOfChain = 0;

    public Rose(List<Module> modules, MappingNode mappingTree, InvocationBean invocation) {
        this.mappingTree = mappingTree;
        this.modules = modules;
        this.invocation = invocation;
        invocation.setRose((Rose) this);
    }

    public InvocationBean getInvocation() {
        return invocation;
    }

    public List<Module> getModules() {
        return modules;
    }

    public boolean execute() throws Throwable {
        MatchResult<? extends Engine> mr = treeSearch();
        if (mr == null) {
            return false;
        }
        if (!mr.isRequestMethodSupported()) {
            /* 405 Method Not Allowed
             * The method specified in the Request-Line is not allowed for the
             * resource identified by the Request-URI. The response MUST include an
             * Allow header containing a list of valid methods for the requested
             * resource.
             */
            HttpServletResponse response = invocation.getResponse();
            Set<ReqMethod> methods = mr.getMapping().getResourceMethods();
            String allow = "";
            for (ReqMethod method : methods) {
                if (allow.length() > 0) {
                    allow = ", " + method.toString();
                } else {
                    allow = method.toString();
                }
            }
            response.addHeader("Allow", allow);
            response.sendError(405, invocation.getRequestPath().getUri());
        } else {
            Map<String, String> mrParameters = null;
            for (int i = 0; i < this.matchResults.size(); i++) {
                MatchResult<?> tmr = this.matchResults.get(i);
                if (tmr.getParameterCount() > 0) {
                    if (mrParameters == null) {
                        mrParameters = new HashMap<String, String>(6);
                    }
                    for (String name : tmr.getParameterNames()) {
                        mrParameters.put(name, tmr.getParameter(name));
                    }
                }
            }
            if (mrParameters != null && mrParameters.size() > 0) {
                invocation.setRequest(new ParameteredUriRequest(invocation.getRequest(),
                        mrParameters));
            }
            InvocationUtils.bindRequestToCurrentThread(invocation.getRequest());
            // invoke the engine chain
            Throwable error = null;
            try {
                ((EngineChain) this).invokeNext((Rose) this, null);
            } catch (Throwable local) {
                error = local;
                throw local;
            } finally {
                for (AfterCompletion task : afterCompletions) {
                    try {
                        task.afterCompletion(invocation, error);
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public Object invokeNext(Rose rose, Object instruction) throws Throwable {
        if (nextIndexOfChain >= matchResults.size()) {
            return instruction;
        }
        MatchResult<? extends Engine> mr = matchResults.get(nextIndexOfChain++);
        Engine engine = mr.getMapping().getTarget();
        return engine.invoke(rose, mr, instruction, (EngineChain) this);
    }

    /*
     * 树的深度遍历：
     * 
     */
    private MatchResult<? extends Engine> treeSearch() {
        String path = invocation.getRequestPath().getRosePath();
        String method = invocation.getRequestPath().getMethod();
        MappingNode cur = mappingTree;
        MatchResult<? extends Engine> mrIngoresRequestMethod = null;
        while (true) {
            MatchResult<? extends Engine> mr = cur.match(path, method);
            if (mr != null && cur.leftMostChild == null) {
                mrIngoresRequestMethod = mr;
            }
            if (logger.isDebugEnabled() && mr != null) {
                logger.debug("searching '" + path + "': rule=" + mr.getNode().mapping.getPath()
                        + "; target=" + mr.getNode().mapping.getTarget());
            }
            if (mr == null || !mr.isRequestMethodSupported()) {
                if (cur.sibling != null) {
                    cur = cur.sibling;
                } else {
                    while (true) {
                        MatchResult<? extends Engine> last = lastMatcheResult();
                        if (last != null) {
                            if (last.getMatchedString().length() > 0) {
                                path = last.getMatchedString() + path;
                            }
                        }
                        backward();
                        cur = cur.parent;
                        if (cur == null) {
                            return mrIngoresRequestMethod;
                        } else {
                            if (cur.sibling != null) {
                                cur = cur.sibling;
                                break;
                            }
                        }
                    }
                }
            } else {
                forward(mr);
                path = path.substring(mr.getMatchedString().length());
                if (cur.leftMostChild != null) {
                    cur = cur.leftMostChild;
                } else {
                    return mr;
                }
            }
        }
    }

    private void forward(MatchResult<? extends Engine> result) {
        this.matchResults.add(result);
    }

    private void backward() {
        if (matchResults.size() > 0) {
            this.matchResults.remove(matchResults.size() - 1);
        }
    }

    public MatchResult<? extends Engine> lastMatcheResult() {
        return matchResults.size() == 0 ? null : matchResults.get(matchResults.size() - 1);
    }

    private LinkedList<AfterCompletion> afterCompletions = new LinkedList<AfterCompletion>();

    @Override
    public void addAfterCompletion(AfterCompletion task) {
        afterCompletions.addFirst(task);
    }
}
