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
package net.paoding.rose.ar.hibernate.interceptors;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import net.paoding.rose.web.ControllerInterceptor;
import net.paoding.rose.web.Dispatcher;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationChain;
import net.paoding.rose.web.advancedinterceptor.ActionSelector;
import net.paoding.rose.web.advancedinterceptor.DispatcherSelector;
import net.paoding.rose.web.advancedinterceptor.Ordered;
import net.paoding.rose.web.impl.thread.AfterCompletion;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateAccessor;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author zhiliang.wang [qieqie.wang@paoding.net]
 */
public class OpenSessionInViewInterceptor extends HibernateAccessor implements Ordered,
        ControllerInterceptor, AfterCompletion, ActionSelector, DispatcherSelector {

    private int priority;

    @Override
    public boolean isForAction(Class<?> controllerClazz, Method actionMethod) {
        OpenSessionInView openSessionInView = actionMethod.getAnnotation(OpenSessionInView.class);
        if (openSessionInView == null) {
            openSessionInView = controllerClazz.getAnnotation(OpenSessionInView.class);
        }
        return openSessionInView == null || openSessionInView.enabled();
    }

    @Override
    public boolean isForDispatcher(Dispatcher dispatcher) {
        return true;
    }

    /**
     * Create a new OpenSessionInViewInterceptor, turning the default
     * flushMode to FLUSH_NEVER.
     * 
     * @see #setFlushMode
     */
    public OpenSessionInViewInterceptor() {
        setFlushMode(FLUSH_NEVER);
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Suffix that gets appended to the SessionFactory toString
     * representation for the "participate in existing session handling"
     * request attribute.
     * 
     * @see #getParticipateAttributeName
     */
    public static final String PARTICIPATE_SUFFIX = ".PARTICIPATE";

    private boolean singleSession = true;

    /**
     * Set whether to use a single session for each request. Default is
     * "true".
     * <p>
     * If set to false, each data access operation or transaction will use
     * its own session (like without Open Session in View). Each of those
     * sessions will be registered for deferred close, though, actually
     * processed at request completion.
     * 
     * @see SessionFactoryUtils#initDeferredClose
     * @see SessionFactoryUtils#processDeferredClose
     */
    public void setSingleSession(boolean singleSession) {
        this.singleSession = singleSession;
    }

    /**
     * Return whether to use a single session for each request.
     */
    protected boolean isSingleSession() {
        return singleSession;
    }

    @Override
    public Object roundInvocation(Invocation invocation, InvocationChain chain) throws Exception {
        if ((isSingleSession() && TransactionSynchronizationManager
                .hasResource(getSessionFactory()))
                || SessionFactoryUtils.isDeferredCloseActive(getSessionFactory())) {
            // Do not modify the Session: just mark the request accordingly.
            String participateAttributeName = getParticipateAttributeName();
            Integer count = (Integer) invocation.getRequest()
                    .getAttribute(participateAttributeName);
            int newCount = (count != null) ? count.intValue() + 1 : 1;
            invocation.getRequest().setAttribute(getParticipateAttributeName(),
                    Integer.valueOf(newCount));
        } else {
            if (isSingleSession()) {
                // single session mode
                logger.debug("Opening single Hibernate Session"
                        + " in OpenSessionInViewInterceptor");
                Session session = SessionFactoryUtils.getSession(getSessionFactory(),
                        getEntityInterceptor(), getJdbcExceptionTranslator());
                applyFlushMode(session, false);
                TransactionSynchronizationManager.bindResource(getSessionFactory(),
                        new SessionHolder(session));
            } else {
                // deferred close mode
                SessionFactoryUtils.initDeferredClose(getSessionFactory());
            }
        }

        Object instruction = chain.doNext();

        if (isSingleSession()) {
            // Only potentially flush in single session mode.
            SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
                    .getResource(getSessionFactory());
            logger.debug("Flushing single Hibernate Session" + " in OpenSessionInViewInterceptor");
            try {
                flushIfNecessary(sessionHolder.getSession(), false);
            } catch (HibernateException ex) {
                throw convertHibernateAccessException(ex);
            }
        }
        return instruction;

    }

    @Override
    public void afterCompletion(Invocation invocation, Throwable ex) throws Exception {
        HttpServletRequest request = invocation.getRequest();
        String participateAttributeName = getParticipateAttributeName();
        Integer count = (Integer) request.getAttribute(participateAttributeName);
        if (count != null) {
            // Do not modify the Session: just clear the marker.
            if (count.intValue() > 1) {
                request.setAttribute(participateAttributeName, Integer
                        .valueOf(count.intValue() - 1));
            } else {
                request.removeAttribute(participateAttributeName);
            }
        } else {
            if (isSingleSession()) {
                // single session mode
                SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
                        .unbindResource(getSessionFactory());
                logger.debug("Closing single Hibernate Session"
                        + " in OpenSessionInViewInterceptor");
                SessionFactoryUtils.closeSession(sessionHolder.getSession());
            } else {
                // deferred close mode
                SessionFactoryUtils.processDeferredClose(getSessionFactory());
            }
        }
    }

    /**
     * Return the name of the request attribute that identifies that a
     * request is already filtered. Default implementation takes the
     * toString representation of the SessionFactory instance and appends
     * ".PARTICIPATE".
     * 
     * @see #PARTICIPATE_SUFFIX
     */
    protected String getParticipateAttributeName() {
        return getSessionFactory().toString() + PARTICIPATE_SUFFIX;
    }

}
