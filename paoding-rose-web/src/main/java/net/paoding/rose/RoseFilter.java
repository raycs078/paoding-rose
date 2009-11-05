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
package net.paoding.rose;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.scanner.ModuleInfo;
import net.paoding.rose.scanner.RoseJarContextResources;
import net.paoding.rose.scanner.RoseModuleInfos;
import net.paoding.rose.web.ControllerInterceptor;
import net.paoding.rose.web.Dispatcher;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationUtils;
import net.paoding.rose.web.NamedValidator;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.impl.context.ContextLoader;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.module.ControllerInfo;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.module.ModulesBuilder;
import net.paoding.rose.web.impl.module.NestedControllerInterceptorWrapper;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.impl.thread.ParameteredUriRequest;
import net.paoding.rose.web.paramresolver.ParamResolver;
import net.paoding.rose.web.var.PrivateVar;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.WebUtils;

/**
 * Paoding Rose是一个MVC开发框架(这里的MVC概念更接近于Grails/RoR的MVC，而非传统的Java
 * MVC，前者范畴较广)。使用Rose框架开发，除了将<strong>paoding-rose.jar</strong>
 * 以及依赖的jar拷贝到classpath下，剩下的最大一项工作就是配置{@link RoseFilter}。
 * <p>
 * 您，作为Rose的使用者或评估者，您只需要按照下面介绍的方式将 {@link RoseFilter}
 * 拷贝配置在web.xml中，用以拦截<strong>所有</strong>的web请求即可。虽然{@link RoseFilter}
 * 会拦截所有请求，但是Rose能够判断所拦截的请求是否应该由对应的Controller控制器来处理(详见Referentce),
 * 如果Rose判断该请求不应在rose处理范围内，则 {@link RoseFilter}会让他“通过”不做任何处理。
 * <p>
 * 因为Rose使用Filter而非Servlet来接收并转发请求给控制器Controller，所以这里的配置必须满足一个很重要的注意点：
 * 即Rose过滤器必须配置在所有过滤器的
 * <strong>最后</strong>。只有这样，才能保证所有应该由控制器处理的web请求都能够通过其他可能的Filter拦截。
 * <p>
 * 绝大多数情况，按以下配置即可：
 * 
 * <pre>
 * 	&lt;filter&gt;
 * 		&lt;filter-name&gt;roseFilter&lt;/filter-name&gt;
 * 		&lt;filter-class&gt;net.paoding.rose.RoseFilter&lt;/filter-class&gt;
 * 	&lt;/filter&gt;
 * 
 * 	&lt;filter-mapping&gt;
 * 		&lt;filter-name&gt;roseFilter&lt;/filter-name&gt;
 * 		&lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * 		&lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 * 		&lt;dispatcher&gt;FORWARD&lt;/dispatcher&gt;
 * 		&lt;dispatcher&gt;INCLUDE&lt;/dispatcher&gt;
 * 	&lt;/filter-mapping&gt;
 * 
 * </pre>
 * 
 * 再次重复一下稍微需要注意的地方：<br>
 * 1)如上所说的<strong>filter-mapping</strong>必须配置在所有Filter Mapping的最后。
 * 2)不能将以上dispatcher的forward去掉，否则forward的请求Rose框架将拦截不到。include也是如此<br>
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class RoseFilter extends GenericFilterBean {

    /** 默认的applicationContext地址 */
    public static final String DEFAULT_CONTEXT_CONFIG_LOCATION = ""
            + "/WEB-INF/applicationContext*.xml,classpath:applicationContext*.xml";

    /** 使用的applicationContext地址 */
    private String contextConfigLocation = DEFAULT_CONTEXT_CONFIG_LOCATION;

    /** web模块 */
    private List<Module> modules = Collections.emptyList();

    /** 所有的请求被RoseFilter过滤时，基本上都将转到该RoseEngine处理 */
    private RoseEngine engine;

    /**
     * 改变默认行为，告知Rose要读取的applicationContext地址
     */
    public void setContextConfigLocation(final String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    public RoseEngine getEngine() {
        return engine;
    }

    @Override
    protected final void initFilterBean() throws ServletException {
        // 把servletContext收藏成起来
        PrivateVar.servletContext(getServletContext());

        // 如果没有通过Filter的init参数配置contextConfigLocation的话，
        // 则看看是否使用Spring要求的方式配置了contextConfigLocation没有?
        if (StringUtils.isBlank(contextConfigLocation)) {
            contextConfigLocation = getServletContext().getInitParameter("contextConfigLocation");
        }

        // 如果Filter和Context都没有配置contextConfigLocation，那就使用默认配置了
        // 我们建议开发者兄弟们，其实您为什么要配置呢？除非特殊情况，否则就使用惯例好了，是吧？大家都轻松
        if (StringUtils.isBlank(contextConfigLocation)) {
            contextConfigLocation = DEFAULT_CONTEXT_CONFIG_LOCATION;
        }

        try {
            // 创建Rose环境下的根WebApplicationContext对象

            List<Resource> jarContextResources = RoseJarContextResources.findContextResources();
            if (logger.isInfoEnabled()) {
                logger.info("jarContextResources: "
                        + ArrayUtils.toString(jarContextResources.toArray()));
            }
            String[] messageBasenames = RoseJarContextResources.findMessageBasenames();
            if (logger.isInfoEnabled()) {
                logger.info("jarMessageResources: " + ArrayUtils.toString(messageBasenames));
            }
            WebApplicationContext rootContext = null;
            rootContext = ContextLoader.createWebApplicationContext(getServletContext(),
                    rootContext, jarContextResources, messageBasenames, "rose.jars");
            rootContext = ContextLoader.createWebApplicationContext(getServletContext(),
                    rootContext, contextConfigLocation, new String[] { "classpath:messages",
                            "/WEB-INF/messages", }, "rose.root");
            PrivateVar.setRootWebApplicationContext(rootContext);

            // 然后自动扫描识别并初始化Controller控制器:
            // controller类应该放到package为xxx.controllers或其直接子package下(并以Controller结尾，请参考RoseConstaints.CONTROLLER_SUFFIXES)
            // 按照这样的约定，Rose就能自动“挑”出他们出来。
            List<ModuleInfo> moduleInfos = new RoseModuleInfos().findModuleInfos();
            modules = new ModulesBuilder().build(rootContext, moduleInfos);
            engine = new RoseEngine(modules);
            if (logger.isInfoEnabled()) {
                final StringBuilder sb = new StringBuilder(4096);
                dumpModules(modules, sb);
                String strModuleInfos = sb.toString();
                logger.info(strModuleInfos);
                getServletContext().log(strModuleInfos);
            }
            logger.info(String.format("Rose Initiated (version=%s)", RoseVersion.getVersion()));
            // 控制台提示
            getServletContext().log(
                    String.format("Rose Initiated (version=%s)", RoseVersion.getVersion()));
        } catch (final Exception e) {
            logger.error("", e);
            throw new NestedServletException(e.getMessage(), e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, final ServletResponse response,
            final FilterChain filterChain) throws IOException, ServletException {
        // cast
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        // debug
        if (logger.isDebugEnabled()) {
            logger.debug(httpRequest.getMethod() + " " + httpRequest.getRequestURL());
        }

        // 创建RequestPath对象，用于记录对地址解析的结果
        final RequestPath requestPath = new RequestPath();
        if (!initRequestPath(httpRequest, requestPath)) {
            forwardToWebContainer(filterChain, httpRequest, httpResponse, requestPath, null);
            return;
        }

        // 构造invocation对象，一个invocation对象用于封装和本次请求有关的匹配结果以及方法调用参数
        final InvocationBean inv = new InvocationBean();
        if (requestPath.isIncludeRequest() || requestPath.isForwardRequest()) {
            inv.setPreInvocation(InvocationUtils.getInvocation(httpRequest));
            // save before include
            if (requestPath.isIncludeRequest()) {
                saveAttributesBeforeInclude(inv, httpRequest);
            }
        }

        // fill invocation 
        inv.setRoseEngine(engine);
        inv.setRequestPath(requestPath);
        inv.setResponse(httpResponse);
        inv.setRequest(httpRequest);

        //
        boolean matched = false;
        try {
            matched = engine.match(inv);
        } catch (Throwable exception) {
            throwServletException(inv, exception);
        }

        // 
        if (!matched) {
            forwardToWebContainer(filterChain, httpRequest, httpResponse, requestPath, inv);
            return;
        } else {
            try {
                final List<String> parameterNames = inv.getMatchResultParameterNames();
                if (parameterNames.size() > 0) {
                    inv.setRequest(new ParameteredUriRequest(inv, parameterNames));
                }
                InvocationUtils.bindRequestToCurrentThread(inv.getRequest());
                engine.invoke(inv);

                // 渲染后的操作：拦截器的afterCompletion以及include属性快照的恢复等
                afterCompletion(inv, (Exception) null);
            } catch (Throwable exception) {
                // 异常后的操作(可能还没渲染)：拦截器的afterCompletion以及include属性快照的恢复等
                afterCompletion(inv, exception);
                throwServletException(inv, exception);
            }
        }
    }

    private void throwServletException(final InvocationBean inv, Throwable exception)
            throws ServletException {
        //
        final RequestPath requestPath = inv.getRequestPath();
        StringBuilder sb = new StringBuilder(1024);
        sb.append("error happended for request:").append(requestPath.getMethod());
        sb.append(" ").append(requestPath.getUri());
        if (inv.getActionMatchResult() != null) {
            sb.append("->");
            sb.append(inv.getActionEngine()).append(" params=");
            sb.append(Arrays.toString(inv.getMethodParameters()));
        }
        ServletException servletException = new NestedServletException(sb.toString(), exception);
        //
        logger.error("", servletException);
        //
        throw servletException;
    }

    private boolean initRequestPath(final HttpServletRequest request, final RequestPath requestPath) {
        // method
        requestPath.setMethod(request.getMethod());

        // ctxpath
        requestPath.setCtxpath(request.getContextPath());
        String invocationCtxpath = null; // 对include而言，invocationCtxPath指的是被include的ctxpath
        // dispather, uri, ctxpath
        String uri;
        if (WebUtils.isIncludeRequest(request)) {
            requestPath.setDispatcher(Dispatcher.INCLUDE);
            uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
            invocationCtxpath = ((String) request
                    .getAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE));
            requestPath.setPathInfo((String) request
                    .getAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE));
        } else {
            uri = request.getRequestURI();
            requestPath.setPathInfo(request.getServletPath());
            if (request.getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE) == null) {
                requestPath.setDispatcher(Dispatcher.REQUEST);
            } else {
                requestPath.setDispatcher(Dispatcher.FORWARD);
            }
        }
        if (uri.indexOf('%') != -1) {
            try {
                String encoding = request.getCharacterEncoding();
                if (encoding == null || encoding.length() == 0) {
                    encoding = "UTF-8";
                }
                uri = URLDecoder.decode(uri, encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        requestPath.setUri(uri);
        // 记录到requestPath的ctxpath值在include的情况下是invocationCtxpath

        if (requestPath.getCtxpath().length() <= 1) {
            requestPath.setPathInfo(requestPath.getUri());
        } else {
            requestPath.setPathInfo(requestPath.getUri().substring(
                    (invocationCtxpath == null ? requestPath.getCtxpath() : invocationCtxpath)
                            .length()));
        }
        if (requestPath.getPathInfo().startsWith(RoseConstants.VIEWS_PATH_WITH_END_SEP)
                || "/favicon.ico".equals(requestPath.getUri())) {
            return false;
        }
        return true;
    }

    protected void forwardToWebContainer(final FilterChain filterChain,
            final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final RequestPath requestPath, InvocationBean inv) throws IOException, ServletException {
        if (logger.isDebugEnabled()) {
            logger.debug("not rose uri: " + requestPath.getUri());
        }
        // 调用其它Filter
        filterChain.doFilter(httpRequest, httpResponse);
    }

    /**
     * Keep a snapshot of the request attributes in case of an include, to
     * be able to restore the original attributes after the include.
     * 
     * @param inv
     */
    private void saveAttributesBeforeInclude(final InvocationBean inv, ServletRequest request) {
        logger.debug("Taking snapshot of request attributes before include");
        Map<String, Object> attributesSnapshot = new HashMap<String, Object>();
        Enumeration<?> attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String) attrNames.nextElement();
            attributesSnapshot.put(attrName, request.getAttribute(attrName));
        }
        inv.setRequestAttributesBeforeInclude(attributesSnapshot);
    }

    private void afterCompletion(InvocationBean inv, Throwable e) {
        // 触发拦截器的afterCompletion接口
        triggerAfterCompletion(inv, e);

        // 恢复include请求前的各种请求属性(包括Model对象)
        if (inv.getRequestPath().isIncludeRequest()) {
            restoreRequestAttributesAfterInclude(inv);
        }
        Invocation preInvocation = inv.getPreInvocation();
        if (preInvocation != null) {
            InvocationUtils.bindRequestToCurrentThread(preInvocation.getRequest());
        } else {
            InvocationUtils.unindRequestFromCurrentThread();
        }
    }

    private void triggerAfterCompletion(InvocationBean inv, Throwable exceptionByModule) {
        // Apply afterCompletion methods of registered interceptors.
        ControllerInterceptor[] interceptors = inv.getActionEngine().getRegisteredInterceptors();
        BitSet executedInterceptorBitSet = inv.getExecutedInterceptorBitSet();
        for (int i = inv.getExecutedInterceptorIndex(); i >= 0; i--) {
            if (!executedInterceptorBitSet.get(i)) {
                continue;
            }
            ControllerInterceptor interceptor = interceptors[i];
            try {
                interceptor.afterCompletion(inv, exceptionByModule);
            } catch (Throwable thisException) {
                logger.error("ControllerInterceptor.afterCompletion", thisException);
            }
        }
    }

    /**
     * Restore the request attributes after an include.
     * 
     * @param request current HTTP request
     * @param attributesSnapshot the snapshot of the request attributes
     *        before the include
     */
    private void restoreRequestAttributesAfterInclude(InvocationBean inv) {
        logger.debug("Restoring snapshot of request attributes after include");
        HttpServletRequest request = inv.getRequest();

        Map<String, Object> attributesSnapshot = inv.getRequestAttributesBeforeInclude();

        // Need to copy into separate Collection here, to avoid side effects
        // on the Enumeration when removing attributes.
        Set<String> attrsToCheck = new HashSet<String>();
        Enumeration<?> attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String) attrNames.nextElement();
            attrsToCheck.add(attrName);
        }

        // Iterate over the attributes to check, restoring the original value
        // or removing the attribute, respectively, if appropriate.
        for (String attrName : attrsToCheck) {
            Object attrValue = attributesSnapshot.get(attrName);
            if (attrValue != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Restoring original value of attribute [" + attrName
                            + "] after include");
                }
                request.setAttribute(attrName, attrValue);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Removing attribute [" + attrName + "] after include");
                }
                request.removeAttribute(attrName);
            }
        }

    }

    @Override
    public void destroy() {
        WebApplicationContext rootContext = PrivateVar.getRootWebApplicationContext();
        if (rootContext != null) {
            AbstractApplicationContext roseJarsContext = (AbstractApplicationContext) rootContext
                    .getParent();
            if (roseJarsContext != null) {
                try {
                    roseJarsContext.close();
                } catch (Throwable e) {
                    logger.error("", e);
                    getServletContext().log("", e);
                }
            }
            try {
                ((AbstractApplicationContext) rootContext).close(); // rose.root
            } catch (Throwable e) {
                logger.error("", e);
                getServletContext().log("", e);
            }
        }
        try {
            engine.destroy();
        } catch (Exception e) {
            logger.error("", e);
            getServletContext().log("", e);
        }
        super.destroy();
    }

    //----------

    // 后续可以提取出来放到什么地方，是不是采用模板语言来定义?
    private void dumpModules(final List<Module> modules, final StringBuilder sb) {
        sb.append("\n--------Modules(Total ").append(modules.size()).append(")--------");
        sb.append("\n");
        for (int i = 0; i < modules.size(); i++) {
            final Module module = modules.get(i);
            sb.append("module ").append(i + 1).append(":");
            sb.append("\n\tmappingPath='").append(module.getMappingPath());
            sb.append("';\n\tpackageRelativePath='").append(module.getRelativePackagePath());
            sb.append("';\n\turl='").append(module.getUrl());
            sb.append("';\n\tcontrollers=[");
            final List<Mapping<ControllerInfo>> controllerMappings = module.getControllerMappings();
            for (final Mapping<ControllerInfo> mapping : controllerMappings) {
                sb.append("'").append(mapping.getPath()).append("'=").append(
                        mapping.getTarget().getControllerClass().getSimpleName()).append(", ");
            }
            if (!controllerMappings.isEmpty()) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("];\n\tparamResolvers=[");
            for (ParamResolver resolver : module.getCustomerResolvers()) {
                sb.append(resolver.getClass().getSimpleName()).append(", ");
            }
            if (module.getCustomerResolvers().size() > 0) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("];\n\tvalidators=[");
            for (NamedValidator validator : module.getValidators()) {
                sb.append(validator.getClass().getSimpleName()).append(", ");
            }
            if (module.getValidators().size() > 0) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("];\n\tinterceptors=[");
            for (NestedControllerInterceptorWrapper interceptor : module.getInterceptors()) {
                sb.append(interceptor.getName()).append("(").append(interceptor.getPriority())
                        .append("), ");
            }
            if (module.getInterceptors().size() > 0) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("];\n\terrorHander=").append(
                    module.getErrorHandler() == null ? "<null>" : module.getErrorHandler());
            final Mapping<ControllerInfo> def = module.getDefaultController();
            sb.append(";\n\tdefaultController=").append(def == null ? "<null>" : def.getPath());
            sb.append("\n\n");
        }
        sb.append("--------end--------");
    }

}
