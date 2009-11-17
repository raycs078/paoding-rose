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
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.scanner.ModuleInfo;
import net.paoding.rose.scanner.RoseJarContextResources;
import net.paoding.rose.scanner.RoseModuleInfos;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.NamedValidator;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.impl.context.ContextLoader;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.mapping.MappingImpl;
import net.paoding.rose.web.impl.mapping.MatchMode;
import net.paoding.rose.web.impl.module.ControllerInfo;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.module.ModulesBuilder;
import net.paoding.rose.web.impl.module.NestedControllerInterceptorWrapper;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.impl.thread.WebEngine;
import net.paoding.rose.web.impl.thread.tree.MappingNode;
import net.paoding.rose.web.impl.thread.tree.Rose;
import net.paoding.rose.web.impl.thread.tree.TreeBuilder;
import net.paoding.rose.web.instruction.InstructionExecutor;
import net.paoding.rose.web.instruction.InstructionExecutorImpl;
import net.paoding.rose.web.paramresolver.ParamResolver;
import net.paoding.rose.web.var.PrivateVar;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.NestedServletException;

/**
 * Paoding Rose 是一个WEB开发框架。
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
 * 注意：<br>
 * 1)<strong>filter-mapping</strong>必须配置在所有Filter Mapping的最后。<br>
 * 2)不能将<strong>FORWARD、INCLUDE</strong>的dispatcher去掉，否则forward、
 * include的请求Rose框架将拦截不到<br>
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class RoseFilter extends GenericFilterBean {

    /** 默认的applicationContext地址 */
    public static final String DEFAULT_CONTEXT_CONFIG_LOCATION = //
    "/WEB-INF/applicationContext*.xml,classpath:applicationContext*.xml";

    /** 使用的applicationContext地址 */
    private String contextConfigLocation;

    private InstructionExecutor instructionExecutor = new InstructionExecutorImpl();

    private List<Module> modules;

    private MappingNode mappingTree;

    /**
     * 改变默认行为，告知Rose要读取的applicationContext地址
     */
    public void setContextConfigLocation(String contextConfigLocation) {
        if (StringUtils.isBlank(contextConfigLocation)) {
            throw new IllegalArgumentException("contextConfigLocation");
        }
        this.contextConfigLocation = contextConfigLocation.trim();
    }

    public void setInstructionExecutor(InstructionExecutor instructionExecutor) {
        this.instructionExecutor = instructionExecutor;
    }

    @Override
    protected final void initFilterBean() throws ServletException {
        // TODO: 有必要把servletContext收藏成起来吗？ 
        // 把servletContext收藏成起来
        PrivateVar.servletContext(getServletContext());

        // 确认所使用的applicationContext配置
        if (StringUtils.isBlank(contextConfigLocation)) {
            String webxmlContextConfigLocation = getServletContext().getInitParameter(
                    "contextConfigLocation");
            if (!StringUtils.isBlank(webxmlContextConfigLocation)) {
                contextConfigLocation = webxmlContextConfigLocation;
            } else {
                contextConfigLocation = DEFAULT_CONTEXT_CONFIG_LOCATION;
            }
        }

        //
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

            // TODO: 有必要否？
            PrivateVar.setRootWebApplicationContext(rootContext);

            // 自动扫描识别web层对象，纳入Rose管理
            List<ModuleInfo> moduleInfoList = new RoseModuleInfos().findModuleInfos();
            this.modules = new ModulesBuilder().build(rootContext, moduleInfoList);
            //
            WebEngine roseEngine = new WebEngine(instructionExecutor);
            Mapping<WebEngine> rootMapping = new MappingImpl<WebEngine>("",
                    MatchMode.PATH_STARTS_WITH, roseEngine);
            this.mappingTree = new MappingNode(rootMapping, null);
            //
            new TreeBuilder().create(mappingTree, modules);
            if (logger.isInfoEnabled()) {
                final StringBuilder sb = new StringBuilder(4096);
                dumpModules(modules, sb);
                String strModuleInfos = sb.toString();
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
        final RequestPath requestPath = new RequestPath(httpRequest);

        // 
        if (requestPath.getRosePath().startsWith(RoseConstants.VIEWS_PATH_WITH_END_SEP)
                || "/favicon.ico".equals(requestPath.getUri())) {
            forwardToWebContainer(filterChain, httpRequest, httpResponse, requestPath);
            return;
        }

        // 构造invocation对象，一个invocation对象用于封装和本次请求有关的匹配结果以及方法调用参数
        final Invocation inv = new InvocationBean(httpRequest, httpResponse, requestPath);
        final Rose rose = new Rose(this.modules, mappingTree, inv);
        boolean matched = false;
        try {
            matched = rose.execute(); // 渲染后的操作：拦截器的afterCompletion以及include属性快照的恢复等
        } catch (Throwable exception) {
            throwServletException(requestPath, exception);
        }
        // don't cache exception by request by forward 
        if (!matched) {
            forwardToWebContainer(filterChain, httpRequest, httpResponse, requestPath);
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
        MappingNode cur = mappingTree;
        while (cur != null) {
            try {
                cur.mapping.getTarget().destroy();
            } catch (Exception e) {
                logger.error("", e);
                getServletContext().log("", e);
            }
            if (cur.leftMostChild != null) {
                cur = cur.leftMostChild;
            } else if (cur.sibling != null) {
                cur = cur.sibling;
            } else {
                while (true) {
                    cur = cur.parent;
                    if (cur == null) {
                        break;
                    } else {
                        if (cur.sibling != null) {
                            cur = cur.sibling;
                            break;
                        }
                    }
                }
            }
        }
        super.destroy();
    }

    private void forwardToWebContainer(//
            FilterChain filterChain, //
            HttpServletRequest httpRequest,//
            HttpServletResponse httpResponse,//
            RequestPath requestPath)//
            throws IOException, ServletException {
        if (logger.isDebugEnabled()) {
            logger.debug("not rose uri: " + requestPath.getUri());
        }
        // 调用其它Filter
        filterChain.doFilter(httpRequest, httpResponse);
    }

    private void throwServletException(RequestPath requestPath, Throwable exception)
            throws ServletException {
        String msg = requestPath.getMethod() + " " + requestPath.getUri();
        ServletException servletException;
        if (exception instanceof ServletException) {
            servletException = (ServletException) exception;
        } else {
            servletException = new NestedServletException(msg, exception);
        }
        logger.error(msg, exception);
        getServletContext().log(msg, exception);
        throw servletException;
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
