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

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.FlashParam;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.impl.validation.ParameterBindingResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.FieldError;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public final class MethodParameterResolver {

    private static Log logger = LogFactory.getLog(MethodParameterResolver.class);

    private static final TypeConverter typeConverter = new ThreadSafedSimpleTypeConverter();

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
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterTypes.length; i++) {
            ParamMetaDataImpl paramMetaData = new ParamMetaDataImpl(controllerClazz, method,
                    parameterTypes[i], parameterNames[i], i);
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof Param) {
                    paramMetaData.setParamAnnotation(Param.class.cast(annotation));
                } else if (annotation instanceof FlashParam) {
                    paramMetaData.setFlashParamAnnotation(FlashParam.class.cast(annotation));
                }
            }
            paramMetaDatas[i] = paramMetaData;
            resolvers[i] = resolverFactory.supports(paramMetaData);
        }
    }

    public ParamMetaData[] getParamMetaDatas() {
        return paramMetaDatas;
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

    public Object[] resolve(final Invocation inv,
            final ParameterBindingResult parameterBindingResult) throws Exception {
        Object[] parameters = new Object[paramMetaDatas.length];
        for (int i = 0; i < resolvers.length; i++) {
            if (resolvers[i] == null) {
                continue;
            }
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Resolves parameter "
                            + paramMetaDatas[i].getParamType().getSimpleName() + " using "
                            + resolvers[i].getClass().getName());
                }
                parameters[i] = resolvers[i].resolve(inv, paramMetaDatas[i]);
                // afterPropertiesSet
                if (parameters[i] instanceof InitializingBean) {
                    ((InitializingBean) parameters[i]).afterPropertiesSet();
                }
            } catch (TypeMismatchException e) {
                // 出现这个错误肯定是解析一般参数失败导致的，而非bean里面的某个属性值的解析失败

                logger.debug("", e);

                // 对简单类型的参数，设置一个默认值给它以支持对该方法的继续调用
                if (paramMetaDatas[i].getParamType().isPrimitive()) {
                    Param paramAnnotation = paramMetaDatas[i].getParamAnnotation();
                    if (paramAnnotation == null || Param.JAVA_DEFAULT.equals(paramAnnotation.def())) {
                        // 对这最常用的类型做一下if-else判断，其他类型就简单使用converter来做吧
                        if (paramMetaDatas[i].getParamType() == int.class) {
                            parameters[i] = Integer.valueOf(0);
                        } else if (paramMetaDatas[i].getParamType() == long.class) {
                            parameters[i] = Long.valueOf(0);
                        } else if (paramMetaDatas[i].getParamType() == boolean.class) {
                            parameters[i] = Boolean.FALSE;
                        } else if (paramMetaDatas[i].getParamType() == double.class) {
                            parameters[i] = Double.valueOf(0);
                        } else if (paramMetaDatas[i].getParamType() == float.class) {
                            parameters[i] = Float.valueOf(0);
                        } else {

                            parameters[i] = typeConverter.convertIfNecessary("0", paramMetaDatas[i]
                                    .getParamType());
                        }
                    } else {
                        parameters[i] = typeConverter.convertIfNecessary(paramAnnotation.def(),
                                paramMetaDatas[i].getParamType());
                    }
                }
                // 

                FieldError fieldError = new FieldError(//
                        "method", // 该出错字段所在的对象的名字；对于这类异常我们统一规定名字为method
                        parameterNames[i], // 出错的字段的名字；取其参数名
                        inv.getRawParameter(parameterNames[i]), // 被拒绝的值
                        true,//whether this error represents a binding failure (like a type mismatch); else, it is a validation failure
                        new String[] { e.getErrorCode() },// "typeMismatch"
                        new String[] { inv.getRawParameter(parameterNames[i]) }, //the array of arguments to be used to resolve this message
                        null // the default message to be used to resolve this message
                );
                parameterBindingResult.addError(fieldError);
            } catch (Exception e) {
                // 什么错误呢？比如很有可能是构造对象不能成功导致的错误，没有默认构造函数、构造函数执行失败等等
                logger.error("", e);
                throw e;
            }
        }
        return parameters;
    }

}
