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
import net.paoding.rose.web.impl.mapping.MappingNode;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.thread.AfterCompletion;
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

    private ArrayList<MatchResult<? extends Engine>> matchResults;

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

    public List<MatchResult<? extends Engine>> getMatchResults() {
        return matchResults;
    }

    public boolean execute() throws Throwable {
        ArrayList<MatchResult<? extends Engine>> matchResults = mappingTree.match(invocation
                .getRequestPath());
        if (matchResults.size() == 0) {
            return false;
        }
        MatchResult<?> mr = matchResults.get(matchResults.size() - 1);
        if (!mr.isLeaf()) {
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
            this.matchResults = matchResults;
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

    private LinkedList<AfterCompletion> afterCompletions = new LinkedList<AfterCompletion>();

    @Override
    public void addAfterCompletion(AfterCompletion task) {
        afterCompletions.addFirst(task);
    }
}
