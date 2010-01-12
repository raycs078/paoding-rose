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

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
import net.paoding.rose.web.annotation.FlashParam;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ParamConf;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.impl.thread.Rose;
import net.paoding.rose.web.var.Flash;
import net.paoding.rose.web.var.Model;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.WebUtils;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ResolverFactoryImpl implements ResolverFactory {

    private static Log logger = LogFactory.getLog(MethodParameterResolver.class);

    private static final TypeConverter typeConverter = new ThreadSafedSimpleTypeConverter();

    public static final String MAP_SEPARATOR = ":";

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

    private static final Map<Class<?>, Class<?>> arrayTypeMap = new HashMap<Class<?>, Class<?>>();
    static {
        arrayTypeMap.put(boolean.class, Boolean.class);
        arrayTypeMap.put(byte.class, byte[].class);
        arrayTypeMap.put(char.class, char[].class);
        arrayTypeMap.put(double.class, double[].class);
        arrayTypeMap.put(float.class, float[].class);
        arrayTypeMap.put(int.class, int[].class);
        arrayTypeMap.put(long.class, long[].class);
        arrayTypeMap.put(short.class, short[].class);

        arrayTypeMap.put(Boolean.class, Boolean[].class);
        arrayTypeMap.put(Byte.class, Byte[].class);
        arrayTypeMap.put(Character.class, Character[].class);
        arrayTypeMap.put(Double.class, Double[].class);
        arrayTypeMap.put(Float.class, Float[].class);
        arrayTypeMap.put(Integer.class, Integer[].class);
        arrayTypeMap.put(Long.class, Long[].class);
        arrayTypeMap.put(Short.class, Short[].class);
    }

    private static final ParamResolver[] buildinResolvers = new ParamResolver[] {//
    new InvocationResolver(), //
            new RoseResolver(), //
            new ApplicationContextResolver(), //
            new MessageSourceResolver(), //
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

    public ResolverFactoryImpl() {
    }

    private final List<ParamResolver> customerResolvers = new ArrayList<ParamResolver>();

    public void addCustomerResolver(ParamResolver resolver) {
        customerResolvers.add(resolver);
    }

    @Override
    public ParamResolver supports(ParamMetaData paramMetaData) {
        for (ParamResolver resolver : customerResolvers) {
            if (resolver.supports(paramMetaData)) {
                return resolver;
            }
        }
        for (ParamResolver resolver : buildinResolvers) {
            if (resolver.supports(paramMetaData)) {
                return resolver;
            }
        }
        return null;
    }

    // ---------------------------------------------------------

    static final class InvocationResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return Invocation.class == paramMetaData.getParamType();
        }

        @Override
        public Invocation resolve(Invocation inv, ParamMetaData paramMetaData) {
            return inv;
        }
    }

    static final class ApplicationContextResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return ApplicationContext.class == paramMetaData.getParamType()
                    || WebApplicationContext.class == paramMetaData.getParamType();
        }

        @Override
        public ApplicationContext resolve(Invocation inv, ParamMetaData paramMetaData) {
            return inv.getApplicationContext();
        }
    }

    static final class MessageSourceResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return MessageSource.class == paramMetaData.getParamType();
        }

        @Override
        public MessageSource resolve(Invocation inv, ParamMetaData paramMetaData) {
            return inv.getApplicationContext();
        }
    }

    static final class RequestResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return HttpServletRequest.class == paramMetaData.getParamType()
                    || ServletRequest.class == paramMetaData.getParamType();
        }

        @Override
        public HttpServletRequest resolve(Invocation inv, ParamMetaData paramMetaData) {
            return inv.getRequest();
        }
    }

    static final class ResponseResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return HttpServletResponse.class == paramMetaData.getParamType()
                    || ServletResponse.class == paramMetaData.getParamType();
        }

        @Override
        public ServletResponse resolve(Invocation inv, ParamMetaData paramMetaData) {
            return inv.getResponse();
        }
    }

    static final class ServletContextResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return ServletContext.class == paramMetaData.getParamType();
        }

        @Override
        public ServletContext resolve(Invocation inv, ParamMetaData paramMetaData) {
            return inv.getServletContext();
        }
    }

    static final class HttpSessionResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return HttpSession.class == paramMetaData.getParamType();
        }

        @Override
        public HttpSession resolve(Invocation inv, ParamMetaData paramMetaData) {
            boolean create = true;
            if (paramMetaData.getParamAnnotation() != null) {
                create = paramMetaData.getParamAnnotation().required();
            }
            return inv.getRequest().getSession(create);
        }
    }

    static final class ModelResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return Model.class == paramMetaData.getParamType();
        }

        @Override
        public Object resolve(Invocation inv, ParamMetaData paramMetaData) {
            return inv.getModel();
        }
    }

    static final class FlashResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return Flash.class == paramMetaData.getParamType();
        }

        @Override
        public Flash resolve(Invocation inv, ParamMetaData paramMetaData) {
            return inv.getFlash();
        }
    }

    static final class ModuleResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return Module.class == paramMetaData.getParamType();
        }

        @Override
        public Module resolve(Invocation inv, ParamMetaData paramMetaData) {
            return ((InvocationBean) inv).getModule();
        }
    }

    static final class StringResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return String.class == paramMetaData.getParamType();
        }

        @Override
        public String resolve(Invocation inv, ParamMetaData paramMetaData) {
            return inv.getRawParameter(paramMetaData.getParamName());
        }
    }

    static final class MultipartRequestResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return MultipartRequest.class == paramMetaData.getParamType()
                    || MultipartHttpServletRequest.class == paramMetaData.getParamType();
        }

        @Override
        public MultipartRequest resolve(Invocation inv, ParamMetaData paramMetaData) {
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

    static final class MultipartHttpServletRequestResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return MultipartHttpServletRequest.class == paramMetaData.getParamType();
        }

        @Override
        public MultipartRequest resolve(Invocation inv, ParamMetaData paramMetaData) {
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

    static final class MultipartFileResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return MultipartFile.class == paramMetaData.getParamType();
        }

        @Override
        public MultipartFile resolve(Invocation inv, ParamMetaData paramMetaData) {
            MultipartFile multipartFile = null;
            if (inv.getRequest() instanceof MultipartRequest) {
                MultipartRequest multipartRequest = (MultipartRequest) inv.getRequest();
                multipartFile = multipartRequest.getFile(paramMetaData.getParamName());
                if (multipartFile == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("not found MultipartFile named:"
                                + paramMetaData.getParamName());
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

    static final class RoseResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData metaData) {
            return metaData.getParamType() == Rose.class;
        }

        @Override
        public Object resolve(Invocation inv, ParamMetaData metaData) throws Exception {
            return ((InvocationBean) inv).getRose();
        }
    }

    static final class BeanResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return !Modifier.isAbstract(paramMetaData.getParamType().getModifiers());

        }

        @Override
        public Object resolve(Invocation inv, ParamMetaData paramMetaData) {
            Object bean = BeanUtils.instantiateClass(paramMetaData.getParamType());
            ServletRequestDataBinder binder;
            if (paramMetaData.getParamAnnotation() == null) {
                binder = new ServletRequestDataBinder(bean);
            } else {
                binder = new ServletRequestDataBinder(bean, paramMetaData.getParamName());
            }
            binder.bind(inv.getRequest());
            String bindingResultName = BindingResult.MODEL_KEY_PREFIX
                    + paramMetaData.getParamName() + "BindingResult";
            inv.addModel(bindingResultName, binder.getBindingResult());
            return bean;
        }
    }

    static final class BindingResultResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return BindingResult.class == paramMetaData.getParamType()
                    || Errors.class == paramMetaData.getParamType();
        }

        @Override
        public BindingResult resolve(Invocation inv, ParamMetaData paramMetaData) {
            if (paramMetaData.getParamName() != null) {
                return inv.getBindingResult(paramMetaData.getParamName());
            } else {
                return inv.getParameterBindingResult();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Object resolveArray(Invocation inv, ParamMetaData paramMetaData,
            Class<?> compnentType) {
        if (compnentType == MultipartFile.class) {
            String filterName = paramMetaData.getParamName();
            if (filterName == null) {
                filterName = "";
            }
            if (inv.getRequest() instanceof MultipartRequest) {
                List<MultipartFile> files = new LinkedList<MultipartFile>();
                MultipartRequest multipartRequest = (MultipartRequest) inv.getRequest();
                Iterator<String> names = multipartRequest.getFileNames();
                while (names.hasNext()) {
                    String name = names.next();
                    if (name.startsWith(filterName)) {
                        files.add(multipartRequest.getFile(name));
                    }
                }
                return files.toArray(new MultipartFile[0]);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("cann't " + "set MultipartFile param to method "
                            + ", the request is not a MultipartRequest");
                }
            }
        } else if (StringUtils.isNotEmpty(paramMetaData.getParamName())) {
            Object toConvert = inv.getRequest().getParameterValues(paramMetaData.getParamName());
            if (toConvert != null) {
                if (((String[]) toConvert).length == 1) {
                    toConvert = ((String[]) toConvert)[0].split(",");
                }
                Class<?> arrayType;
                if (paramMetaData.getParamType().isArray()) {
                    arrayType = paramMetaData.getParamType();
                } else {
                    arrayType = arrayTypeMap.get(compnentType);
                    if (arrayType == null) {
                        arrayType = Array.newInstance((Class<?>) compnentType, 0).getClass();
                    }
                }
                Object array = typeConverter.convertIfNecessary(toConvert, arrayType);
                return array;
            }
        }
        return Array.newInstance((Class<?>) compnentType, 0);
    }

    static final class ArrayResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData paramMetaData) {
            return paramMetaData.getParamType().isArray();
        }

        @Override
        public Object resolve(Invocation inv, ParamMetaData paramMetaData) {
            return resolveArray(inv, paramMetaData, paramMetaData.getParamType().getComponentType());
        }

    }

    static abstract class CollectionResolver<T extends Collection<?>> implements ParamResolver {

        @Override
        public final boolean supports(ParamMetaData metaData) {
            if (innerSupports(metaData)) {
                Class<?>[] generics = compileGenericParameterTypesDetail(metaData.getMethod(),
                        metaData.getIndex());
                if (generics == null || generics.length == 0) {
                    throw new IllegalArgumentException("please use generic for "
                            + metaData.getParamType().getName() + " ["
                            + metaData.getControllerClass().getName() + "."
                            + metaData.getMethod().getName() + "]");
                }
                metaData.setUserObject(this, generics[0]);
                return true;
            }
            return false;
        }

        public abstract boolean innerSupports(ParamMetaData metaData);

        @Override
        @SuppressWarnings("unchecked")
        public Object resolve(Invocation inv, ParamMetaData paramMetaData) throws Exception {
            Object array = resolveArray(inv, paramMetaData, (Class<?>) paramMetaData
                    .getUserObject(this));
            int len = ArrayUtils.getLength(array);
            Collection collection = create(paramMetaData, len);
            for (int i = 0; i < len; i++) {
                collection.add(Array.get(array, i));
            }
            return collection;
        }

        protected abstract Collection<?> create(ParamMetaData paramMetaData, int len)
                throws Exception;
    }

    static final class ListResolver extends CollectionResolver<List<?>> {

        @Override
        public boolean innerSupports(ParamMetaData paramMetaData) {
            return List.class == paramMetaData.getParamType()
                    || Collection.class == paramMetaData.getParamType()
                    || (!Modifier.isAbstract(paramMetaData.getParamType().getModifiers()) && List.class
                            .isAssignableFrom(paramMetaData.getParamType()));
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Collection create(ParamMetaData paramMetaData, int len) throws Exception {
            if (paramMetaData.getParamType().isInterface()) {
                return new ArrayList<Object>(len);
            } else {
                return (Collection<?>) paramMetaData.getParamType().getConstructor().newInstance();
            }
        }
    }

    static final class SetResolver extends CollectionResolver<Set<?>> {

        @Override
        public boolean innerSupports(ParamMetaData metaData) {
            return Set.class == metaData.getParamType()
                    || (!Modifier.isAbstract(metaData.getParamType().getModifiers()) && Set.class
                            .isAssignableFrom(metaData.getParamType()));
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Collection create(ParamMetaData metaData, int len) throws Exception {
            if (metaData.getParamType().isInterface()) {
                return new HashSet<Object>(len);
            } else {
                return (Collection<?>) metaData.getParamType().getConstructor().newInstance();
            }
        }
    }

    private static Class<?>[] compileGenericParameterTypesDetail(Method method, int index) {
        Type genericParameterType = method.getGenericParameterTypes()[index];
        ArrayList<Class<?>> typeDetailList = new ArrayList<Class<?>>();
        if (genericParameterType instanceof ParameterizedType) {
            ParameterizedType aType = (ParameterizedType) genericParameterType;
            Type[] parameterArgTypes = aType.getActualTypeArguments();
            for (Type parameterArgType : parameterArgTypes) {
                if (parameterArgType instanceof Class) {
                    typeDetailList.add((Class<?>) parameterArgType);
                } else {
                    typeDetailList.add(String.class);
                }
            }
            Class<?>[] types = new Class[typeDetailList.size()];
            typeDetailList.toArray(types);
            return types;
        }
        return null;
    }

    static final class MapResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData metaData) {
            boolean supports = Map.class == metaData.getParamType()
                    || HashMap.class == metaData.getParamType();
            if (supports) {
                Class<?>[] generics = compileGenericParameterTypesDetail(metaData.getMethod(),
                        metaData.getIndex());
                if (generics == null || generics.length == 0) {
                    throw new IllegalArgumentException("please use generic for "
                            + metaData.getParamType().getName() + " ["
                            + metaData.getControllerClass().getName() + "."
                            + metaData.getMethod().getName() + "]");
                }
                metaData.setUserObject(this, generics);
            }
            return supports;
        }

        @Override
        public Map<?, ?> resolve(Invocation inv, ParamMetaData metaData) {
            if (StringUtils.isNotEmpty(metaData.getParamName())) {
                Class<?>[] genericTypes = (Class[]) metaData.getUserObject(this);
                Class<?> keyType = genericTypes[0];
                Class<?> valueType = genericTypes[1];
                Map<?, ?> toConvert = WebUtils.getParametersStartingWith(inv.getRequest(), metaData
                        .getParamName()
                        + MAP_SEPARATOR);
                if (toConvert != null) {
                    if (keyType != String.class || valueType != String.class) {
                        Map<Object, Object> ret = new HashMap<Object, Object>();
                        for (Map.Entry<?, ?> entry : toConvert.entrySet()) {
                            Object key = entry.getKey();
                            Object value = entry.getValue();
                            if (keyType != String.class) {
                                key = typeConverter.convertIfNecessary(key, keyType);
                            }
                            if (valueType != String.class) {
                                value = typeConverter.convertIfNecessary(value, valueType);
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

    static class DateEditor extends PropertyEditorSupport {

        private Class<?> targetType;

        public DateEditor(Class<?> targetType) {
            this.targetType = targetType;
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            try {
                setValue(DatePatterns.changeType(DatePatterns.parse(text), targetType));
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }

    }

    static final class DatePatterns {

        private final static String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

        private final static String dateTimePattern2 = "yyyy/MM/dd HH:mm:ss";

        private final static String dateTimePattern3 = "yyyyMMddHHmmss";

        private final static String datePattern = "yyyy-MM-dd";

        private final static String datePattern2 = "yyyy/MM/dd";

        private final static String datePattern3 = "yyyy-MM";

        private final static String timePattern = "HH:mm:ss";

        private final static String stimePattern = "HH:mm";

        private static Date parse(String text) throws ParseException {
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
            } else if (text.length() == datePattern3.length()) {
                return new SimpleDateFormat(datePattern3).parse(text);
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

        private static Date changeType(Date date, Class<?> targetType) {
            if (date == null) {
                return date;
            }
            if (java.sql.Date.class == targetType) {
                date = new java.sql.Date(date.getTime());
            } else if (java.sql.Time.class == targetType) {
                date = new java.sql.Time(date.getTime());
            } else if (java.sql.Timestamp.class == targetType) {
                date = new java.sql.Timestamp(date.getTime());
            }
            return date;
        }

    }

    static final class DateResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData metaData) {
            return Date.class == metaData.getParamType()
                    || java.sql.Date.class == metaData.getParamType()
                    || java.sql.Time.class == metaData.getParamType()
                    || java.sql.Timestamp.class == metaData.getParamType();
        }

        @Override
        public Date resolve(Invocation inv, ParamMetaData metaData) throws Exception {
            String text = inv.getRawParameter(metaData.getParamName());

            Date date = resolveUtilDate(text, metaData);
            return DatePatterns.changeType(date, metaData.getParamType());
        }

        protected Date resolveUtilDate(String text, ParamMetaData metaData) throws ParseException {
            if (StringUtils.isEmpty(text)) {
                if (metaData.getParamAnnotation() != null
                        && !Param.JAVA_DEFAULT.equals(metaData.getParamAnnotation().def())) {
                    text = metaData.getParamAnnotation().def();
                    if (StringUtils.isEmpty(text)) {
                        return new Date(); // 当前时间!
                    }
                } else {
                    return null; // 保留null，而非当前时间
                }
            }
            if (metaData.getParamAnnotation() != null
                    && metaData.getParamAnnotation().conf() != null
                    && metaData.getParamAnnotation().conf().length > 0) {
                ParamConf[] conf = metaData.getParamAnnotation().conf();
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
            return DatePatterns.parse(text);
        }
    }

    static final class EditorResolver implements ParamResolver {

        @Override
        public boolean supports(ParamMetaData metaData) {
            if (ClassUtils.isPrimitiveOrWrapper(metaData.getParamType())) {
                return true;
            }
            SimpleTypeConverter simpleTypeConverter = ((ThreadSafedSimpleTypeConverter) typeConverter)
                    .getSimpleTypeConverter();
            return simpleTypeConverter.findCustomEditor(metaData.getParamType(), null) != null
                    || simpleTypeConverter.getDefaultEditor(metaData.getParamType()) != null;
        }

        @Override
        public Object resolve(Invocation inv, ParamMetaData metaData) {
            String toConvert = null;
            // 
            FlashParam flashParam = metaData.getFlashParamAnnotation();
            Param param = metaData.getParamAnnotation();
            if (flashParam != null) {
                toConvert = inv.getFlash().get(flashParam.value());
            }
            if (toConvert == null && param != null) {
                toConvert = inv.getRawParameter(param.value());
            }
            if (toConvert == null) {
                if (param != null && !Param.JAVA_DEFAULT.equals(param.def())) {
                    toConvert = metaData.getParamAnnotation().def();
                }
            }
            if (toConvert != null) {
                return typeConverter.convertIfNecessary(toConvert, metaData.getParamType());
            }
            if (metaData.getParamType().isPrimitive()) {
                // 对这最常用的类型做一下if-else判断，其他类型就简单使用converter来做吧
                if (metaData.getParamType() == int.class) {
                    return Integer.valueOf(0);
                } else if (metaData.getParamType() == long.class) {
                    return Long.valueOf(0);
                } else if (metaData.getParamType() == boolean.class) {
                    return Boolean.FALSE;
                } else if (metaData.getParamType() == double.class) {
                    return Double.valueOf(0);
                } else if (metaData.getParamType() == float.class) {
                    return Float.valueOf(0);
                } else {
                    return typeConverter.convertIfNecessary("0", metaData.getParamType());
                }
            }
            return null;
        }
    }

}
