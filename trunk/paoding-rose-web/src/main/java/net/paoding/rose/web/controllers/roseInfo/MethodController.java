/*
 * Copyright 2007-2010 the original author or authors.
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
package net.paoding.rose.web.controllers.roseInfo;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import net.paoding.rose.web.InterceptorDelegate;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.impl.mapping.EngineGroup;
import net.paoding.rose.web.impl.mapping.MappingNode;
import net.paoding.rose.web.impl.mapping.MatchResult;
import net.paoding.rose.web.impl.thread.ActionEngine;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.impl.thread.LinkedEngine;

public class MethodController {

    @Get
    public Object x(Invocation inv) {
        String target = inv.getRequest().getQueryString();
        InvocationBean invb = (InvocationBean) inv;
        MappingNode tree = invb.getRose().getMappingTree();
        ReqMethod method = inv.getRequestPath().getMethod();
        RequestPath requestPath = new RequestPath(method, inv.getRequestPath().getCtxpath(),
                target, inv.getRequestPath().getDispatcher());
        ArrayList<MatchResult> matchResults = tree.match(requestPath);
        //
        if (matchResults == null) {
            // not rose uri
            return ("@404: not rose uri: '" + target + "'");
        }

        final MatchResult lastMatched = matchResults.get(matchResults.size() - 1);
        final EngineGroup leafEngineGroup = lastMatched.getMappingNode().getLeafEngines();
        final LinkedEngine leafEngine = select(leafEngineGroup.getEngines(method), inv.getRequest());
        if (leafEngine == null) {
            if (leafEngineGroup.size() == 0) {
                // not rose uri
                return ("@404: not rose uri, not exits leaf engines for it: '" + target + "'");

            } else {
                // 405 Method Not Allowed
                StringBuilder allow = new StringBuilder();
                final String gap = ", ";

                for (ReqMethod m : leafEngineGroup.getAllowedMethods()) {
                    allow.append(m.toString()).append(gap);
                }
                if (allow.length() > 0) {
                    allow.setLength(allow.length() - gap.length());
                }

                // true: don't forward to next filter or servlet
                return "@405; allowed=" + allow.toString();
            }
        }

        StringBuilder sb = new StringBuilder();
        ActionEngine actionEngine = (ActionEngine) leafEngine.getTarget();
        sb.append("200; mapped '" + target + "' to " + actionEngine.getControllerClass().getName()
                + "#" + actionEngine.getMethod().getName());

        sb.append("<br>intectptors:");
        for (InterceptorDelegate i : actionEngine.getRegisteredInterceptors()) {
            sb.append("<br>").append(i.getName()).append("=").append(
                    InterceptorDelegate.getMostInnerInterceptor(i).getClass().getName()).append(
                    "(p=").append(i.getPriority()).append(")");
        }

        return sb;
    }

    private LinkedEngine select(LinkedEngine[] engines, HttpServletRequest request) {
        LinkedEngine selectedEngine = null;
        int score = 0;

        for (LinkedEngine engine : engines) {
            int candidate = engine.isAccepted(request);
            if (candidate > score) {
                selectedEngine = engine;
                score = candidate;
            }
        }
        return selectedEngine;
    }
}
