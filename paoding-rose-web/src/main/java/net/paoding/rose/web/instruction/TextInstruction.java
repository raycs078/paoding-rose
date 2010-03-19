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

import java.io.IOException;
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

    //-------------------------------------------

    private String text;

    public String text() {
        return text;
    }

    public TextInstruction text(String text) {
        this.text = text;
        return this;
    }

    //-------------------------------------------

    @Override
    public void doRender(Invocation inv) throws Exception {
        if (StringUtils.isEmpty(text)) {
            return;
        }
        HttpServletResponse response = inv.getResponse();
        int contentTypeIndex = text.indexOf(':');
        if (contentTypeIndex > 0) {
            int mainContentTypeIndex = text.indexOf(";");
            if (mainContentTypeIndex < 0 || mainContentTypeIndex > contentTypeIndex) {
                mainContentTypeIndex = contentTypeIndex;
            }
            String mainContentType = text.substring(0, mainContentTypeIndex);
            if (mainContentType.length() == 0) {
                mainContentType = "text/html";
            } else if (mainContentType.equals("json")) {
                mainContentType = "application/x-json";
            } else if (mainContentType.equals("html")) {
                mainContentType = "text/html";
            } else if (mainContentType.equals("xml")) {
                mainContentType = "text/xml";
            } else if (mainContentType.equals("text") || mainContentType.equals("plain")) {
                mainContentType = "text/plain";
            } else if (!mainContentType.startsWith("text/")
                    && !mainContentType.startsWith("application/")) {
                throw new IllegalArgumentException("wrong Content-Type in instruction: " + text);
            }
            String contentType = mainContentType;
            if (contentTypeIndex != mainContentTypeIndex) {
                contentType = mainContentType
                        + text.substring(mainContentTypeIndex, contentTypeIndex);
            }
            response.setContentType(contentType);
            if (logger.isDebugEnabled()) {
                logger.debug("set response content-type: " + response.getContentType());
            }
            sendResponse(response, text.substring(contentTypeIndex + 1));
        } else {
            if (response.getContentType() == null) {
                response.setContentType("text/html");
                if (logger.isDebugEnabled()) {
                    logger.debug("set response content-type by default: "
                            + response.getContentType());
                }
            }
            sendResponse(response, text);
        }
    }

    private void sendResponse(HttpServletResponse response, String text) throws IOException {
        if (StringUtils.isNotEmpty(text)) {
            PrintWriter out = response.getWriter();
            out.write(text);
            out.flush();
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
