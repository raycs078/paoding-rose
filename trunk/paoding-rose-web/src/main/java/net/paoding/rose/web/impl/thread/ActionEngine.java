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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.RoseVersion;
import net.paoding.rose.web.InterceptorDelegate;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationChain;
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
import org.springframework.util.Assert;
import org.springframework.validation.Errors;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public final class ActionEngine implements Engine {

    private final static Log logger = LogFactory.getLog(ActionEngine.class);

    private final Module module;

    private final Class<?> controllerClass;

    private final Object controller;

    private final Method method;

    private final InterceptorDelegate[] interceptors;

    private final ParamValidator[] validators;

    private final AcceptedChecker[] acceptedCheckers;
    
    private final MethodParameterResolver methodParameterResolver;

    private Map<String, Pattern> patterns = Collections.emptyMap();

    private transient String toStringCache;

    public ActionEngine(Module module, Class<?> controllerClass, Object controller, Method method) {
        this.module = module;
        this.controllerClass = controllerClass;
        this.controller = controller;
        this.method = method;
        this.interceptors = compileInterceptors();
        this.methodParameterResolver = compileParamResolvers();
        this.validators = compileValidators();
        this.acceptedCheckers = compileAcceptedCheckers();
    }

    public InterceptorDelegate[] getRegisteredInterceptors() {
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

    private InterceptorDelegate[] compileInterceptors() {
        List<InterceptorDelegate> interceptors = module.getInterceptors();
        List<InterceptorDelegate> registeredInterceptors = new ArrayList<InterceptorDelegate>(
                interceptors.size());
        for (InterceptorDelegate interceptor : interceptors) {
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
                        || ArrayUtils.contains(intercepted.deny(), interceptor.getName())) {
                    continue;
                }
                // 3.2 确认最大的allow允许
                else if (!ArrayUtils.contains(intercepted.allow(), "*")
                        && !ArrayUtils.contains(intercepted.allow(), interceptor.getName())) {
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
                .toArray(new InterceptorDelegate[registeredInterceptors.size()]);
    }

    @Override
    public int compareTo(Engine o) {
        Assert.isTrue(o.getClass() == this.getClass());
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
        return (e1 == e2) ? 0 : (e1 ? -1 : 1);
    }

    /**
     * 用来抽象isAccepted过滤的判断逻辑
     * @author Li Weibo (weibo.leo@gmail.com)
     */
    private static class AcceptedChecker {
    	public int check(HttpServletRequest request) {
    		return 0;
    	}
    }
    
    /**
     * 初始化的时候来决定所有的判断条件，并抽象为AcceptedChecker数组
     * @return 
     */
    private AcceptedChecker[] compileAcceptedCheckers() {
    	
    	IfParamExists ifParamExists = method.getAnnotation(IfParamExists.class);
    	//没标注IfParamExists或者标注了IfParamExists("")都认为不作检查
    	if (ifParamExists == null || ifParamExists.value().trim().length() == 0) {
            return new AcceptedChecker[]{};
        }

    	List<AcceptedChecker> checkers = new ArrayList<AcceptedChecker>();	//所有判断条件的列表
    	String value = ifParamExists.value();
        
    	//可以写多个判断条件，以这样的形式: type&subtype=value&anothername=value2
    	String[] terms = value.split("&");
        Assert.isTrue(terms.length >= 1);	//这个应该永远成立
        
        //按'&'分割后，每一个term就是一个检查条件
        for (final String term : terms) {
        	final int index = term.indexOf('=');	//找'='
        	if (index == -1) {	//没有=说明只有参数名，此时term就是参数名
        		checkers.add(new AcceptedChecker() {
        			final String paramName = term.trim();
        			public int check(HttpServletRequest request) {
        				String paramValue = request.getParameter(paramName);
        				if (StringUtils.isNotBlank(paramValue)) {	//规则中没有约束参数值，所以只要存在就ok
                			return 10;
                		} else {
                			return -1;
                		}
        	    	}
        		});
        	} else {	//term中有'='
        		
        		final String paramName = term.substring(0, index).trim();	//参数名
                final String expected = term.substring(index + 1).trim();	//期望的参数值
                
                //xxx=等价于xxx的
                if (expected.length() == 0) {
                	checkers.add(new AcceptedChecker() {
            			public int check(HttpServletRequest request) {
            				String paramValue = request.getParameter(paramName);
            				if (StringUtils.isNotBlank(paramValue)) {
                    			return 10;
                    		} else {
                    			return -1;
                    		}
            	    	}
            		});
                } else if (expected.startsWith(":")) {	//expected是正则表达式
                	Pattern tmpPattern = null;
                	try {
                		tmpPattern = Pattern.compile(expected.substring(1));
					} catch (PatternSyntaxException e) {
                        logger.error("@IfParamExists pattern error, " + controllerClass.getName()
                                + "#" + method.getName(), e);
                    }
					final Pattern pattern = tmpPattern;	//转成final的
                	checkers.add(new AcceptedChecker() {
                		public int check(HttpServletRequest request) {
            				String paramValue = request.getParameter(paramName);
            				if (paramValue == null) {	//参数值不能存在就不能通过
                                return -1;
                            }
            				if (pattern != null && pattern.matcher(paramValue).matches()) {
                            	return 12;
                            } else {
                            	return -1;
                            }
            	    	}
            		});
                } else {	//expected不为""且不是正则表达式
                	checkers.add(new AcceptedChecker() {
                		public int check(HttpServletRequest request) {
            				String paramValue = request.getParameter(paramName);
            				// 13优先于正则表达式的12
                            return expected.equals(paramValue) ? 13 : -1;
            	    	}
            		});
                }
        	}
        }
        return checkers.toArray(new AcceptedChecker[]{});
    }
    
    @Override
    public int isAccepted(HttpServletRequest request) {
    	if (acceptedCheckers.length == 0) {	//没有约束条件，返回1
    		return 1;
    	}
    	int total = 0;
    	for (AcceptedChecker checker : acceptedCheckers) {
    		int c = checker.check(request);
    		if (c == -1) {	//-1表示此约束条件未通过
    			return -1;
    		}
    		//FIXME 目前采用各个检查条件权值相加的办法来决定最终权值，
    		//在权值相等的情况下，可能会有选举问题，需要更好的策略来取代
    		total += c;
    	}
    	return total;
    }
    
    /**
     * @param request
     * @return
     * @deprecated 原来的isAccepted逻辑，被新逻辑替换掉了
     */
    public int isAccepted0(HttpServletRequest request) {
        Assert.isTrue(request != null);
        IfParamExists ifParamExists = method.getAnnotation(IfParamExists.class);
        if (ifParamExists == null) {
            return 1;
        }
        String value = ifParamExists.value();
        // create&form
        String[] terms = StringUtils.split(value, "&");
        Assert.isTrue(terms.length == 1);
        int index = terms[0].indexOf('=');
        if (index == -1) {
            String paramValue = request.getParameter(terms[0]);
            return StringUtils.isNotBlank(paramValue) ? 10 : -1;
        } else {
            String paramName = terms[0].substring(0, index).trim();
            String expected = terms[0].substring(index + 1).trim();
            String paramValue = request.getParameter(paramName);
            if (StringUtils.isEmpty(expected)) {
                // xxx=等价于xxx的
                return StringUtils.isNotBlank(paramValue) ? 10 : -1;
            } else if (expected.startsWith(":")) {
                if (paramValue == null) {
                    return -1;
                }
                Pattern pattern = patterns.get(expected);
                if (pattern == null) {
                    try {
                        String regex = expected.substring(1);
                        pattern = Pattern.compile(regex);
                        synchronized (this) {// patterns为非同步map
                            if (patterns.size() == 0) {
                                HashMap<String, Pattern> _patterns = new HashMap<String, Pattern>();
                                _patterns.put(expected, pattern);
                                this.patterns = _patterns;
                            } else if (!patterns.containsKey(regex)) {
                                this.patterns.put(expected, pattern);
                            }
                        }
                    } catch (PatternSyntaxException e) {
                        logger.error("@IfParamExists pattern error, " + controllerClass.getName()
                                + "#" + method.getName(), e);
                    }
                }
                return pattern != null && pattern.matcher(paramValue).matches() ? 12 : -1;
            } else {
                // 13优先于正则表达式的12
                return expected.equals(paramValue) ? 13 : -1;
            }
        }
    }

    @Override
    public Object execute(Rose rose, MatchResult mr) throws Throwable {
        try {
            return innerExecute(rose, mr);
        } catch (Throwable local) {
            throw createException(rose, local);
        }
    }

    protected Object innerExecute(Rose rose, MatchResult mr) throws Throwable {
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
            }
        }

        // intetceptors & controller
        return new InvocationChainImpl(rose).doNext();
    }

    private class InvocationChainImpl implements InvocationChain {

        private final boolean debugEnabled = logger.isDebugEnabled();

        private int index = -1;

        private final Rose rose;

        private Object instruction;

        public InvocationChainImpl(Rose rose) {
            this.rose = rose;
        }

        @Override
        public Object doNext() throws Exception {
            if (++index < interceptors.length) { // ++index 用于将-1转化为0
                InterceptorDelegate interceptor = interceptors[index];
                //
                rose.addAfterCompletion(interceptor);
                Object instruction = interceptor.roundInvocation(rose.getInvocation(), this);
                //
                if (debugEnabled) {
                    logger.debug("interceptor[" + interceptor.getName() + "] do round and return '"
                            + instruction + "'");
                }

                // 拦截器返回null的，要恢复为原instruction
                // 这个功能非常有用!!
                if (instruction != null) {
                    this.instruction = instruction;
                }
                return this.instruction;
            } else if (index == interceptors.length) {
                this.instruction = method.invoke(controller, rose.getInvocation()
                        .getMethodParameters());

                // @Return
                if (this.instruction == null) {
                    Return returnAnnotation = method.getAnnotation(Return.class);
                    if (returnAnnotation != null) {
                        this.instruction = returnAnnotation.value();
                    }
                }
                return this.instruction;
            }
            throw new IndexOutOfBoundsException(
                    "don't call twice 'chain.doNext()' in one intercpetor; index=" + index
                            + "; interceptors.length=" + interceptors.length);
        }

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
                    contentType = "application/json";
                } else if (contentType.equals("xml")) {
                    contentType = "text/xml";
                } else if (contentType.equals("html")) {
                    contentType = "text/html";
                } else if (contentType.equals("plain") || contentType.equals("text")) {
                    contentType = "text/plain";
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
            Assert.isTrue(encoding != null);
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
