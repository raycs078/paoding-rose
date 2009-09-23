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

import static net.paoding.rose.web.impl.mapping.MatchMode.PATH_STARTS_WITH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.mapping.MappingImpl;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.thread.Engine;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.impl.thread.MatchResult;
import net.paoding.rose.web.impl.thread.ModuleEngine;
import net.paoding.rose.web.instruction.InstructionExecutor;
import net.paoding.rose.web.instruction.InstructionExecutorImpl;
import net.paoding.rose.web.var.FlashImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanInstantiationException;

/**
 * {@link RoseEngine}从{@link RoseFilter}接收web请求，并按照Rose规则进行处理.
 * <p>
 * {@link RoseEngine}会判断该web请求是否是本{@link RoseEngine}
 * 应该处理的，如果是进行后续的委派和处理，如果不是则{@link #match(InvocationBean)}返回false给上层.
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class RoseEngine implements Engine {

    // ------------------------------------------------------------

    protected final Log logger = LogFactory.getLog(getClass());

    /** 模块映射数组，在对象构造时初始化。此为极其频繁使用的数据，为性能考虑，此处不使用普通集合对象 */
    protected final Mapping<ModuleEngine>[] moduleMappings;

    /** 由它最终负责执行模块返回给Rose的指令，进行页面渲染等 */
    protected InstructionExecutor instructionExecutor = new InstructionExecutorImpl();

    // ------------------------------------------------------------

    /**
     * 构造能够将请求正确转发到所给modules的 {@link RoseEngine}对象.
     * <p>
     * 此构造子将调用 {@link #initMappings(List)}进行初始化，需要时，子类可以覆盖提供新的实现
     * 
     * @param modules 模块的集合，非空，对排序无要求； 如果集合的元素为null，该空元素将被忽略
     * @throws Exception
     * @throws NullPointerException 如果所传入的模块集合为null时
     */
    @SuppressWarnings("unchecked")
    public RoseEngine(final List<Module> modules) throws Exception {
        if (modules == null) {
            throw new NullPointerException("modules");
        }
        final List<Module> innerModules = Collections.unmodifiableList(modules);
        final List<Mapping<ModuleEngine>> mappings = initMappings(innerModules);
        Collections.sort(mappings);
        this.moduleMappings = mappings.toArray(new Mapping[innerModules.size()]);
    }

    /**
     * 由构造器调用，负责构建所给模块的映射器返回给构造子，构造子将会把返回的映射进行排序、转化保存到
     * {@link #moduleMappings} 中，
     * {@link #invoke(HttpServletRequest, HttpServletResponse, RequestPath)}
     * 将通过这些映射器将请求映射到合适的module
     * 
     * @param modules 同构造子的modules参数
     * @return 非null集合；元素不要含有空的对象；对排序无要求
     * @throws ClassNotFoundException
     * @throws BeanInstantiationException
     * @see #RoseEngine(List)
     */
    protected List<Mapping<ModuleEngine>> initMappings(final List<Module> modules) throws Exception {
        List<Mapping<ModuleEngine>> mappings = new ArrayList<Mapping<ModuleEngine>>(modules.size());
        for (Module module : modules) {
            if (module == null) {
                continue;
            }
            String path = module.getMappingPath();
            ModuleEngine engine = new ModuleEngine(module);
            Mapping<ModuleEngine> mapping = new MappingImpl<ModuleEngine>( // NL
                    path, PATH_STARTS_WITH, engine);
            mappings.add(mapping);
            if (logger.isDebugEnabled()) {
                logger.debug("create module mapping: " + mapping);
            }
        }
        return mappings;
    }

    // ------------------------------------------------------------

    /**
     * 设置自定义的指令执行器
     * 
     * @param instructionExecutor 非空
     */
    public void setInstructionExecutor(InstructionExecutor instructionExecutor) {
        if (instructionExecutor == null) {
            throw new NullPointerException("instructionExecutor");
        }
        this.instructionExecutor = instructionExecutor;
    }

    /**
     * 返回登记在本引擎的模块映射数组拷贝.
     * 
     * @return 模块映射的一次数组拷贝
     */
    public Mapping<ModuleEngine>[] getModuleMappings() {
        return Arrays.copyOf(moduleMappings, moduleMappings.length);
    }

    public final boolean match(final InvocationBean inv) {
        final RequestPath requestPath = inv.getRequestPath();

        // 从moduleMappings中按顺序判断本请求是否应该由该模块处理
        final MatchResult<ModuleEngine> moduleMatchResult = searchModule(requestPath);

        if (moduleMatchResult == null) {
            // 没有从moduleMappings找到这个请求地址的模块，向上返回CONTINUE表示该请求不应由Rose处理
            return false;
        }

        // 这个请求由这个module来尝试处理!
        inv.setModuleMatchResult(moduleMatchResult);
        final ModuleEngine moduleEngine = moduleMatchResult.getMapping().getTarget();

        if (!moduleEngine.match(inv)) {
            // 没有在匹配的模块中找到相应的控制器或控制器方法，很不幸!
            return false;
        }
        return true;
    }

    /**
     * {@link RoseEngine} 接口.调用此方法判断并处理请求.如果本引擎能够找到该请求相应的控制器方法处理，则启动整个调用过程，
     * 并最终渲染页面到客户端; 如果找不到匹配的控制器方法，则返回 {@link RoseConstants#CONTINUE}报告给调用者
     * 
     * @param request
     * @param response
     * @return
     * @throws ServletException
     */
    public Object invoke(final InvocationBean inv) throws Throwable {
        // rose which
        String roseWhich = inv.getRequest().getParameter("rose.which");
        if (roseWhich != null
                && inv.getRequest().getAttribute("$$paoding-rose.rose-info.mapping.invocation") == null) {
            inv.getRequest().setAttribute("$$paoding-rose.rose-info.mapping.invocation", inv);
            roseWhich = roseWhich.trim();
            if (roseWhich.length() == 0) {
                roseWhich = "controller";
            }
            return instructionExecutor.render(inv, "f:/rose-info/which/" + roseWhich);
        }
        //
        final RequestPath requestPath = inv.getRequestPath();
        ModuleEngine moduleEngine = inv.getModuleMatchResult().getMapping().getTarget();

        // 调用之前设置内置属性
        inv.addModel("invocation", inv);
        inv.addModel("ctxpath", requestPath.getCtxpath());

        // 按照Spring规范，设置当前的applicationContext对象到request对象中,用于messageSource/国际化等功能
        inv.getRequest().setAttribute(RoseConstants.WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                inv.getApplicationContext());

        // instruction是控制器action方法的返回结果或其对应的Instruction对象(也可能是拦截器、错误处理器返回的)
        Object ins = moduleEngine.invoke(inv);

        // 写flash消息到Cookie (被include的请求不会有功能)
        if (!requestPath.isIncludeRequest()) {
            FlashImpl flash = (FlashImpl) inv.getFlash(false);
            if (flash != null) {
                flash.writeNewMessages();
            }
        }

        // 渲染页面
        return ins = instructionExecutor.render(inv, ins);
    }

    public void destroy() {
        for (Mapping<ModuleEngine> mapping : moduleMappings) {
            try {
                mapping.getTarget().destroy();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    /**
     * 找到该请求地址的匹配对象，如果没有找到，返回null
     * 
     * @param requestPath
     * @return
     */
    private MatchResult<ModuleEngine> searchModule(final RequestPath requestPath) {
        for (Mapping<ModuleEngine> moduleMapping : moduleMappings) {
            MatchResult<ModuleEngine> moduleMatchResult = moduleMapping.match(//NL
                    requestPath.getPathInfo(), requestPath.getMethod());
            if (moduleMatchResult != null) {
                // !!found!!
                requestPath.setModulePath(moduleMatchResult.getMatchedString());
                if (logger.isDebugEnabled()) {
                    logger.debug("found module mapping: " + requestPath.getUri() + " -> "
                            + moduleMatchResult.getMapping().getTarget());
                }
                return moduleMatchResult;
            }
        }
        return null;
    }

}
