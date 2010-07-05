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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.Invocation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class TextInstruction extends AbstractInstruction {

    protected static Log logger = LogFactory.getLog(TextInstruction.class);

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
        String oldEncoding = response.getCharacterEncoding();
        if (StringUtils.isBlank(oldEncoding) || oldEncoding.startsWith("ISO-")) {
            String encoding = inv.getRequest().getCharacterEncoding();
            Assert.isTrue(encoding != null);
            response.setCharacterEncoding(encoding);
            if (logger.isDebugEnabled()) {
                logger.debug("set response.characterEncoding by default:"
                        + response.getCharacterEncoding());
            }
        }

        //
        String mainContentType = null; // text中说明的mainContentType，分号之前的部分(如有分号的话）
        final int contentTypeIndex = text.indexOf(':');
        int mainContentTypeIndex = -1;
        if (contentTypeIndex > 0) {
            mainContentTypeIndex = text.indexOf(";");
            if (mainContentTypeIndex < 0 || mainContentTypeIndex > contentTypeIndex) {
                mainContentTypeIndex = contentTypeIndex;
            }
            mainContentType = text.substring(0, mainContentTypeIndex);
            if (mainContentType.length() == 0) {
                mainContentType = "text/html";
            } else if (mainContentType.equals("json")) {
                mainContentType = "application/json";
            } else if (mainContentType.equals("html")) {
                mainContentType = "text/html";
            } else if (mainContentType.equals("xml")) {
                mainContentType = "text/xml";
            } else if (mainContentType.equals("text") || mainContentType.equals("plain")) {
                mainContentType = "text/plain";
            } else if (!mainContentType.startsWith("text/")
                    && !mainContentType.startsWith("application/")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("'" + mainContentType + "' is not a content-type, skip it! ");
                }
                mainContentType = null;
            }
        }
        if (mainContentType != null) {
            Assert.isTrue(contentTypeIndex > 0);
            final String contentType;
            if (contentTypeIndex != mainContentTypeIndex) {
                contentType = mainContentType
                        + text.substring(mainContentTypeIndex, contentTypeIndex);
            } else {
                contentType = mainContentType;
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
            String encoding = response.getCharacterEncoding();
            if (encoding == null) {
                encoding = "UTF-8";
                response.setCharacterEncoding("UTF-8");
            }
            byte[] content = text.getBytes(encoding);
            response.setContentLength(content.length);
            ServletOutputStream out = response.getOutputStream();
            out.write(content);
            out.flush();
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
