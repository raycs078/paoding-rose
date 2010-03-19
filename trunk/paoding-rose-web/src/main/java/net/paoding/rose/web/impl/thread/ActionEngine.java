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

import static org.springframework.validation.BindingResult.MODEL_KEY_PREFIX;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.RoseVersion;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.ParamValidator;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.annotation.HttpFeatures;
import net.paoding.rose.web.annotation.IfParamExists;
import net.paoding.rose.web.annotation.Intercepted;
import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.annotation.Return;
import net.paoding.rose.web.impl.mapping.MatchResult;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.module.NestedControllerInterceptor;
import net.paoding.rose.web.impl.validation.ParameterBindingResult;
import net.paoding.rose.web.paramresolver.MethodParameterResolver;
import net.paoding.rose.web.paramresolver.ParamMetaData;
import net.paoding.rose.web.paramresolver.ParamResolver;
import net.paoding.rose.web.paramresolver.ParameterNameDiscovererImpl;
import net.paoding.rose.web.paramresolver.ResolverFactoryImpl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.SpringVersion;
import org.springframework.validation.Errors;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public final class ActionEngine implements Engine {

    private static Log logger = LogFactory.getLog(ActionEngine.class);

    private final Module module;

    private final Class<?> controllerClass;

    private final Object controller;

    private final Method method;

    private final NestedControllerInterceptor[] interceptors;

    private final ParamValidator[] validators;

    private final MethodParameterResolver methodParameterResolver;

    private transient String toStringCache;

    public ActionEngine(Module module, Class<?> controllerClass, Object controller, Method method) {
        this.module = module;
        this.controllerClass = controllerClass;
        this.controller = controller;
        this.method = method;
        interceptors = compileInterceptors();
        methodParameterResolver = compileParamResolvers();
        validators = compileValidators();
        if (logger.isDebugEnabled()) {
            logger
                    .debug("action info: " + controllerClass.getName() + "." + method.getName()
                            + ":");
            logger.debug("\t interceptors:" + Arrays.toString(interceptors));
            logger.debug("\t validators:" + Arrays.toString(validators));
        }
    }

    public NestedControllerInterceptor[] getRegisteredInterceptors() {
        return interceptors;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Object getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }

    public String[] getParameterNames() {
        return methodParameterResolver.getParameterNames();
    }

    private MethodParameterResolver compileParamResolvers() {
        ParameterNameDiscovererImpl parameterNameDiscoverer = new ParameterNameDiscovererImpl();
        ResolverFactoryImpl resolverFactory = new ResolverFactoryImpl();
        for (ParamResolver resolver : module.getCustomerResolvers()) {
            resolverFactory.addCustomerResolver(resolver);
        }
        return new MethodParameterResolver(this.controllerClass, method, parameterNameDiscoverer,
                resolverFactory);
    }

    @SuppressWarnings("unchecked")
    private ParamValidator[] compileValidators() {
        Class[] parameterTypes = method.getParameterTypes();
        List<ParamValidator> validators = module.getValidators();
        ParamValidator[] registeredValidators = new ParamValidator[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            for (ParamValidator validator : validators) {
                if (validator.supports(methodParameterResolver.getParamMetaDatas()[i])) {
                    registeredValidators[i] = validator;
                    break;
                }
            }
        }
        //
        return registeredValidators;
    }

    private NestedControllerInterceptor[] compileInterceptors() {
        List<NestedControllerInterceptor> interceptors = module.getInterceptors();
        List<NestedControllerInterceptor> registeredInterceptors = new ArrayList<NestedControllerInterceptor>(
                interceptors.size());
        for (NestedControllerInterceptor interceptor : interceptors) {
            // 确定本拦截器的名字
            String name = interceptor.getName();
            String nameForUser = name;
            // 获取@Intercepted注解 (@Intercepted注解配置于控制器或其方法中，决定一个拦截器是否应该拦截之。没有配置按“需要”处理)
            Intercepted intercepted = method.getAnnotation(Intercepted.class);
            if (intercepted == null) {
                // 对于标注@Inherited的annotation，class.getAnnotation可以保证：如果本类没有，自动会从父类判断是否具有
                intercepted = this.controllerClass.getAnnotation(Intercepted.class);
            }
            // 通过@Intercepted注解的allow和deny排除拦截器
            if (intercepted != null) {
                // 3.1 先排除deny禁止的
                if (ArrayUtils.contains(intercepted.deny(), "*")
                        || ArrayUtils.contains(intercepted.deny(), nameForUser)) {
                    continue;
                }
                // 3.2 确认最大的allow允许
                else if (!ArrayUtils.contains(intercepted.allow(), "*")
                        && !ArrayUtils.contains(intercepted.allow(), nameForUser)) {
                    continue;
                }
            }
            // 取得拦截器同意后，注册到这个控制器方法中
            if (interceptor.isForAction(controllerClass, method)) {
                registeredInterceptors.add(interceptor);
            }
        }
        //
        return registeredInterceptors
                .toArray(new NestedControllerInterceptor[registeredInterceptors.size()]);
    }

    @Override
    public int compareTo(Engine o) {
        assert o.getClass() == this.getClass();
        // 还有All放最后!
        ReqMapping rm1 = method.getAnnotation(ReqMapping.class);
        ReqMapping rm2 = ((ActionEngine) o).method.getAnnotation(ReqMapping.class);
        boolean ca1 = rm1 == null ? false : ArrayUtils.contains(rm1.methods(), ReqMethod.ALL);
        boolean ca2 = rm2 == null ? false : ArrayUtils.contains(rm2.methods(), ReqMethod.ALL);
        if (ca1 != ca2) {
            return ca1 ? 1 : -1;
        }
        // 还有@IfParamExists放前面，都含有的按方法名来区分
        IfParamExists if1 = method.getAnnotation(IfParamExists.class);
        IfParamExists if2 = ((ActionEngine) o).method.getAnnotation(IfParamExists.class);
        boolean e1 = (if1 != null);
        boolean e2 = (if2 != null);
        if (e1 != e2) {
            return (e1 ? -1 : 1);
        } else if (e1) {
            return if2.value()[0].length() - if1.value()[0].length();
        } else {
            return method.getName().length() - ((ActionEngine) o).method.getName().length();
        }
    }

    @Override
    public boolean isAccepted(HttpServletRequest request) {
        assert request != null;
        IfParamExists ifParamExists = method.getAnnotation(IfParamExists.class);
        if (ifParamExists != null) {
            String[] values = ifParamExists.value();
            assert values.length == 1;// TODO: 目前只支持1个参数配置的
            // create&form
            String[] terms = StringUtils.split(values[0], "&");
            assert terms.length == 1;
            int index = terms[0].indexOf('=');
            if (index == -1) {
                String paramValue = request.getParameter(terms[0]);
                return StringUtils.isNotBlank(paramValue);
            } else {
                String paramName = terms[0].substring(0, index).trim();
                String expected = terms[0].substring(index + 1).trim();
                String paramValue = request.getParameter(paramName);
                if (StringUtils.isBlank(expected)) {
                    // xxx=等价于xxx的
                    return paramValue != null;
                } else {
                    return expected.equals(paramValue);
                }
            }
        }
        return true;
    }

    @Override
    public Object execute(Rose rose, MatchResult mr) throws Throwable {
        try {
            return innerExecute(rose, mr);
        } catch (Throwable local) {
            throw createException(rose, local);
        }
    }

    public Object innerExecute(Rose rose, MatchResult mr) throws Throwable {
        Invocation inv = rose.getInvocation();
        inv.getRequestPath().setActionPath(mr.getValue());
        // applies http features before the resolvers
        applyHttpFeatures(inv);

        // creates parameter binding result (not bean, just simple type, like int, Integer, int[] ...
        ParameterBindingResult paramBindingResult = new ParameterBindingResult(inv);
        String paramBindingResultName = MODEL_KEY_PREFIX + paramBindingResult.getObjectName();
        inv.addModel(paramBindingResultName, paramBindingResult);

        // resolves method parameters, adds the method parameters to model
        Object[] methodParameters = methodParameterResolver.resolve(inv, paramBindingResult);
        ((InvocationBean) inv).setMethodParameters(methodParameters);
        String[] parameterNames = methodParameterResolver.getParameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i] != null && methodParameters[i] != null
                    && inv.getModel().get(parameterNames[i]) != methodParameters[i]) {
                inv.addModel(parameterNames[i], methodParameters[i]);
            }
        }

        Object instruction = null;

        ParamMetaData[] metaDatas = methodParameterResolver.getParamMetaDatas();
        // validators
        for (int i = 0; i < this.validators.length; i++) {
            if (validators[i] != null && !(methodParameters[i] instanceof Errors)) {
                Errors errors = inv.getBindingResult(parameterNames[i]);
                instruction = validators[i].validate(//
                        metaDatas[i], inv, methodParameters[i], errors);
                if (logger.isDebugEnabled()) {
                    logger.debug("do validate [" + validators[i].getClass().getName()
                            + "] and return '" + instruction + "'");
                }
                // 如果返回的instruction不是null、boolean或空串==>杯具：流程到此为止！
                if (instruction != null) {
                    if (instruction instanceof Boolean) {
                        continue;
                    }
                    if (instruction instanceof String && ((String) instruction).length() == 0) {
                        continue;
                    }
                    return instruction;
                }
                //                }
            }
        }

        // 恢复instruction为null
        instruction = null;

        // invokes before-interceptors
        boolean broken = false;
        boolean[] bitSt = new boolean[interceptors.length];
        for (int i = 0; i < interceptors.length; i++) {
            final NestedControllerInterceptor interceptor = interceptors[i];
            if (!interceptor.isForDispatcher(inv.getRequestPath().getDispatcher())) {
                continue;
            }

            // returned by before method
            rose.addAfterCompletion(interceptor);
            instruction = interceptor.before(inv);
            bitSt[i] = true;
            if (logger.isDebugEnabled()) {
                logger.debug("interceptor[" + interceptor.getName() + "] do before and return '"
                        + instruction + "'");
            }

            if (instruction != null && !Boolean.TRUE.equals(instruction)) {
                if (instruction == inv) {
                    throw new IllegalArgumentException("Don't return an inv as an instruction: "
                            + interceptor.getInterceptor().getClass().getName());
                }
                // the inv is broken

                // if false, don't render anything
                if (Boolean.FALSE.equals(instruction)) {
                    instruction = null;
                }
                broken = true;
                break; // just break, don't return
            }
        }

        if (!broken) {
            // invoke
            instruction = method.invoke(controller, methodParameters);

            // @Return
            if (instruction == null) {
                Return returnAnnotation = method.getAnnotation(Return.class);
                if (returnAnnotation != null) {
                    instruction = returnAnnotation.value();
                }
            }
        }

        Object orginInstruction = instruction;

        // after the inv
        for (int i = bitSt.length - 1; i >= 0; i--) {
            if (!bitSt[i]) {
                continue;
            }
            instruction = interceptors[i].after(inv, instruction);
            if (logger.isDebugEnabled()) {
                logger.debug("invoke interceptor.after: [" + interceptors[i].getName()
                        + "] and return '" + instruction + "'");
            }
            // 拦截器返回null的，要恢复为原instruction
            // 这个功能非常有用!!
            if (instruction == null) {
                instruction = orginInstruction;
            }
        }
        return instruction;
    }

    private Exception createException(Rose rose, Throwable exception) {
        final RequestPath requestPath = rose.getInvocation().getRequestPath();
        StringBuilder sb = new StringBuilder(1024);
        sb.append("[Rose-").append(RoseVersion.getVersion()).append("@Spring-").append(
                SpringVersion.getVersion());
        sb.append("]Error happended: ").append(requestPath.getMethod());
        sb.append(" ").append(requestPath.getUri());
        sb.append("->");
        sb.append(this).append(" params=");
        sb.append(Arrays.toString(rose.getInvocation().getMethodParameters()));
        InvocationTargetException servletException = new InvocationTargetException(exception, sb
                .toString());
        return servletException;
    }

    private void applyHttpFeatures(final Invocation inv) throws UnsupportedEncodingException {
        HttpServletRequest request = inv.getRequest();
        HttpServletResponse response = inv.getResponse();
        HttpFeatures httpFeatures = method.getAnnotation(HttpFeatures.class);
        if (httpFeatures == null) {
            httpFeatures = this.controllerClass.getAnnotation(HttpFeatures.class);
        }
        if (httpFeatures != null) {
            if (StringUtils.isNotBlank(httpFeatures.charset())) {
                response.setCharacterEncoding(httpFeatures.charset());
                if (logger.isDebugEnabled()) {
                    logger.debug("set response.characterEncoding by HttpFeatures:"
                            + httpFeatures.charset());
                }
            }
            if (StringUtils.isNotBlank(httpFeatures.contentType())) {
                String contentType = httpFeatures.contentType();
                if (contentType.equals("json")) {
                    contentType = "application/x-json";
                } else if (contentType.equals("xml")) {
                    contentType = "text/xml";
                }
                response.setContentType(contentType);
                if (logger.isDebugEnabled()) {
                    logger.debug("set response.contentType by HttpFeatures:"
                            + response.getContentType());
                }
            }
        }
        String oldEncoding = response.getCharacterEncoding();
        if (StringUtils.isBlank(oldEncoding) || oldEncoding.startsWith("ISO-")) {
            String encoding = request.getCharacterEncoding();
            assert encoding != null;
            response.setCharacterEncoding(encoding);
            if (logger.isDebugEnabled()) {
                logger.debug("set response.characterEncoding by default:"
                        + response.getCharacterEncoding());
            }
        }

        // !!不用写设置content-type代码，web容器会自动把修改后的charset加进去!
    }

    @Override
    public String toString() {
        if (toStringCache == null) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            String appPackageName = this.controllerClass.getPackage().getName();
            if (appPackageName.indexOf('.') != -1) {
                appPackageName = appPackageName.substring(0, appPackageName.lastIndexOf('.'));
            }
            String methodParamNames = "";
            for (int i = 0; i < parameterTypes.length; i++) {
                if (methodParamNames.length() == 0) {
                    methodParamNames = showSimpleName(parameterTypes[i], appPackageName);
                } else {
                    methodParamNames = methodParamNames + ", "
                            + showSimpleName(parameterTypes[i], appPackageName);
                }
            }
            toStringCache = ""//
                    //                    + this.controllerClass.getName() //
                    + showSimpleName(method.getReturnType(), appPackageName)
                    + " "
                    + method.getName() //
                    + "(" + methodParamNames + ")" //
            ;
        }
        return toStringCache;
    }

    private String showSimpleName(Class<?> parameterType, String appPackageName) {
        if (parameterType.getName().startsWith("net.paoding")
                || parameterType.getName().startsWith("java.lang")
                || parameterType.getName().startsWith("java.util")
                || parameterType.getName().startsWith(appPackageName)) {
            return parameterType.getSimpleName();
        }
        return parameterType.getName();
    }

    public void destroy() {

    }

}
