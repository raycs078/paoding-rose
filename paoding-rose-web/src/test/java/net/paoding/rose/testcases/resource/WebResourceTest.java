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
package net.paoding.rose.testcases.resource;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.mapping.MatchResult;
import net.paoding.rose.web.impl.mapping.WebResource;
import net.paoding.rose.web.impl.mapping.WebResourceImpl;
import net.paoding.rose.web.impl.thread.Engine;
import net.paoding.rose.web.impl.thread.Rose;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class WebResourceTest extends TestCase {

    private Engine getEngine = new Engine() {

        public int compareTo(Engine o) {
            return 0;
        }

        @Override
        public boolean isAccepted(HttpServletRequest rose) {
            return true;
        }

        @Override
        public Object execute(Rose rose, MatchResult mr) throws Throwable {
            return null;
        }

        @Override
        public void destroy() {

        }

        @Override
        public String toString() {
            return "get";
        }
    };

    private Engine postEngine = new Engine() {

        public int compareTo(Engine o) {
            return 0;
        }

        @Override
        public boolean isAccepted(HttpServletRequest rose) {
            return true;
        }

        @Override
        public Object execute(Rose rose, MatchResult mr) throws Throwable {
            return null;
        }

        @Override
        public void destroy() {

        }

        @Override
        public String toString() {
            return "post";
        }
    };

    private Engine defEngine = new Engine() {

        public int compareTo(Engine o) {
            return 1;
        }

        @Override
        public boolean isAccepted(HttpServletRequest rose) {
            return true;
        }

        @Override
        public Object execute(Rose rose, MatchResult mr) throws Throwable {
            return null;
        }

        @Override
        public void destroy() {

        }

        @Override
        public String toString() {
            return "def";
        }
    };

    public void testGetPost() {
        WebResource resource = new WebResourceImpl("testGetPost");
        resource.addEngine(ReqMethod.GET, getEngine);
        resource.addEngine(ReqMethod.POST, postEngine);

        assertSame(getEngine, resource.getEngines(ReqMethod.GET)[0]);
        assertSame(postEngine, resource.getEngines(ReqMethod.POST)[0]);

        String msg = "not allowed method should return none engine";
        assertNull(msg, resource.getEngines(ReqMethod.PUT));
        assertNull(msg, resource.getEngines(ReqMethod.DELETE));
        assertNull(msg, resource.getEngines(ReqMethod.OPTIONS));

        assertEquals("testGetPost [GET, POST]", resource.toString());
    }

    public void testNotOverrideByAll() {
        WebResource resource = new WebResourceImpl("testNotOverrideByAll");
        resource.addEngine(ReqMethod.GET, getEngine);
        resource.addEngine(ReqMethod.ALL, defEngine);
        resource.addEngine(ReqMethod.POST, postEngine);

        assertSame(getEngine, resource.getEngines(ReqMethod.GET)[0]);
        assertSame(defEngine, resource.getEngines(ReqMethod.GET)[1]);
        assertSame(postEngine, resource.getEngines(ReqMethod.POST)[0]);
        assertSame(defEngine, resource.getEngines(ReqMethod.POST)[1]);

        assertSame(defEngine, resource.getEngines(ReqMethod.PUT)[0]);
        assertSame(defEngine, resource.getEngines(ReqMethod.DELETE)[0]);

        assertEquals("testNotOverrideByAll [GET, POST, DELETE, PUT, HEAD, OPTIONS, TRACE]",
                resource.toString());
    }
}
