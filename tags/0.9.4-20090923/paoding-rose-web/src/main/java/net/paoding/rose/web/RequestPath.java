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
package net.paoding.rose.web;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class RequestPath {

    private String method;

    private String uri; // = contextPath + ctxpath

    private String ctxpath; // by servlet container

    private String pathInfo; // = modulePath + controllerPath + actionPath

    private String modulePath; //

    private String controllerPathInfo; //

    private String controllerPath;

    private String actionPath;

    private Dispatcher dispatcher;

    public boolean isIncludeRequest() {
        return dispatcher == Dispatcher.INCLUDE;
    }

    public boolean isForwardRequest() {
        return dispatcher == Dispatcher.FORWARD;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCtxpath() {
        return ctxpath;
    }

    public void setCtxpath(String ctxpath) {
        this.ctxpath = ctxpath;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public String getModulePath() {
        return modulePath;
    }

    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public String getControllerPathInfo() {
        if (controllerPathInfo == null) {
            controllerPathInfo = pathInfo.substring(modulePath.length());
        }
        return controllerPathInfo;
    }

    public String getControllerPath() {
        return controllerPath;
    }

    public void setControllerPath(String controllerPath) {
        this.controllerPath = controllerPath;
    }

    public String getActionPath() {
        return actionPath;
    }

    public void setActionPath(String actionPath) {
        this.actionPath = actionPath;
    }

    @Override
    public String toString() {
        return "ctxpath=" + ctxpath + "; pathInfo=" + pathInfo + "; modulePath=" + modulePath
                + "; controllerPath=" + controllerPath + "; actionPath=" + actionPath;
    }

}
