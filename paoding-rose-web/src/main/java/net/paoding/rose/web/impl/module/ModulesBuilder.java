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

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.paoding.rose.RoseConstants;
import net.paoding.rose.scanner.ModuleResource;
import net.paoding.rose.util.SpringUtils;
import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.ControllerInterceptor;
import net.paoding.rose.web.NamedValidator;
import net.paoding.rose.web.annotation.Ignored;
import net.paoding.rose.web.annotation.Interceptor;
import net.paoding.rose.web.annotation.NotForSubModules;
import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.impl.context.ContextLoader;
import net.paoding.rose.web.paramresolver.ParamResolver;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.validation.Validator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * 根据输入的module类信息，构造出具体的Module结构出来
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ModulesBuilder {

    private Log logger = LogFactory.getLog(getClass());

    public List<Module> build(WebApplicationContext rootContext, List<ModuleResource> moduleInfos)
            throws Exception {
        List<Module> modules = new ArrayList<Module>(moduleInfos.size());
        Map<ModuleResource, Module> moduleMap = new HashMap<ModuleResource, Module>();
        for (ModuleResource resource : moduleInfos) {
            Module parentModule = resource.getParent() == null ? null : moduleMap.get(resource
                    .getParent());
            WebApplicationContext parentContext = (parentModule == null) ? rootContext
                    : parentModule.getApplicationContext();
            String moduleMappingPath = resource.getMappingPath();
            final String contextNamespace = "module-" + moduleMappingPath;
            final XmlWebApplicationContext context = createModuleContext(// NL
                    parentContext, contextNamespace, resource.getContextResources(), resource
                            .getMessageBasenames());
            final String contextAttrKey = WebApplicationContext.class.getName() + "."
                    + contextNamespace;
            if (context.getServletContext() != null) {
                context.getServletContext().setAttribute(contextAttrKey, context);
            }
            // 创建module对象
            ModuleImpl module = new ModuleImpl(parentModule, resource.getModuleUrl(),
                    moduleMappingPath, resource.getModulePath(), context);
            moduleMap.put(resource, module);

            // 扫描找到的类...定义到applicationContext
            registerBeanDefinitions(context, resource.getModuleClasses());

            // 从Spring应用环境中找出全局resolver, interceptors, errorHanlder
            List<ParamResolver> customerResolvers = findContextResolvers(context);
            List<NestedControllerInterceptor> interceptors = findContextInterceptors(context);
            List<NamedValidator> validators = findContextValidators(context);
            ControllerErrorHandler errorHandler = getContextErrorHandler(context);

            // resolvers
            for (ParamResolver resolver : customerResolvers) {
                module.addCustomerResolver(resolver);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("module '" + module.getMappingPath() + "': apply global resolvers "
                        + Arrays.toString(customerResolvers.toArray()));
            }

            // 将拦截器设置到module中
            for (NestedControllerInterceptor interceptor : interceptors) {
                module.addControllerInterceptor(interceptor);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("module '" + module.getMappingPath() + "': apply global intercetpors "
                        + Arrays.toString(interceptors.toArray()));
            }
            // 将validator设置到module中
            for (NamedValidator validator : validators) {
                module.addValidator(validator);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("module '" + module.getMappingPath() + "': apply global validators "
                        + Arrays.toString(validators.toArray()));
            }

            // errorhandler
            if (errorHandler != null) {
                if (Proxy.isProxyClass(errorHandler.getClass())) {
                    module.setErrorHandler(errorHandler);
                } else {
                    ErrorHandlerDispatcher dispatcher = new ErrorHandlerDispatcher(errorHandler);
                    module.setErrorHandler(dispatcher);
                }
                if (logger.isInfoEnabled()) {
                    logger.info("set errorHandler: " + module.getMappingPath() + "  "
                            + errorHandler);
                }
            }

            // controllers
            final ListableBeanFactory beanFactory = context.getBeanFactory();
            for (String beanName : beanFactory.getBeanDefinitionNames()) {
                checkController(context, beanName, module);
            }

            // 放进去以返回
            modules.add(module);
        }

        return modules;
    }

    private boolean checkController(final XmlWebApplicationContext context, String beanName,
            ModuleImpl module) throws IllegalAccessException {
        AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) context.getBeanFactory()
                .getBeanDefinition(beanName);
        String beanClassName = beanDefinition.getBeanClassName();
        String controllerSuffix = null;
        for (String suffix : RoseConstants.CONTROLLER_SUFFIXES) {
            if (beanClassName.length() > suffix.length() && beanClassName.endsWith(suffix)) {
                if (suffix.length() == 1
                        && Character.isUpperCase(beanClassName.charAt(beanClassName.length()
                                - suffix.length() - 1))) {
                    continue;
                }
                controllerSuffix = suffix;
                break;
            }
        }
        if (controllerSuffix == null) {
            return false;
        }
        String[] controllerPaths = null;
        if (!beanDefinition.hasBeanClass()) {
            try {
                beanDefinition.resolveBeanClass(Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                throw new CannotLoadBeanClassException("", beanName, beanDefinition
                        .getBeanClassName(), e);
            }
        }
        final Class<?> clazz = beanDefinition.getBeanClass();
        final String controllerName = StringUtils.removeEnd(ClassUtils
                .getShortNameAsProperty(clazz), controllerSuffix);
        ReqMapping reqMappingAnnotation = clazz.getAnnotation(ReqMapping.class);
        if (reqMappingAnnotation != null) {
            controllerPaths = reqMappingAnnotation.path();
        }
        if (controllerPaths != null) {
            // 如果controllerPaths.length==0，表示没有任何path可以映射到这个controller了
            for (int i = 0; i < controllerPaths.length; i++) {
                if (ReqMapping.DEFAULT_PATH.equals(controllerPaths[i])) {
                    controllerPaths[i] = "/" + controllerName;
                } else if (controllerPaths[i].length() > 0 && controllerPaths[i].charAt(0) != '/') {
                    controllerPaths[i] = '/' + controllerPaths[i];
                } else if (controllerPaths[i].equals("/")) {
                    controllerPaths[i] = "";
                }
            }
        } else {
            controllerPaths = new String[] { "/" + controllerName };
        }
        // 这个Controller是否已经在Context中配置了?
        // 如果使用Context配置，就不需要在这里实例化
        Object controller = context.getBean(beanName);
        module.addController(//
                controllerPaths, clazz, controllerName, controller);
        if (Proxy.isProxyClass(controller.getClass())) {
            if (logger.isDebugEnabled()) {
                logger.debug("module '" + module.getMappingPath() + "': add controller "
                        + Arrays.toString(controllerPaths) + "= proxy of " + clazz.getName());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("module '" + module.getMappingPath() + "': add controller "
                                + Arrays.toString(controllerPaths) + "= "
                                + controller.getClass().getName());
            }
        }
        return true;
    }

    private XmlWebApplicationContext createModuleContext(WebApplicationContext parent,
            final String namespace, final List<URL> contextResources,
            final String[] messageBasenames) throws IOException {
        return ContextLoader.createWebApplicationContext(parent == null ? null : parent
                .getServletContext(), parent, ContextLoader.toResources(contextResources), "",
                messageBasenames, namespace);
    }

    private void registerBeanDefinitions(XmlWebApplicationContext context, List<Class<?>> classes) {
        DefaultListableBeanFactory bf = (DefaultListableBeanFactory) context.getBeanFactory();
        String[] definedClasses = new String[bf.getBeanDefinitionCount()];
        String[] definitionNames = bf.getBeanDefinitionNames();
        for (int i = 0; i < definedClasses.length; i++) {
            String name = definitionNames[i];
            definedClasses[i] = bf.getBeanDefinition(name).getBeanClassName();
        }
        for (Class<?> clazz : classes) {
            // 排除非规范的类
            if (Modifier.isAbstract(clazz.getModifiers())
                    || Modifier.isInterface(clazz.getModifiers())
                    || !Modifier.isPublic(clazz.getModifiers())
                    || clazz.isAnnotationPresent(Ignored.class)) {
                logger.debug("ignores controller[abstract?interface?not public?Ignored?]: "
                        + clazz.getName());
                continue;
            }
            // 排除手动定义的bean
            String clazzName = clazz.getName();
            if (ArrayUtils.contains(definedClasses, clazzName)) {
                logger.debug("ignores controller[bean in context]: " + clazz.getName());
                continue;
            }
            //
            String beanName = null;
            if (ControllerInterceptor.class.isAssignableFrom(clazz)
                    && clazz.isAnnotationPresent(Interceptor.class)) {
                beanName = clazz.getAnnotation(Interceptor.class).name();
            }
            if (StringUtils.isEmpty(beanName) && clazz.isAnnotationPresent(Component.class)) {
                beanName = clazz.getAnnotation(Component.class).value();
            }
            if (StringUtils.isEmpty(beanName) && clazz.isAnnotationPresent(Resource.class)) {
                beanName = clazz.getAnnotation(Resource.class).name();
            }
            if (StringUtils.isEmpty(beanName)) {
                beanName = ClassUtils.getShortNameAsProperty(clazz);
            }

            bf.registerBeanDefinition(beanName, new AnnotatedGenericBeanDefinition(clazz));
        }
    }

    private ControllerErrorHandler getContextErrorHandler(XmlWebApplicationContext context) {
        ControllerErrorHandler errorHandler = null;
        String[] names = SpringUtils.getBeanNames(context.getBeanFactory(),
                ControllerErrorHandler.class);
        for (int i = 0; errorHandler == null && i < names.length; i++) {
            errorHandler = (ControllerErrorHandler) context.getBean(names[i]);
            Class<?> userClass = ClassUtils.getUserClass(errorHandler);
            if (userClass.isAnnotationPresent(Ignored.class)) {
                logger.debug("Ignored controllerErrorHandler: " + errorHandler);
                errorHandler = null;
                continue;
            } else if (userClass.isAnnotationPresent(NotForSubModules.class)
                    && context.getBeanFactory().getBeanDefinition(names[i]) == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored controllerErrorHandler (NotForSubModules):"
                            + errorHandler);
                }
                errorHandler = null;
                continue;
            }
        }
        return errorHandler;
    }

    private List<ParamResolver> findContextResolvers(XmlWebApplicationContext context) {
        String[] resolverNames = SpringUtils.getBeanNames(context.getBeanFactory(),
                ParamResolver.class);
        ArrayList<ParamResolver> resolvers = new ArrayList<ParamResolver>(resolverNames.length);
        for (String beanName : resolverNames) {
            ParamResolver resolver = (ParamResolver) context.getBean(beanName);
            Class<?> userClass = ClassUtils.getUserClass(resolver);
            if (userClass.isAnnotationPresent(Ignored.class)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored context resolver:" + resolver);
                }
                continue;
            }
            if (userClass.isAnnotationPresent(NotForSubModules.class)
                    && context.getBeanFactory().getBeanDefinition(beanName) == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored context resolver (NotForSubModules):" + resolver);
                }
                continue;
            }
            resolvers.add(resolver);
            if (logger.isDebugEnabled()) {
                logger.debug("context resolver[" + resolver.getClass().getName());
            }
        }
        return resolvers;
    }

    private List<NestedControllerInterceptor> findContextInterceptors(
            XmlWebApplicationContext context) {
        String[] interceptorNames = SpringUtils.getBeanNames(context.getBeanFactory(),
                ControllerInterceptor.class);
        ArrayList<NestedControllerInterceptor> globalInterceptors = new ArrayList<NestedControllerInterceptor>(
                interceptorNames.length);
        for (String beanName : interceptorNames) {
            ControllerInterceptor interceptor = (ControllerInterceptor) context.getBean(beanName);
            Class<?> userClass = ClassUtils.getUserClass(interceptor);
            if (userClass.isAnnotationPresent(Ignored.class)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored context interceptor:" + interceptor);
                }
                continue;
            }
            if (userClass.isAnnotationPresent(NotForSubModules.class)
                    && !context.getBeanFactory().containsBeanDefinition(beanName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored context interceptor (NotForSubModules):" + interceptor);
                }
                continue;
            }
            NestedControllerInterceptor.Builder builder = new NestedControllerInterceptor.Builder(
                    interceptor);
            Interceptor annotation = userClass.getAnnotation(Interceptor.class);
            if (annotation != null) {
                builder.oncePerRequest(annotation.oncePerRequest());
            }
            if (annotation != null && StringUtils.isNotBlank(annotation.name())) {
                builder.name(annotation.name());
            } else {
                builder.name(asShortPropertyName(userClass.getSimpleName(), "Interceptor"));
            }
            NestedControllerInterceptor wrapper = builder.build();
            globalInterceptors.add(wrapper);
            if (logger.isDebugEnabled()) {
                logger.debug("context interceptor[" + interceptor.getPriority() + "]: " // \r\n
                        + wrapper.getName() + "=" + userClass.getName());
            }
        }
        return globalInterceptors;
    }

    private List<NamedValidator> findContextValidators(XmlWebApplicationContext context) {
        String[] validatorNames = SpringUtils.getBeanNames(context.getBeanFactory(),
                Validator.class);
        ArrayList<NamedValidator> globalValidators = new ArrayList<NamedValidator>(
                validatorNames.length);
        for (String beanName : validatorNames) {
            Validator validator = (Validator) context.getBean(beanName);
            Class<?> userClass = ClassUtils.getUserClass(validator);
            if (userClass.isAnnotationPresent(Ignored.class)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored context validator:" + validator);
                }
                continue;
            }
            if (userClass.isAnnotationPresent(NotForSubModules.class)
                    && context.getBeanFactory().getBeanDefinition(beanName) == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored context validator (NotForSubModules):" + validator);
                }
                continue;
            }
            NamedValidator wrapper;
            if (NamedValidator.class.isAssignableFrom(userClass)) {
                wrapper = (NamedValidator) validator;
            } else {
                wrapper = new NamedValidatorWrapper(asShortPropertyName(userClass.getSimpleName(),
                        "Validator"), validator);
            }
            globalValidators.add(wrapper);
            if (logger.isDebugEnabled()) {
                logger.debug("context validator: " // \r\n
                        + wrapper.getName() + "=" + userClass.getName());
            }
        }
        return globalValidators;
    }

    static String asShortPropertyName(String beanName, String suffixToRemove) {
        beanName = org.springframework.util.StringUtils.unqualify(beanName);
        beanName = org.springframework.util.StringUtils.uncapitalize(beanName);
        if (suffixToRemove != null && beanName.endsWith(suffixToRemove)) {
            beanName = beanName.substring(0, beanName.length() - suffixToRemove.length());
        }
        return beanName;
    }

}
