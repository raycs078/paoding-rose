/*
 * Copyright 2007-2012 the original author or authors.
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
package net.paoding.rose.web.portal.impl;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.paoding.rose.web.portal.Window;
import net.paoding.rose.web.portal.WindowRender;

/**
 * 
 * @author qieqie
 * 
 */
public final class NestedWindowRender implements WindowRender {

    private static WindowRender simpleRender = new SimpleWindowRender();

    private WindowRender innerRender;

    public NestedWindowRender(WindowRender innerRender) {
        setInnerRender(innerRender);
    }

    public NestedWindowRender() {
    }

    public void setInnerRender(WindowRender actualRender) {
        this.innerRender = actualRender;
    }

    public WindowRender getInnerRender() {
        return innerRender;
    }

    @Override
    public String render(Window w) {
        WindowRender render = this.innerRender;
        if (render == null) {
            render = simpleRender;
        }
        WindowImpl window = (WindowImpl) w;
        if (window.getContextLength() >= 0) {
            return render.render(window);
        }
        if (window.getThrowable() != null) {
            writeExceptionAsContent(window);
        } else if (window.getStatusCode() > 299 || window.getStatusCode() < 200) {
            window.appendContent(window.getPath());
            window.appendContent("<br>");
            window.appendContent(String.valueOf(window.getStatusCode()));
            if (window.getStatusMessage() != null) {
                window.appendContent(" ");
                window.appendContent(window.getStatusMessage());
            }
        }
        return render.render(window);
    }

    private void writeExceptionAsContent(WindowImpl window) {
        window.appendContent(window.getPath());
        window.appendContent("<br>");
        window.appendContent(String.valueOf(window.getStatusCode()));
        if (window.getStatusMessage() != null) {
            window.appendContent(" ");
            window.appendContent(window.getStatusMessage());
        }
        window.appendContent("<br>");
        window.appendContent("<pre>");
        Throwable ex = window.getThrowable();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        window.appendContent(stringWriter.getBuffer());
        window.appendContent("</pre>");

    }

}
