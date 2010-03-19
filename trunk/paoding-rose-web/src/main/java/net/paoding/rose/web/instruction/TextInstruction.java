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
package net.paoding.rose.web.instruction;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.Invocation;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class TextInstruction extends AbstractInstruction {

    @Override
    public void doRender(Invocation inv) throws Exception {
        String encoding = resolvePlaceHolder(this.encoding, inv);
        String contentType = resolvePlaceHolder(this.contentType, inv);
        String text = resolvePlaceHolder(this.text, inv);

        HttpServletResponse response = inv.getResponse();
        if (encoding != null) {
            response.setCharacterEncoding(encoding);
            if (logger.isDebugEnabled()) {
                logger.debug("set response.setCharacterEncoding:"//
                        + response.getCharacterEncoding());
            }
        }
        if (contentType != null) {
            response.setContentType(contentType);
            if (logger.isDebugEnabled()) {
                logger.debug("set response.setContentType:" + response.getContentType());
            }
        }
        if (StringUtils.isNotEmpty(text)) {
            PrintWriter out = response.getWriter();
            out.write(text);
            out.flush();
        }
    }

    //-------------------------------------------

    private String text;

    private String contentType;

    private String encoding;

    public TextInstruction contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String contentType() {
        return contentType;
    }

    public TextInstruction encoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public String encoding() {
        return encoding;
    }

    public String text() {
        return text;
    }

    public TextInstruction plain(String text) {
        this.text = text;
        return this;
    }

    public TextInstruction html(String html) {
        this.text = html;
        contentType("text/html");
        return this;
    }

    public TextInstruction xml(String xml) {
        this.text = xml;
        contentType("text/xml");
        return this;
    }

    public TextInstruction json(String json) {
        this.text = json;
        contentType("application/x-json");
        return this;
    }

    @Override
    public String toString() {
        return text;
    }
}
