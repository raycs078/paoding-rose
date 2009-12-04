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
package net.paoding.rose.web.portal.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 封装Portal的请求对象，使能够支持并发执行窗口请求
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
final class PortalRequest extends HttpServletRequestWrapper implements HttpServletRequest {

    private static final Log logger = LogFactory.getLog(PortalRequest.class);

    /**
     * 封装了原始的 portal 请求对象， 组织了容器的 HttpServletRequestWrapper 链<br>
     * 即 RequestWrapper 通过不实现 HttpServletRequestWrapper 使得
     * PortalRequest的setRequest方法能够被容器调用到
     */
    private final PrivateRequestWrapper privateRequestWrapper;

    /**
     * 保存本次 portal 请求的窗口请求对象 (由容器调用setRequest设置进来)
     */
    private final ThreadLocal<HttpServletRequest> threadLocalRequests = new ThreadLocal<HttpServletRequest>();

    /**
     * 构造子
     * 
     * @param orginRequest 封装之前访问该 portal 的请求对象
     */
    public PortalRequest(HttpServletRequest orginRequest) {
        super(new PrivateRequestWrapper(orginRequest));
        privateRequestWrapper = (PrivateRequestWrapper) super.getRequest();
    }

    /**
     * 设置一个请求对象到这个包装器中， {@link PortalRequest} 将把这个请求对象关联给当前线程。
     * <p>
     * 
     * 这个方法将由web容器(tomcat/resin等)调用
     */
    public void setRequest(ServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("set request: %s", request));
        }
        this.threadLocalRequests.set((HttpServletRequest) request);
    }

    /**
     * 返回当前邦定到当前线程的请求对象，如果没有则返回 portalNotWrapperRequest 请求对象
     */
    public HttpServletRequest getRequest() {
        HttpServletRequest request = (HttpServletRequest) threadLocalRequests.get();
        if (request == null) {
            request = privateRequestWrapper;
        }
        return request;
    }

    // ----- HttpServletRequestWrapper的每个方法都得重新覆盖，不解时请看ServletRequestWrapper的代码即知 ----

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return getRequest().getRequestDispatcher(path);
    }

    @Override
    public Object getAttribute(String name) {
        return getRequest().getAttribute(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames() {
        return getRequest().getAttributeNames();
    }

    @Override
    public void removeAttribute(String name) {
        getRequest().removeAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        getRequest().setAttribute(name, value);
    }

    @Override
    public String getContextPath() {
        return getRequest().getContextPath();
    }

    @Override
    public String getQueryString() {
        return getRequest().getQueryString();
    }

    @Override
    public String getRequestURI() {
        return getRequest().getRequestURI();
    }

    @Override
    public String getServletPath() {
        return getRequest().getServletPath();
    }

    @Override
    public StringBuffer getRequestURL() {
        return getRequest().getRequestURL();
    }

    @Override
    public String getAuthType() {
        return getRequest().getAuthType();
    }

    @Override
    public Cookie[] getCookies() {
        return getRequest().getCookies();
    }

    @Override
    public long getDateHeader(String name) {
        return getRequest().getDateHeader(name);
    }

    @Override
    public String getHeader(String name) {
        return getRequest().getHeader(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getHeaders(String name) {
        return getRequest().getHeaders(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getHeaderNames() {
        return getRequest().getHeaderNames();
    }

    @Override
    public int getIntHeader(String name) {
        return getRequest().getIntHeader(name);
    }

    @Override
    public String getMethod() {
        return getRequest().getMethod();
    }

    @Override
    public String getPathInfo() {
        return getRequest().getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return getRequest().getPathTranslated();
    }

    @Override
    public String getRemoteUser() {
        return getRequest().getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String role) {
        return getRequest().isUserInRole(role);
    }

    @Override
    public java.security.Principal getUserPrincipal() {
        return getRequest().getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId() {
        return getRequest().getRequestedSessionId();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return getRequest().getSession(create);
    }

    @Override
    public HttpSession getSession() {
        return getRequest().getSession();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return getRequest().isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return getRequest().isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return getRequest().isRequestedSessionIdFromURL();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return getRequest().isRequestedSessionIdFromUrl();
    }

    @Override
    public String getCharacterEncoding() {
        return getRequest().getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String enc) throws java.io.UnsupportedEncodingException {
        getRequest().setCharacterEncoding(enc);
    }

    @Override
    public int getContentLength() {
        return getRequest().getContentLength();
    }

    @Override
    public String getContentType() {
        return getRequest().getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return getRequest().getInputStream();
    }

    @Override
    public String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map getParameterMap() {
        return getRequest().getParameterMap();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getParameterNames() {
        return getRequest().getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return getRequest().getParameterValues(name);
    }

    @Override
    public String getProtocol() {
        return getRequest().getProtocol();
    }

    @Override
    public String getScheme() {
        return getRequest().getScheme();
    }

    @Override
    public String getServerName() {
        return getRequest().getServerName();
    }

    @Override
    public int getServerPort() {
        return getRequest().getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return getRequest().getReader();
    }

    @Override
    public String getRemoteAddr() {
        return getRequest().getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return getRequest().getRemoteHost();
    }

    @Override
    public Locale getLocale() {
        return getRequest().getLocale();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getLocales() {
        return getRequest().getLocales();
    }

    @Override
    public boolean isSecure() {
        return getRequest().isSecure();
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getRealPath(String path) {
        return getRequest().getRealPath(path);
    }

    @Override
    public int getRemotePort() {
        return getRequest().getRemotePort();
    }

    @Override
    public String getLocalName() {
        return getRequest().getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return getRequest().getLocalAddr();
    }

    @Override
    public int getLocalPort() {
        return getRequest().getLocalPort();
    }

    // ---------------toString ----------------

    @Override
    public String toString() {
        return "PortalRequest for " + privateRequestWrapper.getRequestURI();
    }

}
