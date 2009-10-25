/*
 * $Id$
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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ParamConf;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.impl.thread.MatchResult;
import net.paoding.rose.web.var.Flash;
import net.paoding.rose.web.var.Model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.WebUtils;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ResolverFactoryImpl implements ResolverFactory {

    private static Log logger = LogFactory.getLog(MethodParameterResolver.class);

    public static final String MAP_SEPARATOR = ":";

    private static final SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter();

    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>(
            8);
    static {
        primitiveWrapperTypeMap.put(boolean.class, Boolean.class);
        primitiveWrapperTypeMap.put(byte.class, Byte.class);
        primitiveWrapperTypeMap.put(char.class, Character.class);
        primitiveWrapperTypeMap.put(double.class, Double.class);
        primitiveWrapperTypeMap.put(float.class, Float.class);
        primitiveWrapperTypeMap.put(int.class, Integer.class);
        primitiveWrapperTypeMap.put(long.class, Long.class);
        primitiveWrapperTypeMap.put(short.class, Short.class);
    }

    private static final ParamResolverBean[] buildinResolvers = new ParamResolverBean[] {//
    new InvocationResolver(), //
            new ModelResolver(), //
            new FlashResolver(), //
            new ModuleResolver(), //
            new StringResolver(), //
            new RequestResolver(), //
            new ResponseResolver(), //
            new HttpSessionResolver(), //
            new MultipartFileResolver(), //
            new MultipartRequestResolver(), //
            new MultipartHttpServletRequestResolver(), //
            new ServletContextResolver(), //
            new ArrayResolver(),//
            new ListResolver(), //
            new SetResolver(), //
            new MapResolver(), //
            new BindingResultResolver(), //
            new DateResolver(), //
            new EditorResolver(), //
            new BeanResolver(), //
    };

    private final List<ParamResolverBean> customerResolvers = new ArrayList<ParamResolverBean>();

    public void addCustomerResolver(ParamResolverBean resolver) {
        customerResolvers.add(resolver);
    }

    @Override
    public ParamResolverBean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
        for (ParamResolverBean resolver : customerResolvers) {
            if (resolver.supports(parameterType, controllerClazz, method)) {
                return resolver;
            }
        }
        for (ParamResolverBean resolver : buildinResolvers) {
            if (resolver.supports(parameterType, controllerClazz, method)) {
                return resolver;
            }
        }
        return null;
    }

    // ---------------------------------------------------------

    static final class InvocationResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return Invocation.class == parameterType;
        }

        @Override
        public Invocation resolve(Class<?> parameterType, int replicatedCount,
                int indexOfReplicated, Invocation inv, String parameterName, Param paramAnnotation) {
            return inv;
        }
    }

    static final class RequestResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return HttpServletRequest.class == parameterType
                    || ServletRequest.class == parameterType;
        }

        @Override
        public HttpServletRequest resolve(Class<?> parameterType, int replicatedCount,
                int indexOfReplicated, Invocation inv, String parameterName, Param paramAnnotation) {
            return inv.getRequest();
        }
    }

    static final class ResponseResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return HttpServletResponse.class == parameterType
                    || ServletResponse.class == parameterType;
        }

        @Override
        public ServletResponse resolve(Class<?> parameterType, int replicatedCount,
                int indexOfReplicated, Invocation inv, String parameterName, Param paramAnnotation) {
            return inv.getResponse();
        }
    }

    static final class ServletContextResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return ServletContext.class == parameterType;
        }

        @Override
        public ServletContext resolve(Class<?> parameterType, int replicatedCount,
                int indexOfReplicated, Invocation inv, String parameterName, Param paramAnnotation) {
            return inv.getServletContext();
        }
    }

    static final class HttpSessionResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return HttpSession.class == parameterType;
        }

        @Override
        public HttpSession resolve(Class<?> parameterType, int replicatedCount,
                int indexOfReplicated, Invocation inv, String parameterName, Param paramAnnotation) {
            boolean create = true;
            if (paramAnnotation != null) {
                create = paramAnnotation.required();
            }
            return inv.getRequest().getSession(create);
        }
    }

    static final class ModelResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return Model.class == parameterType;
        }

        @Override
        public Object resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
                Invocation inv, String parameterName, Param paramAnnotation) {
            return inv.getModel();
        }
    }

    static final class FlashResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return Flash.class == parameterType;
        }

        @Override
        public Flash resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
                Invocation inv, String parameterName, Param paramAnnotation) {
            return inv.getFlash();
        }
    }

    static final class ModuleResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return Module.class == parameterType;
        }

        @Override
        public Module resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
                Invocation inv, String parameterName, Param paramAnnotation) {
            return ((InvocationBean) inv).getModule();
        }
    }

    static final class StringResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return String.class == parameterType;
        }

        @Override
        public String resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
                Invocation inv, String parameterName, Param paramAnnotation) {
            return inv.getRawParameter(parameterName);
        }
    }

    static final class MultipartRequestResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return MultipartRequest.class == parameterType
                    || MultipartHttpServletRequest.class == parameterType;
        }

        @Override
        public MultipartRequest resolve(Class<?> parameterType, int replicatedCount,
                int indexOfReplicated, Invocation inv, String parameterName, Param paramAnnotation) {
            if (inv.getRequest() instanceof MultipartRequest) {
                return (MultipartRequest) inv.getRequest();
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("cann't set MultipartRequest param to method "
                            + ", the request is not a MultipartRequest");
                }
                return null;
            }
        }
    }

    static final class MultipartHttpServletRequestResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return MultipartHttpServletRequest.class == parameterType;
        }

        @Override
        public MultipartRequest resolve(Class<?> parameterType, int replicatedCount,
                int indexOfReplicated, Invocation inv, String parameterName, Param paramAnnotation) {
            if (inv.getRequest() instanceof MultipartHttpServletRequest) {
                return (MultipartHttpServletRequest) inv.getRequest();
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("cann't set MultipartRequest param to method "
                            + ", the request is not a MultipartRequest");
                }
                return null;
            }
        }
    }

    static final class MultipartFileResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return MultipartFile.class == parameterType;
        }

        @Override
        public MultipartFile resolve(Class<?> parameterType, int replicatedCount,
                int indexOfReplicated, Invocation inv, String parameterName, Param paramAnnotation) {
            MultipartFile multipartFile = null;
            if (inv.getRequest() instanceof MultipartRequest) {
                MultipartRequest multipartRequest = (MultipartRequest) inv.getRequest();
                multipartFile = multipartRequest.getFile(parameterName);
                if (multipartFile == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("not found MultipartFile named:" + parameterName);
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("cann't " + "set MultipartFile param to method "
                            + ", the request is not a MultipartRequest");
                }
            }
            return multipartFile;
        }
    }

    static final class BeanResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return !Modifier.isAbstract(parameterType.getModifiers());

        }

        @Override
        public Object resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
                Invocation inv, String parameterName, Param paramAnnotation) {
            Object bean = BeanUtils.instantiateClass(parameterType);
            ServletRequestDataBinder binder;
            if (replicatedCount == 1) {
                binder = new ServletRequestDataBinder(bean);
            } else {
                binder = new ServletRequestDataBinder(bean, parameterName);
            }
            binder.bind(inv.getRequest());
            String bindingResultName = BindingResult.MODEL_KEY_PREFIX + parameterName
                    + "BindingResult";
            inv.addModel(bindingResultName, binder.getBindingResult());
            return bean;
        }
    }

    static final class BindingResultResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return BindingResult.class == parameterType || Errors.class == parameterType;
        }

        @Override
        public BindingResult resolve(Class<?> parameterType, int replicatedCount,
                int indexOfReplicated, Invocation inv, String parameterName, Param paramAnnotation) {
            if (parameterName != null) {
                return inv.getBindingResult(parameterName);
            } else {
                return inv.getParameterBindingResult();
            }
        }
    }

    static final class ArrayResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return parameterType.isArray();
        }

        @Override
        public Object resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
                Invocation inv, String parameterName, Param paramAnnotation) {
            if (StringUtils.isNotEmpty(parameterName)) {
                Object toConvert = inv.getRequest().getParameterValues(parameterName);
                if (toConvert != null) {
                    if (((String[]) toConvert).length == 1) {
                        toConvert = ((String[]) toConvert)[0].split(",");
                    }
                    return simpleTypeConverter.convertIfNecessary(toConvert, parameterType);
                }
            }
            return Array.newInstance(parameterType.getComponentType(), 0);
        }
    }

    static abstract class CollectionResolver<T extends Collection<?>> implements ParamResolverBean {

        @Override
        public T resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
                Invocation inv, String parameterName, Param paramAnnotation) throws Exception {
            if (StringUtils.isNotEmpty(parameterName)) {
                Class<?> componentType = ((InvocationBean) inv).getActionEngine()
                        .getParameterGenericTypes(parameterName)[0];
                Object toConvert = inv.getRequest().getParameterValues(parameterName);
                if (toConvert != null) {
                    if (((String[]) toConvert).length == 1) {
                        toConvert = ((String[]) toConvert)[0].split(","); // 去掉数组，变为一个String，converter将按逗号切割
                    }
                    if (componentType != String.class) {
                        toConvert = simpleTypeConverter.convertIfNecessary(toConvert, Array
                                .newInstance(componentType, 0).getClass());
                    }
                    return convertFromArray(parameterType, (Object[]) toConvert);
                }
            }
            return convertFromArray(parameterType, new Object[0]);
        }

        protected abstract T convertFromArray(Class<?> parameterType, Object[] toConvert)
                throws Exception;
    }

    static final class ListResolver extends CollectionResolver<List<?>> {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return List.class == parameterType
                    || Collection.class == parameterType
                    || (!Modifier.isAbstract(parameterType.getModifiers()) && List.class
                            .isAssignableFrom(parameterType));
        }

        @Override
        @SuppressWarnings("unchecked")
        protected List<?> convertFromArray(Class<?> parameterType, Object[] toConvert)
                throws InstantiationException, IllegalAccessException, IllegalArgumentException,
                SecurityException, InvocationTargetException, NoSuchMethodException {
            Collection<Object> param;
            if (parameterType.isInterface()) {
                param = new ArrayList<Object>(toConvert.length);
            } else {
                param = (Collection<Object>) parameterType.getConstructor().newInstance();
            }
            Collections.addAll(param, toConvert);
            return (List<?>) param;
        }
    }

    static final class SetResolver extends CollectionResolver<Set<?>> {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return Set.class == parameterType
                    || (!Modifier.isAbstract(parameterType.getModifiers()) && Set.class
                            .isAssignableFrom(parameterType));
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Set<?> convertFromArray(Class<?> parameterType, Object[] toConvert)
                throws InstantiationException, IllegalAccessException, IllegalArgumentException,
                SecurityException, InvocationTargetException, NoSuchMethodException {
            Collection<Object> param;
            if (parameterType.isInterface()) {
                param = new HashSet<Object>(toConvert.length);
            } else {
                param = (Collection<Object>) parameterType.getConstructor().newInstance();
            }
            Collections.addAll(param, toConvert);
            return (Set<?>) param;
        }
    }

    static final class MapResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return Map.class == parameterType || HashMap.class == parameterType;
        }

        @Override
        public Map<?, ?> resolve(Class<?> parameterType, int replicatedCount,
                int indexOfReplicated, Invocation inv, String parameterName, Param paramAnnotation) {
            if (StringUtils.isNotEmpty(parameterName)) {
                Class<?>[] genericTypes = ((InvocationBean) inv).getActionEngine()
                        .getParameterGenericTypes(parameterName);
                Class<?> keyType = genericTypes[0];
                Class<?> valueType = genericTypes[1];
                Map<?, ?> toConvert = WebUtils.getParametersStartingWith(inv.getRequest(),
                        parameterName + MAP_SEPARATOR);
                if (toConvert != null) {
                    if (keyType != String.class || valueType != String.class) {
                        Map<Object, Object> ret = new HashMap<Object, Object>();
                        for (Map.Entry<?, ?> entry : toConvert.entrySet()) {
                            Object key = entry.getKey();
                            Object value = entry.getValue();
                            if (keyType != String.class) {
                                key = simpleTypeConverter.convertIfNecessary(key, keyType);
                            }
                            if (valueType != String.class) {
                                value = simpleTypeConverter.convertIfNecessary(value, valueType);
                            }
                            ret.put(key, value);
                        }
                        return ret;
                    }
                    return toConvert;
                }

            }
            return new HashMap<Object, Object>(2);
        }
    }

    static final class DateResolver implements ParamResolverBean {

        private final static String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

        private final static String dateTimePattern2 = "yyyy/MM/dd HH:mm:ss";

        private final static String dateTimePattern3 = "yyyyMMddHHmmss";

        private final static String datePattern = "yyyy-MM-dd";

        private final static String datePattern2 = "yyyy/MM/dd";

        private final static String timePattern = "HH:mm:ss";

        private final static String stimePattern = "HH:mm";

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return Date.class == parameterType || java.sql.Date.class == parameterType
                    || java.sql.Time.class == parameterType
                    || java.sql.Timestamp.class == parameterType;
        }

        @Override
        public Date resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
                Invocation inv, String parameterName, Param paramAnnotation) throws Exception {
            Date date = resolveUtilDate(parameterType, inv, parameterName, paramAnnotation);
            if (date == null) {
                return date;
            }
            if (java.sql.Date.class == parameterType) {
                date = new java.sql.Date(date.getTime());
            } else if (java.sql.Time.class == parameterType) {
                date = new java.sql.Time(date.getTime());
            } else if (java.sql.Timestamp.class == parameterType) {
                date = new java.sql.Timestamp(date.getTime());
            }
            return date;
        }

        protected Date resolveUtilDate(Class<?> parameterType, Invocation inv,
                String parameterName, Param paramAnnotation) throws ParseException {
            String text = inv.getRawParameter(parameterName);
            if (StringUtils.isEmpty(text)) {
                if (paramAnnotation != null && !"~".equals(paramAnnotation.def())) {
                    text = paramAnnotation.def();
                    if (StringUtils.isEmpty(text)) {
                        return new Date(); // 当前时间!
                    }
                } else {
                    return null; // 保留null，而非当前时间
                }
            }
            if (paramAnnotation != null && paramAnnotation.conf() != null
                    && paramAnnotation.conf().length > 0) {
                ParamConf[] conf = paramAnnotation.conf();
                for (ParamConf paramConf : conf) {
                    if ("pattern".equals(paramConf.name())) {
                        // 如果都找不到pattern则使用parseLong，但是总可能存在意外，
                        // 比如dateTimePattern2也全部都是数字，那是使用它，还是parseLong?
                        // 通过把patter定义为long，就明确一定是用parseLong,也建议实际情况应如此定义
                        if ("long".equals(paramConf.value())) {
                            boolean digit = true;
                            for (int i = 0; i < text.length(); i++) {
                                if (!Character.isDigit(text.charAt(i))) {
                                    digit = false;
                                    break;
                                }
                            }
                            if (digit) {
                                return new Date(Long.parseLong(text));
                            }
                        }
                        // 可以配置多个pattern!! 通过长度匹配
                        if (text.length() == paramConf.value().length()) {
                            return new SimpleDateFormat(paramConf.value()).parse(text);
                        }
                    }
                }
            }
            if (text.length() == dateTimePattern.length()) {
                if (text.charAt(4) == '-' && text.charAt(7) == '-') {
                    return new SimpleDateFormat(dateTimePattern).parse(text);
                }
                if (text.charAt(4) == '/' && text.charAt(7) == '/') {
                    if (text.charAt(13) == ':' && text.charAt(16) == ':') {
                        return new SimpleDateFormat(dateTimePattern2).parse(text);
                    }
                }
            } else if (text.length() == dateTimePattern3.length()) {
                return new SimpleDateFormat(dateTimePattern3).parse(text);
            } else if (text.length() == datePattern.length()) {
                if (text.charAt(4) == '-' && text.charAt(7) == '-') {
                    return new SimpleDateFormat(datePattern).parse(text);
                }
                if (text.charAt(4) == '/' && text.charAt(7) == '/') {
                    return new SimpleDateFormat(datePattern2).parse(text);
                }
            } else if (text.length() == timePattern.length()) {
                if (text.charAt(2) == ':' && text.charAt(5) == ':') {
                    return new SimpleDateFormat(timePattern).parse(text);
                }
            } else if (text.length() == stimePattern.length()) {
                if (text.charAt(2) == ':') {
                    return new SimpleDateFormat(stimePattern).parse(text);
                }
            }
            return new Date(Long.parseLong(text));
        }
    }

    static final class EditorResolver implements ParamResolverBean {

        @Override
        public boolean supports(Class<?> parameterType, Class<?> controllerClazz, Method method) {
            return ClassUtils.isPrimitiveOrWrapper(parameterType)
                    || simpleTypeConverter.findCustomEditor(parameterType, null) != null
                    || simpleTypeConverter.getDefaultEditor(parameterType) != null;
        }

        @Override
        public Object resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
                Invocation inv, String parameterName, Param paramAnnotation) {
            String toConvert = null;
            if (paramAnnotation != null && "$".equals(paramAnnotation.value())) {
                MatchResult<?> mr = ((InvocationBean) inv).getActionMatchResult();
                int index = Integer.parseInt(parameterName.substring(1)) - 1;
                if (index < mr.getParameterCount()) {
                    toConvert = mr.getParameter(index);
                }
            } else if (parameterName != null) {
                toConvert = inv.getRawParameter(parameterName);
            }
            if (toConvert == null) {
                if (paramAnnotation != null && !"~".equals(paramAnnotation.def())) {
                    toConvert = paramAnnotation.def();
                }
            }
            if (toConvert != null) {
                return simpleTypeConverter.convertIfNecessary(toConvert, parameterType);
            }
            if (parameterType.isPrimitive()) {
                return simpleTypeConverter.convertIfNecessary("0", parameterType);
            }
            return null;
        }
    }

}
