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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.RoseEngine;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationUtils;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.validation.ParameterBindingResult;
import net.paoding.rose.web.var.Flash;
import net.paoding.rose.web.var.FlashImpl;
import net.paoding.rose.web.var.Model;
import net.paoding.rose.web.var.ModelImpl;
import net.paoding.rose.web.var.PrivateVar;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public final class InvocationBean implements Invocation {

    public static final Object[] UN_INITIATED_ARRAY = new Object[0];

    private static final Log logger = LogFactory.getLog(InvocationBean.class);

    private RoseEngine roseEngine;

    private Object[] methodParameters = UN_INITIATED_ARRAY; // 在还没有设置方法参数进来时为UN_INITIATED_ARRAY

    private Map<String, Object> attributes;

    private Map<String, Object> oncePerRequestAttributes;

    private HttpServletRequestWrapper request;

    private HttpServletResponse response;

    private RequestPath requestPath;

    private transient Model model;

    private transient Flash flash;

    private MatchResult<ModuleEngine> moduleMatchResult;

    private MatchResult<ControllerEngine> controllerMatchResult;

    private MatchResult<ActionEngine> actionMatchResult;

    private Map<String, Object> requestAttributesBeforeInclude;

    private Invocation preInvocation;

    private boolean multiPartRequest;

    private int executedInterceptorIndex = -1;

    private BitSet executedInterceptorBitSet;

    private List<BindingResult> bindingResults;

    private List<String> bindingResultNames;

    //    private boolean destroyed;

    public InvocationBean() {
    }

    public void setRoseEngine(RoseEngine roseEngine) {
        this.roseEngine = roseEngine;
    }

    public RoseEngine getRoseEngine() {
        return roseEngine;
    }

    protected boolean isMethodParametersInitiated() {
        return methodParameters != UN_INITIATED_ARRAY;
    }

    @Override
    public WebApplicationContext getApplicationContext() {
        return PrivateVar.getWebApplicationContext(getModule().getMappingPath());
    }

    public void setMethodParameters(Object[] methodParameters) {
        this.methodParameters = methodParameters;
    }

    @Override
    public Object getController() {
        return getControllerEngine().getController();
    }

    @Override
    public Class<?> getControllerClass() {
        return getControllerEngine().getControllerClass();
    }

    @Override
    public Method getMethod() {
        return getActionEngine().getMethod();
    }

    @Override
    public String[] getMethodParameterNames() {
        String[] copy = new String[getActionEngine().getParameterNames().length];
        System.arraycopy(getActionEngine().getParameterNames(), 0, copy, 0, copy.length);
        return copy;
    }

    @Override
    public Object[] getMethodParameters() {
        return methodParameters;
    }

    @Override
    public Object getMethodParameter(String name) {
        if (!isMethodParametersInitiated()) {
            throw new IllegalStateException();
        }
        String[] names = getActionEngine().getParameterNames();
        for (int i = 0; i < names.length; i++) {
            if (name != null && name.equals(names[i])) {
                return methodParameters[i];
            }
        }
        return null;
    }

    @Override
    public Object getParameter(String name) {
        Object value = null;
        if (isMethodParametersInitiated()) {
            value = getMethodParameter(name);
        }
        if (value == null && !ArrayUtils.contains(getActionEngine().getParameterNames(), name)) {
            value = getRawParameter(name);
        }
        return value;
    }

    @Override
    public String getRawParameter(String name) {
        String value = actionMatchResult == null ? null : actionMatchResult.getParameter(name);
        if (value == null) {
            value = controllerMatchResult == null ? null : controllerMatchResult.getParameter(name);
        }
        if (value == null) {
            value = moduleMatchResult == null ? null : moduleMatchResult.getParameter(name);
        }
        if (value == null) {
            value = request.getParameter(name);
        }
        return value;
    }

    @Override
    public void changeMethodParameter(int index, Object value) {
        if (!isMethodParametersInitiated()) {
            throw new IllegalStateException();
        }
        if (value != this.methodParameters[index]) {
            if (logger.isDebugEnabled()) {
                logger.debug("change method parameter "
                        + this.getActionEngine().getParameterNames()[index] + " (index=" + index
                        + ") from '" + this.methodParameters[index] + "' to '" + value + "'");
            }
            Object oldValue = this.methodParameters[index];
            this.methodParameters[index] = value;
            if (logger.isDebugEnabled()) {
                logger.debug("change method parameter at " + index//
                        + ": " + oldValue + "->" + value);
            }
        }
    }

    public void changeMethodParameter(String name, Object value) {
        if (!isMethodParametersInitiated()) {
            throw new IllegalStateException();
        }
        if (StringUtils.isEmpty(name)) {
            throw new NullPointerException("parameter name");
        }
        String[] names = this.getActionEngine().getParameterNames();
        for (int i = 0; i < names.length; i++) {
            if (name.equals(names[i])) {
                changeMethodParameter(i, value);
                return;
            }
        }
    }

    public String getMatchResultParameter(String name) {
        return getMatchResultParameter(name, null);
    }

    public String getMatchResultParameter(String name, String defValue) {
        String[] values = getMatchResultParameterValues(name);
        return values.length == 0 ? defValue : values[0];
    }

    public String[] getMatchResultParameterValues(String name) {
        String[] values = new String[3];
        values[0] = actionMatchResult == null ? null : actionMatchResult.getParameter(name);
        values[1] = controllerMatchResult == null ? null : controllerMatchResult.getParameter(name);
        values[2] = moduleMatchResult == null ? null : moduleMatchResult.getParameter(name);
        int count = 0;
        for (String value : values) {
            if (value != null) {
                count++;
            }
        }
        String[] ret = new String[count];
        count = 0;
        for (String value : values) {
            if (value != null) {
                ret[count++] = value;
            }
        }
        return ret;
    }

    public MatchResult<ModuleEngine> getModuleMatchResult() {
        return moduleMatchResult;
    }

    public void setModuleMatchResult(MatchResult<ModuleEngine> moduleMatchResult) {
        this.moduleMatchResult = moduleMatchResult;
    }

    public MatchResult<?> getControllerMatchResult() {
        return controllerMatchResult;
    }

    public void setControllerMatchResult(MatchResult<ControllerEngine> controllerMatchResult) {
        this.controllerMatchResult = controllerMatchResult;
    }

    public MatchResult<?> getActionMatchResult() {
        return actionMatchResult;
    }

    public void setActionMatchResult(MatchResult<ActionEngine> actionMatchResult) {
        this.actionMatchResult = actionMatchResult;
    }

    @Override
    public void addModel(Object value) {
        getModel().add(value);
    }

    @Override
    public void addModel(String name, Object value) {
        getModel().add(name, value);
    }

    @Override
    public Model getModel() {
        if (this.model != null) {
            return this.model;
        }
        synchronized (this) {
            ModelImpl model = (ModelImpl) getRequest().getAttribute("$$paoding-rose.model");
            if (model == null || model.getInvocation() != this) {
                Model parent = model;
                model = new ModelImpl(this);
                if (parent != null && requestPath.isForwardRequest()) {
                    synchronized (parent) {
                        model.merge(parent.getAttributes());
                    }
                }
                getRequest().setAttribute("$$paoding-rose.model", model);
            }
            this.model = model;
        }
        return this.model;
    }

    @Override
    public synchronized Object getAttribute(String name) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(name);
    }

    @Override
    public synchronized void removeAttribute(String name) {
        if (attributes != null) {
            attributes.remove(name);
        }
    }

    @Override
    public synchronized Set<String> getAttributeNames() {
        if (attributes == null) {
            return Collections.emptySet();
        }
        return attributes.keySet();
    }

    @Override
    public synchronized Invocation setAttribute(String name, Object value) {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        attributes.put(name, value);
        return this;
    }

    @Override
    public synchronized Object getOncePerRequestAttribute(String name) {
        if (preInvocation != null) {
            return preInvocation.getOncePerRequestAttribute(name);
        } else {
            return oncePerRequestAttributes == null ? null : oncePerRequestAttributes.get(name);
        }
    }

    @Override
    public synchronized Invocation setOncePerRequestAttribute(String name, Object value) {
        if (preInvocation != null) {
            preInvocation.getOncePerRequestAttribute(name);
        } else {
            if (oncePerRequestAttributes == null) {
                oncePerRequestAttributes = new HashMap<String, Object>();
            }
            oncePerRequestAttributes.put(name, value);
        }
        return this;
    }

    @Override
    public void addFlash(String name, String msg) {
        getFlash(true).add(name, msg);
    }

    @Override
    public Flash getFlash() {
        return getFlash(true);
    }

    public Flash getFlash(boolean create) {
        if (this.flash != null) {
            return this.flash;
        }
        Flash flash = (Flash) getRequest().getAttribute("$$paoding-rose.flash");
        if (flash == null && create) {
            flash = new FlashImpl(this);
            getRequest().setAttribute("$$paoding-rose.flash", flash);
        }
        return this.flash = flash;
    }

    @Override
    public RequestPath getRequestPath() {
        return requestPath;
    }

    @Override
    public HttpServletRequest getRequest() {
        return (HttpServletRequest) (request == null ? null : request.getRequest());
    }

    @Override
    public HttpServletResponse getResponse() {
        return response;
    }

    public ControllerEngine getControllerEngine() {
        return getActionEngine().getControllerEngine();
    }

    public ActionEngine getActionEngine() {
        if (actionMatchResult == null) {
            throw new NullPointerException("actionMatchResult");
        }
        return actionMatchResult.getMapping().getTarget();
    }

    public List<String> getMatchResultParameterNames() {
        int count = moduleMatchResult.getParameterCount()
                + controllerMatchResult.getParameterCount() + actionMatchResult.getParameterCount();
        if (count == 0) {
            return Collections.emptyList();
        }
        ArrayList<String> parameterNames = new ArrayList<String>(count);
        parameterNames.addAll(moduleMatchResult.getParameterNames());
        parameterNames.addAll(controllerMatchResult.getParameterNames());
        parameterNames.addAll(actionMatchResult.getParameterNames());
        return parameterNames;
    }

    public void setRequest(HttpServletRequest request) {
        if (request == null) {
            throw new NullPointerException("request");
        }
        if (request == this.request) {
            return;
        }
        if (this.request == null) {
            this.request = new HttpServletRequestWrapper(request);
            InvocationUtils.bindInvocationToRequest(this, this.request);
        } else {
            this.request.setRequest(request);
        }
    }

    @Override
    public ServletContext getServletContext() {
        return PrivateVar.servletContext();
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public void setRequestPath(RequestPath requestPath) {
        this.requestPath = requestPath;
    }

    public Module getModule() {
        return this.moduleMatchResult.getMapping().getTarget().getModule();
    }

    public Map<String, Object> getRequestAttributesBeforeInclude() {
        return requestAttributesBeforeInclude;
    }

    public void setRequestAttributesBeforeInclude(Map<String, Object> attributesSnapshot) {
        this.requestAttributesBeforeInclude = attributesSnapshot;
    }

    public Invocation getPreInvocation() {
        return preInvocation;
    }

    public void setPreInvocation(Invocation preInvocation) {
        this.preInvocation = preInvocation;
    }

    public void setMultiPartRequest(boolean multiPartRequest) {
        this.multiPartRequest = multiPartRequest;
    }

    public boolean isMultiPartRequest() {
        return multiPartRequest;
    }

    public void setExecutedInterceptorIndex(int executedInterceptorIndex) {
        this.executedInterceptorIndex = executedInterceptorIndex;
    }

    public int getExecutedInterceptorIndex() {
        return executedInterceptorIndex;
    }

    public BitSet getExecutedInterceptorBitSet() {
        return executedInterceptorBitSet;
    }

    public void setExecutedInterceptorBitSet(BitSet executedInterceptorBitSet) {
        this.executedInterceptorBitSet = executedInterceptorBitSet;
    }

    @Override
    public List<BindingResult> getBindingResults() {
        fetchBindingResults();
        return this.bindingResults;
    }

    @Override
    public List<String> getBindingResultNames() {
        fetchBindingResults();
        return this.bindingResultNames;
    }

    @Override
    public BindingResult getParameterBindingResult() {
        return getBindingResult(getController());
    }

    @Override
    public BindingResult getBindingResult(Object bean) {
        Assert.notNull(bean);
        if (bean instanceof String) {
            if (!((String) bean).endsWith("BindingResult")) {
                bean = bean + "BindingResult";
            }
            return (BindingResult) this.getModel().get(BindingResult.MODEL_KEY_PREFIX + bean);
        } else {
            if (this.getController() == bean) {
                return (BindingResult) this.getModel().get(
                        BindingResult.MODEL_KEY_PREFIX + ParameterBindingResult.OBJECT_NAME);
            }
            Object[] params = methodParameters;
            String[] names = getActionEngine().getParameterNames();
            for (int i = 0; i < params.length; i++) {
                if (bean.equals(params[i])) {
                    return getBindingResult(names[i]);
                }
            }
        }
        return null;
    }

    public ModuleEngine getModuleEngine() {
        if (moduleMatchResult == null) {
            throw new NullPointerException("moduleMatchResult");
        }
        return moduleMatchResult.getMapping().getTarget();
    }

    //    @Override
    //    public boolean isDestroyed() {
    //        return destroyed;
    //    }
    //
    //    public void destroy() {
    //        destroyed = true;
    //        if (this.request == InvocationUtils.getCurrentThreadRequest()) {
    //            InvocationUtils.bindRequestToCurrentThread(null);
    //        }
    //    }

    protected void fetchBindingResults() {
        if (this.bindingResults == null) {
            Map<String, Object> attributes = getModel().getAttributes();
            List<String> bindingResultNames = new ArrayList<String>();
            List<BindingResult> bindingResults = new ArrayList<BindingResult>();
            for (String key : attributes.keySet()) {
                if (key.startsWith(BindingResult.MODEL_KEY_PREFIX)) {
                    Object value = attributes.get(key);
                    if (value instanceof BindingResult) {
                        bindingResults.add((BindingResult) value);
                        bindingResultNames.add(((BindingResult) value).getObjectName());
                    }
                }
            }
            this.bindingResults = Collections.unmodifiableList(bindingResults);
            this.bindingResultNames = Collections.unmodifiableList(bindingResultNames);
        }
    }

    @Override
    public String toString() {
        return requestPath.getUri();
    }

}
