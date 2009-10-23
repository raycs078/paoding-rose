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

import static net.paoding.rose.web.impl.mapping.MatchMode.PATH_STARTS_WITH;
import static net.paoding.rose.web.impl.mapping.ModifiedMapping.changeTarget;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.mapping.MappingImpl;
import net.paoding.rose.web.impl.module.ControllerInfo;
import net.paoding.rose.web.impl.module.Module;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

/**
 * {@link ModuleEngine} 负责从表示的模块中找出可匹配的 控制器引擎 {@link ControllerEngine}
 * 并委托其返回匹配的 {@link ActionEngine}对象，最终构成 {@link InvocationBean}对象返回.
 * <p>
 * {@link ModuleEngine}能够从失败控制器引擎匹配中走出来，继续匹配下一个控制器引擎，找到最终的
 * {@link ActionEngine}对象。即，如果一个匹配的控制器引擎中没有匹配的 {@link ActionEngine}对象，
 * {@link ModuleEngine}能够自动到下一个匹配的{@link ControllerEngine}对象去判断.
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ModuleEngine implements Engine {

    /** 日志对象 */
    private static Log logger = LogFactory.getLog(ModuleEngine.class);

    /** 模块对象 */
    private final Module module;

    /** 控制器映射数组，在对象构造时初始化。此为极其频繁使用的数据，为性能考虑，此处不使用普通集合对象 */
    private final Mapping<ControllerEngine>[] controllerMappings;

    // ---------------------------------------------------------------------

    /**
     * 构造能够正确匹配出到所给模块请求的控制器和方法的引擎，返回到相应 {@link InvocationBean}对象的模块引擎.
     * 
     * @param module
     * @throws NullPointerException 如果所传入的模块为空时
     */
    public ModuleEngine(Module module) {
        if (module == null) {
            throw new NullPointerException("module");
        }
        this.module = module;
        this.controllerMappings = initMappings(module);
    }

    public Mapping<ControllerEngine>[] getControllerMappings() {
        return Arrays.copyOf(controllerMappings, controllerMappings.length);
    }

    /**
     * 由构造子调用，创建给定模块对象的控制器引擎对象
     * 
     * @param module
     * @return
     */
    protected Mapping<ControllerEngine>[] initMappings(Module module) {
        // module返回的mappings
        List<Mapping<ControllerInfo>> rawMappings = module.getControllerMappings();

        // module定义的default mapping. rawDefMapping可能因为存在path=""的控制器，而被重置为空
        Mapping<ControllerInfo> rawDefMapping = module.getDefaultController();

        // 改target对象为engine后的mappings
        List<Mapping<ControllerEngine>> yesMappings = new ArrayList<Mapping<ControllerEngine>>(
                rawMappings.size());

        // 仅当最后rawDefMapping仍未非空时有效; 而且只能放置到mappings的最后
        Mapping<ControllerEngine> defMapping = null;
        for (Mapping<ControllerInfo> rawMapping : rawMappings) {
            // 将rawMapping转为target是ControllerEngine的mapping，放入到rightMappings中
            String path = rawMapping.getPath();
            ControllerInfo controllerInfo = rawMapping.getTarget();
            ControllerEngine controllerEngine = new ControllerEngine(module, path, controllerInfo);

            Mapping<ControllerEngine> rightMapping = changeTarget(rawMapping, controllerEngine);
            yesMappings.add(rightMapping);

            // 如果已经有定义path=""的控制器，可省去构造下面的lastMapping对象
            // 另，这个代码位置放在这，其正确性建立在rawMappings已经是排序的基础上：path=""如果存在，则一定是在最后
            if (StringUtils.isEmpty(rawMapping.getPath())) {
                defMapping = null;
            } else if (rawMapping == rawDefMapping) {
                defMapping = rightMapping;
            }
        }
        if (defMapping != null) {
            Mapping<ControllerEngine> lastMapping = new MappingImpl<ControllerEngine>(//NL
                    "", PATH_STARTS_WITH, defMapping.getTarget());
            yesMappings.add(lastMapping);
        }
        /* 为性能考虑这里返回数组：使用@SuppressWarnings压制unchecked警告 */
        @SuppressWarnings("unchecked")
        Mapping<ControllerEngine>[] array = yesMappings.toArray(new Mapping[yesMappings.size()]);
        return array;
    }

    // ---------------------------------------------------------------------

    /**
     * 返回所包含模块对象
     * 
     * @return
     */
    public Module getModule() {
        return module;
    }

    /**
     * @param request
     * @param response
     * @param requestPath
     * @return
     */
    public boolean match(final InvocationBean inv) {
        final HttpServletRequest request = inv.getRequest();
        final RequestPath requestPath = inv.getRequestPath();
        String mappingPath = requestPath.getControllerPathInfo();
        String requestMethod = request.getMethod();
        NoAtomicInteger nextMapping = new NoAtomicInteger();
        while (nextMapping.value < this.controllerMappings.length) {
            MatchResult<ControllerEngine> controllerMatchResult = nextMatchedController(
                    mappingPath, requestMethod, nextMapping);
            if (controllerMatchResult != null) {
                inv.setControllerMatchResult(controllerMatchResult);
                Mapping<ControllerEngine> mapping = controllerMatchResult.getMapping();
                if (logger.isDebugEnabled()) {
                    logger.debug("matched(" + mappingPath + "): " + mapping);
                    logger.debug("matchResult.matchedString= "
                            + controllerMatchResult.getMatchedString());
                }
                //
                requestPath.setControllerPath(controllerMatchResult.getMatchedString());
                //
                ControllerEngine controllerEngine = mapping.getTarget();
                if (controllerEngine.match(inv)) {
                    return true;
                }
            }
        }
        inv.setControllerMatchResult(null);
        return false;
    }

    public Object invoke(final InvocationBean inv) throws Throwable {
        for (String matchResultParam : inv.getModuleMatchResult().getParameterNames()) {
            inv.addModel(matchResultParam, inv.getModuleMatchResult()
                    .getParameter(matchResultParam));
        }
        try {
            inv.setMultiPartRequest(checkMultipart(inv));
            return innerInvoke(inv);
        } finally {
            if (inv.isMultiPartRequest()) {
                cleanupMultipart(inv);
            }
        }
    }

    private Object innerInvoke(final InvocationBean inv) throws Throwable {
        Object instruction = null;
        try {
            instruction = inv.getControllerEngine().invoke(inv);
        } catch (Throwable invException) {
            // 抛出异常了(可能是拦截器或控制器抛出的)，此时让该控制器所在模块的ControllerErrorHanlder处理

            Throwable cause = invException;
            // 因为使用的是类反射技术，所以需要先把实际异常从InvocationTargetException取出来
            while (cause instanceof InvocationTargetException) {
                cause = ((InvocationTargetException) cause).getTargetException();
            }
            // 
            ControllerErrorHandler errorHandler = module.getErrorHandler();
            if (errorHandler != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("exception happended, ControllerErrorHandler in module '" //
                            + module.getMappingPath() + "' will handler the exception: " //
                            + cause.getClass().getName() + ":" + cause.getMessage()); //
                }
                instruction = errorHandler.onError(inv, cause);
            }

            // onError方法返回null，表示需要重新throw出去
            // rethrow出去的不是cause而是invException，目的要把整个异常抛出来，以让知道整个异常的来由
            if ((errorHandler == null) || (instruction == null)) {
                if (invException instanceof Exception) {
                    throw (Exception) invException;
                } else {
                    throw (Error) invException;
                }
            }
        }
        return instruction;
    }

    public void destroy() {
        try {
            WebApplicationContext applicationContext = module.getApplicationContext();
            if (applicationContext instanceof AbstractApplicationContext) {
                ((AbstractApplicationContext) applicationContext).close();
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        for (Mapping<ControllerEngine> mapping : controllerMappings) {
            try {
                mapping.getTarget().destroy();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    /**
     * 返回该module engine的映射地址
     */
    @Override
    public String toString() {
        return this.module.getMappingPath();
    }

    //------------------------------------------------------

    protected MatchResult<ControllerEngine> nextMatchedController(String path,
            String requestMethod, NoAtomicInteger nextMapping) {
        if (logger.isDebugEnabled()) {
            logger.debug("try to find a controllerEngine for mappingPath [" + path + "]");
        }
        while (nextMapping.value < controllerMappings.length) {
            Mapping<ControllerEngine> mapping = controllerMappings[nextMapping.value++];
            if (logger.isDebugEnabled()) {
                logger.debug("try matching(" + path + "): " + mapping);
            }
            MatchResult<ControllerEngine> matchResult = mapping.match(path, requestMethod);
            if (matchResult != null) {
                return matchResult;
            }
        }
        return null;
    }

    protected boolean checkMultipart(InvocationBean inv) throws MultipartException {
        if ((module.getMultipartResolver() != null)
                && module.getMultipartResolver().isMultipart(inv.getRequest())) {
            if (inv.getRequest() instanceof MultipartHttpServletRequest) {
                logger.debug("Request is already a MultipartHttpServletRequest");
            } else {
                inv.setRequest(module.getMultipartResolver().resolveMultipart(inv.getRequest()));
            }
            return true;
        }
        return false;
    }

    /**
     * Clean up any resources used by the given multipart request (if any).
     * 
     * @see MultipartResolver#cleanupMultipart
     */
    protected void cleanupMultipart(InvocationBean inv) {
        if (inv.getRequest() instanceof MultipartHttpServletRequest) {
            module.getMultipartResolver().cleanupMultipart(
                    (MultipartHttpServletRequest) inv.getRequest());
        }
    }

    /**
     * @author 王志亮 [qieqie.wang@gmail.com]
     */
    private static class NoAtomicInteger {

        int value;
    }

}
