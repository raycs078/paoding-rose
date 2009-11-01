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
package net.paoding.rose.web.paramresolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.paoding.rose.web.annotation.FlashParam;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.impl.validation.ParameterBindingResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.validation.ObjectError;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public final class MethodParameterResolver {

    private static final SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter();

    private static Log logger = LogFactory.getLog(MethodParameterResolver.class);

    // ---------------------------------------------------------

    private final Method method;

    private final String[] parameterNames;

    private final ParamResolver[] resolvers;

    private final ParamMetaData[] paramMetaDatas;

    public MethodParameterResolver(Class<?> controllerClazz, Method method,
            ParameterNameDiscovererImpl parameterNameDiscoverer, ResolverFactory resolverFactory) {
        this.method = method;
        Class<?>[] parameterTypes = method.getParameterTypes();
        parameterNames = parameterNameDiscoverer.getParameterNames(method);
        resolvers = new ParamResolver[parameterTypes.length];
        paramMetaDatas = new ParamMetaData[parameterTypes.length];
        // 
        int[][] replicatedResolverCount = new int[parameterTypes.length][2];
        for (int i = 0; i < parameterTypes.length; i++) {
            replicatedResolverCount[i][0] = 1;
            replicatedResolverCount[i][1] = 0;
            for (int j = 0; j < i; j++) {
                if (parameterTypes[i] == parameterTypes[j]) {
                    replicatedResolverCount[j][0] += 1;
                    replicatedResolverCount[i][0] += 1;
                    replicatedResolverCount[i][1] += 1;
                }
            }
        }
        //
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterTypes.length; i++) {
            ParamMetaDataImpl paramMetaData = new ParamMetaDataImpl(controllerClazz, method,
                    parameterTypes[i], parameterNames[i], replicatedResolverCount[i][0],
                    replicatedResolverCount[i][1]);
            paramMetaDatas[i] = paramMetaData;
            resolvers[i] = resolverFactory.supports(paramMetaData);
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof Param) {
                    paramMetaData.setParamAnnotation(Param.class.cast(annotation));
                } else if (annotation instanceof FlashParam) {
                    paramMetaData.setFlashParamAnnotation(FlashParam.class.cast(annotation));
                }
            }
        }
    }

    public String[] getParameterNames() {
        return parameterNames;
    }

    public Method getMethod() {
        return method;
    }

    public Param getParamAnnotationAt(int index) {
        return this.paramMetaDatas[index].getParamAnnotation();
    }

    // ---------------------------------------------------------

    public Object[] resolve(final InvocationBean inv,
            final ParameterBindingResult parameterBindingResult) throws Exception {
        Object[] parameters = new Object[paramMetaDatas.length];
        for (int i = 0; i < resolvers.length; i++) {
            if (resolvers[i] == null) {
                continue;
            }
            String parameterName = parameterNames[i];
            Class<?> parameterType = paramMetaDatas[i].getParamType();
            try {
                if (parameterName == null) {
                    if (ClassUtils.isPrimitiveOrWrapper(parameterType)) {
                        parameters[i] = simpleTypeConverter.convertIfNecessary("0", parameterType);
                        continue;
                    } else if (parameterType == String.class) {
                        parameters[i] = null;
                        continue;
                    } else {
                        parameterName = parameterType.getSimpleName()
                                + paramMetaDatas[i].getIndexOfReplicated();
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Resolves parameter " + parameterName + ":"
                            + parameterType.getSimpleName() + " using "
                            + resolvers[i].getClass().getName());
                }
                parameters[i] = resolvers[i].resolve(inv, paramMetaDatas[i]);
                // afterPropertiesSet
                if (parameters[i] instanceof InitializingBean) {
                    ((InitializingBean) parameters[i]).afterPropertiesSet();
                }
            } catch (TypeMismatchException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("", e);
                }
                parameterBindingResult.rejectValue(parameterName, "convert.failed", new Object[] {
                        parameterName, e }, e.getMessage());
                if (parameterType.isPrimitive()) {
                    Param paramAnnotation = paramMetaDatas[i].getParamAnnotation();
                    if (paramAnnotation != null && !"~".equals(paramAnnotation.def())) {
                        parameters[i] = simpleTypeConverter.convertIfNecessary(paramAnnotation
                                .def(), parameterType);
                    } else {
                        parameters[i] = simpleTypeConverter.convertIfNecessary("0", parameterType);
                    }
                }
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("", e);
                }
                parameterBindingResult.addError(new ObjectError(parameterName, e.getMessage()));
            }
        }
        return parameters;
    }

}
