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
package net.paoding.rose.web.impl.module;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.NamedValidator;
import net.paoding.rose.web.annotation.DefaultController;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.mapping.MappingImpl;
import net.paoding.rose.web.impl.mapping.MatchMode;
import net.paoding.rose.web.paramresolver.ParamResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartResolver;

/**
 * {@link Module}的实现
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ModuleBean implements Module {

    private static Log logger = LogFactory.getLog(ModuleBean.class);

    // 该模块的映射地址
    private String mappingPath;

    // 该模块的类地址
    private URL url;

    // 该模块相对于所在目录树controllers的地址，以'/'分隔，空串或以'/'开始
    private String relativePackagePath;

    // url父目录对应的module,如果本module是根module,则parent==null
    private Module parent;

    // 这个module的spring applicationContext对象，和上级module的applicationContext的关系也是上下级关系
    private WebApplicationContext applicationContext;

    // 本module下的控制器以及映射定义
    private List<Mapping<ControllerInfo>> controllerMappings = new ArrayList<Mapping<ControllerInfo>>();

    // 本module使用的所有非内置的方法参数解析器
    private List<ParamResolver> customerResolvers = new ArrayList<ParamResolver>();

    // 用于add方法加进来
    private List<NestedControllerInterceptorWrapper> interceptors = new ArrayList<NestedControllerInterceptorWrapper>(
            32);

    // 用于add方法加进来
    private List<NamedValidator> validators = new ArrayList<NamedValidator>(32);

    // 本模块使用的错误处理器(如果本模块没有定义，则使用上级模块的errorHanlder或根applicationContext的errorHandler)
    private ControllerErrorHandler errorHandler;

    // 默认的控制器，当按照"/controller/action"找不到控制器处理请求时，会试着看看这个控制器是否可以处理
    // 会先看看有没有@Path("")标注的或@DefaultController标注的
    // 没有的话则按照候选方案看看有没有default,index,home,welcome的控制器，有的话就是它了
    private Mapping<ControllerInfo> defaultController;

    // 默认控制器可能是null的，那么现在的defaultController如果是null，是什么意思呢？
    // defaultControllerDone==false,代表应该重新从interceptors计算defaultController
    private boolean defaultControllerDone;

    // 本模块使用的上传解析器(如果本模块的applicationContext没有，则使用上级模块的multipartResolver)
    private MultipartResolver multipartResolver;

    public ModuleBean(Module parent, URL url, String mappingPath, String relativePackagePath,
            WebApplicationContext context) {
        this.parent = parent;
        this.url = url;
        this.mappingPath = mappingPath;
        this.relativePackagePath = relativePackagePath;
        this.applicationContext = context;
    }

    @Override
    public Module getParent() {
        return parent;
    }

    @Override
    public String getMappingPath() {
        return mappingPath;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public String getRelativePackagePath() {
        return relativePackagePath;
    }

    @Override
    public WebApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public Mapping<ControllerInfo> getDefaultController() {
        if (!defaultControllerDone) {
            if (defaultController == null) {
                for (Mapping<ControllerInfo> info : controllerMappings) {
                    if (info.getTarget().getControllerClass()
                            .getAnnotation(DefaultController.class) != null) {
                        defaultController = info;
                        logger.info("module '" + getMappingPath() + "': found default controller '"
                                + defaultController + "'("
                                + info.getTarget().getControllerClass().getName()
                                + ") by annotation @" + DefaultController.class.getSimpleName());
                        break;
                    }
                }
            }
            if (defaultController == null) {
                String[] candidates = { "", "/default", "/index", "/home", "/welcome", "/hello" };
                for (String candidate : candidates) {
                    for (Mapping<ControllerInfo> info : controllerMappings) {
                        if (candidate.equals(info.getPath())) {
                            defaultController = info;
                            logger.info("module '" + getMappingPath()
                                    + "': found default controller '" + defaultController + "'("
                                    + info.getTarget().getControllerClass().getName()
                                    + ")  by sacnning controller by convention.");
                            break;
                        }

                    }
                }
            }
            if (defaultController == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("module '" + getMappingPath() + "': nout found"
                            + " a default controller by annotation" + " or by convention.");
                }
            }
            defaultControllerDone = true;
        }
        return defaultController;
    }

    public void setDefaultController(Mapping<ControllerInfo> defaultController) {
        this.defaultController = defaultController;
    }

    @Override
    public List<Mapping<ControllerInfo>> getControllerMappings() {
        List<Mapping<ControllerInfo>> controllerInfos = new ArrayList<Mapping<ControllerInfo>>(
                this.controllerMappings.size());
        controllerInfos.addAll(this.controllerMappings);
        Collections.sort(controllerInfos);
        return Collections.unmodifiableList(controllerInfos);
    }

    public ModuleBean addController(String controllerPath, ReqMethod[] methods,
            Class<?> controllerClass, String controllerName, Object controller) {
        ControllerInfo contr = new ControllerInfo();
        contr.setControllerClass(controllerClass);
        contr.setControllerObject(controller);
        contr.setControllerName(controllerName);
        this.controllerMappings.add(new MappingImpl<ControllerInfo>(controllerPath,
                MatchMode.PATH_STARTS_WITH, methods, contr));
        return this;
    }

    public ModuleBean addCustomerResolver(ParamResolver resolver) {
        customerResolvers.add(resolver);
        return this;
    }

    public List<ParamResolver> getCustomerResolvers() {
        return Collections.unmodifiableList(customerResolvers);
    }

    public ModuleBean addControllerInterceptor(NestedControllerInterceptorWrapper interceptor) {
        for (int i = 0; i < interceptors.size(); i++) {
            if (interceptor.getPriority() > interceptors.get(i).getPriority()) {
                this.interceptors.add(i, interceptor);
                return this;
            }
        }
        this.interceptors.add(interceptor);
        return this;
    }

    @Override
    public List<NestedControllerInterceptorWrapper> getInterceptors() {
        return interceptors;
    }

    @Override
    public ModuleBean addValidator(NamedValidator validator) {
        this.validators.add(validator);
        return this;
    }

    @Override
    public List<NamedValidator> getValidators() {
        return validators;
    }

    @Override
    public ControllerErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ControllerErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public MultipartResolver getMultipartResolver() {
        return multipartResolver;
    }

    public void setMultipartResolver(MultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }
}
