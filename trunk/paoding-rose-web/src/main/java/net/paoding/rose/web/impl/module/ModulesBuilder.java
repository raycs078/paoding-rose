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
import java.lang.reflect.Field;
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
import net.paoding.rose.scanner.ModuleInfo;
import net.paoding.rose.util.SpringUtils;
import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.ControllerInterceptor;
import net.paoding.rose.web.NamedValidator;
import net.paoding.rose.web.annotation.Ignored;
import net.paoding.rose.web.annotation.Interceptor;
import net.paoding.rose.web.annotation.NotForSubModules;
import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.context.ContextLoader;
import net.paoding.rose.web.paramresolver.ParamResolver;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.validation.Validator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * 根据输入的module类信息，构造出具体的Module结构出来
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ModulesBuilder {

    private Log logger = LogFactory.getLog(getClass());

    public List<Module> build(WebApplicationContext rootContext, List<ModuleInfo> moduleInfos)
            throws Exception {
        List<Module> modules = new ArrayList<Module>(moduleInfos.size());
        Map<ModuleInfo, Module> moduleMap = new HashMap<ModuleInfo, Module>();
        for (ModuleInfo moduleInfo : moduleInfos) {
            Module parentModule = moduleInfo.getParent() == null ? null : moduleMap.get(moduleInfo
                    .getParent());
            WebApplicationContext parentContext = (parentModule == null) ? rootContext
                    : parentModule.getApplicationContext();
            String moduleMappingPath = moduleInfo.getMappingPath();
            final String contextNamespace = "module-" + moduleMappingPath;
            final XmlWebApplicationContext context = createModuleContext(// NL
                    parentContext, contextNamespace, moduleInfo.getContextResources(), moduleInfo
                            .getMessageBasenames());
            final String contextAttrKey = WebApplicationContext.class.getName() + "."
                    + contextNamespace;
            context.getServletContext().setAttribute(contextAttrKey, context);

            // 创建module对象
            ModuleBean module = new ModuleBean(parentModule, moduleInfo.getModuleUrl(),
                    moduleMappingPath, moduleInfo.getRelativePackagePath(), context);
            moduleMap.put(moduleInfo, module);

            // 扫描找到的类...定义到applicationContext
            registerBeanDefinitions(context, moduleInfo.getModuleClasses());

            // 从Spring应用环境中找出全局resolver, interceptors, errorHanlder
            List<ParamResolver> customerResolvers = findContextResolvers(context);
            List<NestedControllerInterceptorWrapper> interceptors = findContextInterceptors(context);
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
            for (NestedControllerInterceptorWrapper interceptor : interceptors) {
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

            // multipart resolver
            module.setMultipartResolver(initMultipartResolver(context));

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
            ModuleBean module) throws IllegalAccessException {
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
        ReqMethod[] methods;
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
            methods = reqMappingAnnotation.methods();
            controllerPaths = reqMappingAnnotation.path();
        } else {
            methods = new ReqMethod[] { ReqMethod.ALL };
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
        Object rawController = null;
        if (clazz.isAssignableFrom(controller.getClass())) {
            rawController = controller;
        }
        for (int i = 0; i < controllerPaths.length; i++) {
            if (methods.length > 0) {
                module.addController(//
                        controllerPaths[i], methods, clazz, controllerName, controller);
            }
        }
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
        if (rawController != null) {
            if (controllerPaths.length > 0) {
                // 实现注意：内部拦截器对控制器字段的引用，最好使用控制器提供getXxx()方法获取，而非直接获取!
                checkInnerInterceptors(module, clazz, controllerPaths[0], controller, rawController);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("skip checking controller inner intercetpors: " + clazz.getName());
            }
        }
        return true;
    }

    private XmlWebApplicationContext createModuleContext(WebApplicationContext parent,
            final String namespace, final List<URL> contextResources,
            final String[] messageBasenames) throws IOException {
        return ContextLoader.createWebApplicationContext(parent.getServletContext(), parent,
                ContextLoader.toResources(contextResources), messageBasenames, namespace);
    }

    private void registerBeanDefinitions(XmlWebApplicationContext context, List<Class<?>> classes) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context.getBeanFactory();
        for (Class<?> clazz : classes) {
            if (context.getBeansOfType(clazz).size() > 0) {
                continue;
            }
            if (Modifier.isAbstract(clazz.getModifiers())
                    || Modifier.isInterface(clazz.getModifiers())
                    || !Modifier.isPublic(clazz.getModifiers())
                    || clazz.isAnnotationPresent(Ignored.class)) {
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
            registry.registerBeanDefinition(beanName, new AnnotatedGenericBeanDefinition(clazz));
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
        ArrayList<ParamResolver> resolvers = new ArrayList<ParamResolver>(
                resolverNames.length);
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

    private List<NestedControllerInterceptorWrapper> findContextInterceptors(
            XmlWebApplicationContext context) {
        String[] interceptorNames = SpringUtils.getBeanNames(context.getBeanFactory(),
                ControllerInterceptor.class);
        ArrayList<NestedControllerInterceptorWrapper> globalInterceptors = new ArrayList<NestedControllerInterceptorWrapper>(
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
            NestedControllerInterceptorWrapper.Builder builder = new NestedControllerInterceptorWrapper.Builder(
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
            NestedControllerInterceptorWrapper wrapper = builder.build();
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

    private void checkInnerInterceptors(ModuleBean module, Class<?> clazz, String controllerPath,
            Object controller, Object rawController) throws IllegalAccessException {
        // 控制器特有的拦截器
        Class<?> _clazz = clazz;
        while (true) {
            if (_clazz == Object.class || _clazz == null
                    || Modifier.isInterface(_clazz.getModifiers())) {
                break;
            }
            Field[] fields = _clazz.getDeclaredFields();
            for (Field field : fields) {
                // 只对本类声明或父类声明的公共或保护的字段(也就是子类可以引用的字段)
                // !!和控制器是否暴露父类的方法不一样的地方，这里不需要父类声明@AsSuperController标注
                if (clazz == _clazz || Modifier.isPublic(field.getModifiers())
                        || Modifier.isProtected(field.getModifiers())) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    // 
                    if (field.getAnnotation(Ignored.class) == null) {
                        field.setAccessible(true);
                        Object fieldValue = field.get(rawController);
                        if (fieldValue == null) {
                            continue;
                        }
                        if (fieldValue instanceof ControllerInterceptor) {
                            ControllerInterceptor unwrapperInterceptor = (ControllerInterceptor) fieldValue;
                            NestedControllerInterceptorWrapper.Builder builder = new NestedControllerInterceptorWrapper.Builder(
                                    unwrapperInterceptor);
                            Interceptor annotation = field.getAnnotation(Interceptor.class);
                            if (annotation != null) {
                                builder.oncePerRequest(annotation.oncePerRequest());
                            }
                            if (annotation != null && StringUtils.isNotBlank(annotation.name())) {
                                builder.name(controllerPath + "." + annotation.name());
                            } else {
                                builder.name(controllerPath + "." + field.getName());
                            }
                            //
                            NestedControllerInterceptorWrapper interceptor = builder.controller(
                                    controller).build();
                            module.addControllerInterceptor(interceptor);
                            if (logger.isDebugEnabled()) {
                                logger.debug("module '" + module.getMappingPath()
                                        + "': add intercetpor [" + interceptor.getPriority() + "]:"
                                        + interceptor.getName() + "="
                                        + fieldValue.getClass().getName());
                            }
                        } else {
                            ArrayList<ControllerInterceptor> list = new ArrayList<ControllerInterceptor>();
                            if (fieldValue.getClass().isArray()
                                    && fieldValue.getClass().getComponentType().isAssignableFrom(
                                            ControllerInterceptor.class)) {
                                ControllerInterceptor[] array = (ControllerInterceptor[]) fieldValue;
                                for (ControllerInterceptor object : array) {
                                    if (object != null) {
                                        list.add(object);
                                    }
                                }
                            } else if (fieldValue instanceof Iterable<?>) {
                                for (Object elem : (Iterable<?>) fieldValue) {
                                    if (elem != null && !(elem instanceof ControllerInterceptor)) {
                                        list.clear();
                                        break;
                                    }
                                    if (elem != null) {
                                        list.add((ControllerInterceptor) elem);
                                    }
                                }
                            }
                            // 具有相同名字的多个拦截器
                            for (ControllerInterceptor unwrapperInterceptor : list) {
                                String name = controllerPath + "." + field.getName();
                                NestedControllerInterceptorWrapper interceptor = new NestedControllerInterceptorWrapper.Builder(
                                        unwrapperInterceptor).name(name).controller(controller)
                                        .build();
                                module.addControllerInterceptor(interceptor);
                                if (logger.isDebugEnabled()) {
                                    logger.debug("module '"
                                            + module.getMappingPath()
                                            + "': add intercetpor ["
                                            + unwrapperInterceptor.getPriority()
                                            + "]="
                                            + interceptor.getName()
                                            + ClassUtils.getUserClass(unwrapperInterceptor)
                                                    .getName());
                                }
                            }
                        }
                    }
                }
            }
            _clazz = _clazz.getSuperclass();
        }
    }

    private MultipartResolver initMultipartResolver(ApplicationContext context) {
        MultipartResolver multipartResolver = (MultipartResolver) SpringUtils.getBean(context,
                MultipartResolver.class);
        if (multipartResolver != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Using MultipartResolver [" + multipartResolver + "]");
            }
        } else {
            multipartResolver = new CommonsMultipartResolver();
            if (logger.isDebugEnabled()) {
                logger.debug("No found MultipartResolver in context, "
                        + "Using MultipartResolver by default [" + multipartResolver + "]");
            }
        }
        return multipartResolver;
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
