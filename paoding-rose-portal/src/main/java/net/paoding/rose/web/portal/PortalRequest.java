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
package net.paoding.rose.web.portal;

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

/**
 * 继承 {@link HttpServletRequestWrapper}设置内部非
 * {@link HttpServletRequestWrapper}请求对象，使web容器能够调用到这个类的
 * {@link #setRequest(ServletRequest)}方法，支持多线程同时"forward"
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@SuppressWarnings("unchecked")
public class PortalRequest extends HttpServletRequestWrapper implements HttpServletRequest {

    private Portal portal;

    private final PortalNestRequest portalNestRequest;

    private ThreadLocal<HttpServletRequest> requests = new ThreadLocal<HttpServletRequest>();

    public Portal getPortal() {
        return portal;
    }

    public PortalRequest(Portal portal) {
        super(new PortalNestRequest(portal));
        this.portal = portal;
        setRequest(portalNestRequest = (PortalNestRequest) super.getRequest());
    }

    /**
     * Sets the request object being wrapped.
     * 
     * @throws java.lang.IllegalArgumentException if the request is null.
     */

    public void setRequest(ServletRequest request) {
        super.setRequest(request);
        this.requests.set((HttpServletRequest) request);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return _getHttpServletRequest().getRequestDispatcher(path);
    }

    // ------------------------------------------------- ServletRequest Methods

    /**
     * Override the <code>getAttribute()</code> method of the wrapped
     * request.
     * 
     * @param name Name of the attribute to retrieve
     */
    public Object getAttribute(String name) {
        return _getHttpServletRequest().getAttribute(name);
    }

    /**
     * Override the <code>getAttributeNames()</code> method of the wrapped
     * request.
     */
    public Enumeration getAttributeNames() {
        return _getHttpServletRequest().getAttributeNames();
    }

    /**
     * Override the <code>removeAttribute()</code> method of the wrapped
     * request.
     * 
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {
        _getHttpServletRequest().removeAttribute(name);
    }

    /**
     * Override the <code>setAttribute()</code> method of the wrapped
     * request.
     * 
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set
     */
    public void setAttribute(String name, Object value) {
        _getHttpServletRequest().setAttribute(name, value);
    }

    //---------------
    /**
     * The default behavior of this method is to return getContextPath() on
     * the wrapped request object.
     */
    public String getContextPath() {
        return _getHttpServletRequest().getContextPath();
    }

    /**
     * The default behavior of this method is to return getQueryString() on
     * the wrapped request object.
     */
    public String getQueryString() {
        return _getHttpServletRequest().getQueryString();
    }

    /**
     * The default behavior of this method is to return getRequestURI() on
     * the wrapped request object.
     */
    public String getRequestURI() {
        return _getHttpServletRequest().getRequestURI();
    }

    /**
     * The default behavior of this method is to return getServletPath() on
     * the wrapped request object.
     */
    public String getServletPath() {
        return _getHttpServletRequest().getServletPath();
    }

    public StringBuffer getRequestURL() {
        return _getHttpServletRequest().getRequestURL();
    }

    //---------------

    private HttpServletRequest _getHttpServletRequest() {
        HttpServletRequest request = (HttpServletRequest) requests.get();
        if (request == null) {
            request = portalNestRequest;
        }
        return request;
    }

    /**
     * The default behavior of this method is to return getAuthType() on
     * the wrapped request object.
     */

    public String getAuthType() {
        return this._getHttpServletRequest().getAuthType();
    }

    /**
     * The default behavior of this method is to return getCookies() on the
     * wrapped request object.
     */
    public Cookie[] getCookies() {
        return this._getHttpServletRequest().getCookies();
    }

    /**
     * The default behavior of this method is to return
     * getDateHeader(String name) on the wrapped request object.
     */
    public long getDateHeader(String name) {
        return this._getHttpServletRequest().getDateHeader(name);
    }

    /**
     * The default behavior of this method is to return getHeader(String
     * name) on the wrapped request object.
     */
    public String getHeader(String name) {
        return this._getHttpServletRequest().getHeader(name);
    }

    /**
     * The default behavior of this method is to return getHeaders(String
     * name) on the wrapped request object.
     */
    public Enumeration getHeaders(String name) {
        return this._getHttpServletRequest().getHeaders(name);
    }

    /**
     * The default behavior of this method is to return getHeaderNames() on
     * the wrapped request object.
     */

    public Enumeration getHeaderNames() {
        return this._getHttpServletRequest().getHeaderNames();
    }

    /**
     * The default behavior of this method is to return getIntHeader(String
     * name) on the wrapped request object.
     */

    public int getIntHeader(String name) {
        return this._getHttpServletRequest().getIntHeader(name);
    }

    /**
     * The default behavior of this method is to return getMethod() on the
     * wrapped request object.
     */
    public String getMethod() {
        return this._getHttpServletRequest().getMethod();
    }

    /**
     * The default behavior of this method is to return getPathInfo() on
     * the wrapped request object.
     */
    public String getPathInfo() {
        return this._getHttpServletRequest().getPathInfo();
    }

    /**
     * The default behavior of this method is to return getPathTranslated()
     * on the wrapped request object.
     */

    public String getPathTranslated() {
        return this._getHttpServletRequest().getPathTranslated();
    }

    /**
     * The default behavior of this method is to return getRemoteUser() on
     * the wrapped request object.
     */
    public String getRemoteUser() {
        return this._getHttpServletRequest().getRemoteUser();
    }

    /**
     * The default behavior of this method is to return isUserInRole(String
     * role) on the wrapped request object.
     */
    public boolean isUserInRole(String role) {
        return this._getHttpServletRequest().isUserInRole(role);
    }

    /**
     * The default behavior of this method is to return getUserPrincipal()
     * on the wrapped request object.
     */
    public java.security.Principal getUserPrincipal() {
        return this._getHttpServletRequest().getUserPrincipal();
    }

    /**
     * The default behavior of this method is to return
     * getRequestedSessionId() on the wrapped request object.
     */
    public String getRequestedSessionId() {
        return this._getHttpServletRequest().getRequestedSessionId();
    }

    /**
     * The default behavior of this method is to return getSession(boolean
     * create) on the wrapped request object.
     */
    public HttpSession getSession(boolean create) {
        return this._getHttpServletRequest().getSession(create);
    }

    /**
     * The default behavior of this method is to return getSession() on the
     * wrapped request object.
     */
    public HttpSession getSession() {
        return this._getHttpServletRequest().getSession();
    }

    /**
     * The default behavior of this method is to return
     * isRequestedSessionIdValid() on the wrapped request object.
     */

    public boolean isRequestedSessionIdValid() {
        return this._getHttpServletRequest().isRequestedSessionIdValid();
    }

    /**
     * The default behavior of this method is to return
     * isRequestedSessionIdFromCookie() on the wrapped request object.
     */
    public boolean isRequestedSessionIdFromCookie() {
        return this._getHttpServletRequest().isRequestedSessionIdFromCookie();
    }

    /**
     * The default behavior of this method is to return
     * isRequestedSessionIdFromURL() on the wrapped request object.
     */
    public boolean isRequestedSessionIdFromURL() {
        return this._getHttpServletRequest().isRequestedSessionIdFromURL();
    }

    /**
     * The default behavior of this method is to return
     * isRequestedSessionIdFromUrl() on the wrapped request object.
     */
    @SuppressWarnings("deprecation")
    public boolean isRequestedSessionIdFromUrl() {
        return this._getHttpServletRequest().isRequestedSessionIdFromUrl();
    }

    /**
     * Creates a ServletRequest adaptor wrapping the given request object.
     * 
     * @throws java.lang.IllegalArgumentException if the request is null
     */

    /**
     * Return the wrapped request object.
     */
    public ServletRequest getRequest() {
        return _getHttpServletRequest();
    }

    /**
     * The default behavior of this method is to return
     * getCharacterEncoding() on the wrapped request object.
     */

    public String getCharacterEncoding() {
        return _getHttpServletRequest().getCharacterEncoding();
    }

    /**
     * The default behavior of this method is to set the character encoding
     * on the wrapped request object.
     */

    public void setCharacterEncoding(String enc) throws java.io.UnsupportedEncodingException {
        _getHttpServletRequest().setCharacterEncoding(enc);
    }

    /**
     * The default behavior of this method is to return getContentLength()
     * on the wrapped request object.
     */

    public int getContentLength() {
        return _getHttpServletRequest().getContentLength();
    }

    /**
     * The default behavior of this method is to return getContentType() on
     * the wrapped request object.
     */
    public String getContentType() {
        return _getHttpServletRequest().getContentType();
    }

    /**
     * The default behavior of this method is to return getInputStream() on
     * the wrapped request object.
     */

    public ServletInputStream getInputStream() throws IOException {
        return _getHttpServletRequest().getInputStream();
    }

    /**
     * The default behavior of this method is to return getParameter(String
     * name) on the wrapped request object.
     */

    public String getParameter(String name) {
        return _getHttpServletRequest().getParameter(name);
    }

    /**
     * The default behavior of this method is to return getParameterMap()
     * on the wrapped request object.
     */
    public Map getParameterMap() {
        return _getHttpServletRequest().getParameterMap();
    }

    /**
     * The default behavior of this method is to return getParameterNames()
     * on the wrapped request object.
     */

    public Enumeration getParameterNames() {
        return _getHttpServletRequest().getParameterNames();
    }

    /**
     * The default behavior of this method is to return
     * getParameterValues(String name) on the wrapped request object.
     */
    public String[] getParameterValues(String name) {
        return _getHttpServletRequest().getParameterValues(name);
    }

    /**
     * The default behavior of this method is to return getProtocol() on
     * the wrapped request object.
     */

    public String getProtocol() {
        return _getHttpServletRequest().getProtocol();
    }

    /**
     * The default behavior of this method is to return getScheme() on the
     * wrapped request object.
     */

    public String getScheme() {
        return _getHttpServletRequest().getScheme();
    }

    /**
     * The default behavior of this method is to return getServerName() on
     * the wrapped request object.
     */
    public String getServerName() {
        return _getHttpServletRequest().getServerName();
    }

    /**
     * The default behavior of this method is to return getServerPort() on
     * the wrapped request object.
     */

    public int getServerPort() {
        return _getHttpServletRequest().getServerPort();
    }

    /**
     * The default behavior of this method is to return getReader() on the
     * wrapped request object.
     */

    public BufferedReader getReader() throws IOException {
        return _getHttpServletRequest().getReader();
    }

    /**
     * The default behavior of this method is to return getRemoteAddr() on
     * the wrapped request object.
     */

    public String getRemoteAddr() {
        return _getHttpServletRequest().getRemoteAddr();
    }

    /**
     * The default behavior of this method is to return getRemoteHost() on
     * the wrapped request object.
     */

    public String getRemoteHost() {
        return _getHttpServletRequest().getRemoteHost();
    }

    //
    //    /**
    //      * The default behavior of this method is to return setAttribute(String name, Object o)
    //     * on the wrapped request object.
    //     */
    //
    //    public void setAttribute(String name, Object o) {
    //    _getHttpServletRequest().setAttribute(name, o);
    //    }
    //    
    //    
    //    
    //
    //    /**
    //      * The default behavior of this method is to call removeAttribute(String name)
    //     * on the wrapped request object.
    //     */
    //    public void removeAttribute(String name) {
    //    _getHttpServletRequest().removeAttribute(name);
    //    }

    /**
     * The default behavior of this method is to return getLocale() on the
     * wrapped request object.
     */

    public Locale getLocale() {
        return _getHttpServletRequest().getLocale();
    }

    /**
     * The default behavior of this method is to return getLocales() on the
     * wrapped request object.
     */

    public Enumeration getLocales() {
        return _getHttpServletRequest().getLocales();
    }

    /**
     * The default behavior of this method is to return isSecure() on the
     * wrapped request object.
     */

    public boolean isSecure() {
        return _getHttpServletRequest().isSecure();
    }

    /**
     * The default behavior of this method is to return getRealPath(String
     * path) on the wrapped request object.
     */

    @SuppressWarnings("deprecation")
    public String getRealPath(String path) {
        return _getHttpServletRequest().getRealPath(path);
    }

    /**
     * The default behavior of this method is to return getRemotePort() on
     * the wrapped request object.
     * 
     * @since 2.4
     */
    public int getRemotePort() {
        return _getHttpServletRequest().getRemotePort();
    }

    /**
     * The default behavior of this method is to return getLocalName() on
     * the wrapped request object.
     * 
     * @since 2.4
     */
    public String getLocalName() {
        return _getHttpServletRequest().getLocalName();
    }

    /**
     * The default behavior of this method is to return getLocalAddr() on
     * the wrapped request object.
     * 
     * @since 2.4
     */
    public String getLocalAddr() {
        return _getHttpServletRequest().getLocalAddr();
    }

    /**
     * The default behavior of this method is to return getLocalPort() on
     * the wrapped request object.
     * 
     * @since 2.4
     */
    public int getLocalPort() {
        return _getHttpServletRequest().getLocalPort();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "['" + portal.getRequestPath().getUri() + "']";
    }

}
