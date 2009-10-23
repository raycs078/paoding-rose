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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * 不扩展 {@link HttpServletRequestWrapper}，使
 * {@link PortalRequest#setRequest(javax.servlet.ServletRequest)}能被web容器调用到
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@SuppressWarnings("unchecked")
public class PortalNestRequest implements HttpServletRequest {

    private HttpServletRequest request;

    public PortalNestRequest(Portal portal) {
        this.request = portal.getRequest();
    }

    private HttpServletRequest _getHttpServletRequest() {
        return request;
    }

    /**
     * 
     * The default behavior of this method is to call getAttribute(String
     * name) on the wrapped request object.
     */

    public Object getAttribute(String name) {
        return this.request.getAttribute(name);
    }

    /**
     * The default behavior of this method is to return getAttributeNames()
     * on the wrapped request object.
     */

    public Enumeration getAttributeNames() {
        return this.request.getAttributeNames();
    }

    /**
     * The default behavior of this method is to return
     * getCharacterEncoding() on the wrapped request object.
     */

    public String getCharacterEncoding() {
        return this.request.getCharacterEncoding();
    }

    /**
     * The default behavior of this method is to set the character encoding
     * on the wrapped request object.
     */

    public void setCharacterEncoding(String enc) throws java.io.UnsupportedEncodingException {
        this.request.setCharacterEncoding(enc);
    }

    /**
     * The default behavior of this method is to return getContentLength()
     * on the wrapped request object.
     */

    public int getContentLength() {
        return this.request.getContentLength();
    }

    /**
     * The default behavior of this method is to return getContentType() on
     * the wrapped request object.
     */
    public String getContentType() {
        return this.request.getContentType();
    }

    /**
     * The default behavior of this method is to return getInputStream() on
     * the wrapped request object.
     */

    public ServletInputStream getInputStream() throws IOException {
        return this.request.getInputStream();
    }

    /**
     * The default behavior of this method is to return getParameter(String
     * name) on the wrapped request object.
     */

    public String getParameter(String name) {
        return this.request.getParameter(name);
    }

    /**
     * The default behavior of this method is to return getParameterMap()
     * on the wrapped request object.
     */
    public Map getParameterMap() {
        return this.request.getParameterMap();
    }

    /**
     * The default behavior of this method is to return getParameterNames()
     * on the wrapped request object.
     */

    public Enumeration getParameterNames() {
        return this.request.getParameterNames();
    }

    /**
     * The default behavior of this method is to return
     * getParameterValues(String name) on the wrapped request object.
     */
    public String[] getParameterValues(String name) {
        return this.request.getParameterValues(name);
    }

    /**
     * The default behavior of this method is to return getProtocol() on
     * the wrapped request object.
     */

    public String getProtocol() {
        return this.request.getProtocol();
    }

    /**
     * The default behavior of this method is to return getScheme() on the
     * wrapped request object.
     */

    public String getScheme() {
        return this.request.getScheme();
    }

    /**
     * The default behavior of this method is to return getServerName() on
     * the wrapped request object.
     */
    public String getServerName() {
        return this.request.getServerName();
    }

    /**
     * The default behavior of this method is to return getServerPort() on
     * the wrapped request object.
     */

    public int getServerPort() {
        return this.request.getServerPort();
    }

    /**
     * The default behavior of this method is to return getReader() on the
     * wrapped request object.
     */

    public BufferedReader getReader() throws IOException {
        return this.request.getReader();
    }

    /**
     * The default behavior of this method is to return getRemoteAddr() on
     * the wrapped request object.
     */

    public String getRemoteAddr() {
        return this.request.getRemoteAddr();
    }

    /**
     * The default behavior of this method is to return getRemoteHost() on
     * the wrapped request object.
     */

    public String getRemoteHost() {
        return this.request.getRemoteHost();
    }

    /**
     * The default behavior of this method is to return setAttribute(String
     * name, Object o) on the wrapped request object.
     */

    public void setAttribute(String name, Object o) {
        this.request.setAttribute(name, o);
    }

    /**
     * The default behavior of this method is to call
     * removeAttribute(String name) on the wrapped request object.
     */
    public void removeAttribute(String name) {
        this.request.removeAttribute(name);
    }

    /**
     * The default behavior of this method is to return getLocale() on the
     * wrapped request object.
     */

    public Locale getLocale() {
        return this.request.getLocale();
    }

    /**
     * The default behavior of this method is to return getLocales() on the
     * wrapped request object.
     */

    public Enumeration getLocales() {
        return this.request.getLocales();
    }

    /**
     * The default behavior of this method is to return isSecure() on the
     * wrapped request object.
     */

    public boolean isSecure() {
        return this.request.isSecure();
    }

    /**
     * The default behavior of this method is to return
     * getRequestDispatcher(String path) on the wrapped request object.
     */

    public RequestDispatcher getRequestDispatcher(String path) {
        return this.request.getRequestDispatcher(path);
    }

    /**
     * The default behavior of this method is to return getRealPath(String
     * path) on the wrapped request object.
     */
    @SuppressWarnings("deprecation")
    public String getRealPath(String path) {
        return this.request.getRealPath(path);
    }

    /**
     * The default behavior of this method is to return getRemotePort() on
     * the wrapped request object.
     * 
     * @since 2.4
     */
    public int getRemotePort() {
        return this.request.getRemotePort();
    }

    /**
     * The default behavior of this method is to return getLocalName() on
     * the wrapped request object.
     * 
     * @since 2.4
     */
    public String getLocalName() {
        return this.request.getLocalName();
    }

    /**
     * The default behavior of this method is to return getLocalAddr() on
     * the wrapped request object.
     * 
     * @since 2.4
     */
    public String getLocalAddr() {
        return this.request.getLocalAddr();
    }

    /**
     * The default behavior of this method is to return getLocalPort() on
     * the wrapped request object.
     * 
     * @since 2.4
     */
    public int getLocalPort() {
        return this.request.getLocalPort();
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
     * The default behavior of this method is to return getContextPath() on
     * the wrapped request object.
     */
    public String getContextPath() {
        return this._getHttpServletRequest().getContextPath();
    }

    /**
     * The default behavior of this method is to return getQueryString() on
     * the wrapped request object.
     */
    public String getQueryString() {
        return this._getHttpServletRequest().getQueryString();
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
     * The default behavior of this method is to return getRequestURI() on
     * the wrapped request object.
     */
    public String getRequestURI() {
        return this._getHttpServletRequest().getRequestURI();
    }

    /**
     * The default behavior of this method is to return getRequestURL() on
     * the wrapped request object.
     */
    public StringBuffer getRequestURL() {
        return this._getHttpServletRequest().getRequestURL();
    }

    /**
     * The default behavior of this method is to return getServletPath() on
     * the wrapped request object.
     */
    public String getServletPath() {
        return this._getHttpServletRequest().getServletPath();
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
}
