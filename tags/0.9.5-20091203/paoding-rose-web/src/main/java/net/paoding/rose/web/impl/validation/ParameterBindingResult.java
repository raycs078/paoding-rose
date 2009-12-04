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
package net.paoding.rose.web.impl.validation;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.impl.thread.InvocationBean;

import org.springframework.util.StringUtils;
import org.springframework.validation.AbstractBindingResult;
import org.springframework.validation.FieldError;

/**
 * 控制器action方法普通参数绑定信息类，
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ParameterBindingResult extends AbstractBindingResult {

    public static final String OBJECT_NAME = "parameterBindingResult";

    private static final long serialVersionUID = -592629554361073051L;

    private transient Invocation inv;

    /**
     * 
     * @param inv
     * @throws NullPointerException 如果给定的 {@link InvocationBean}参数为null
     */
    public ParameterBindingResult(Invocation inv) {
        super(OBJECT_NAME);
        if (inv == null) {
            throw new NullPointerException();
        }
        this.inv = inv;

    }

    @Override
    public Object getTarget() {
        return this.inv;
    }

    /**
     * 
     * @throws IllegalStateException 在反序列化后调用
     */
    public void rejectValue(String field, String errorCode, Object[] errorArgs,
            String defaultMessage) {
        if ("".equals(getNestedPath()) && !StringUtils.hasLength(field)) {
            // We're at the top of the nested object hierarchy,
            // so the present level is not a field but rather the top object.
            // The best we can do is register a global error here...
            reject(errorCode, errorArgs, defaultMessage);
            return;
        }
        String fixedField = fixedField(field);
        Object newVal = getActualFieldValue(fixedField);
        FieldError fe = new FieldError(getObjectName(), fixedField, newVal, true,
                resolveMessageCodes(errorCode, field), errorArgs, defaultMessage);
        addError(fe);
    }

    /**
     * 
     * @throws IllegalStateException 在反序列化后调用
     */
    @Override
    protected Object getActualFieldValue(String field) {
        if (inv == null) {
            throw new IllegalStateException();
        }
        return inv.getRawParameter(field);
    }

}
