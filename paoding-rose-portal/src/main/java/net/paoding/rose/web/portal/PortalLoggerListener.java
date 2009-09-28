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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PortalLoggerListener implements PortalListener {

    private Log logger = LogFactory.getLog(PortalLoggerListener.class);

    @Override
    public void onPortalCreated(Portal portal) {
        if (logger.isDebugEnabled()) {
            logger.debug("onPortalCreated [" + portal.getInvocation().getRequestPath().getUri()
                    + "]");
        }
    }

    @Override
    public void onPortalReady(Portal portal) {
        if (logger.isDebugEnabled()) {
            logger.debug("onPortalReady [" // NL
                    + portal.getInvocation().getRequestPath().getUri() + "]");
        }
    }

    @Override
    public void onWindowAdded(WindowTask task) {
        if (logger.isDebugEnabled()) {
            logger.debug("onWindowAdded ["
                    + task.getPortal().getInvocation().getRequestPath().getUri() + "]: "
                    + task.getName());
        }
    }

    @Override
    public void onWindowCanceled(WindowTask task) {
        if (logger.isDebugEnabled()) {
            logger.debug("onWindowCanceled ["
                    + task.getPortal().getInvocation().getRequestPath().getUri() + "]: "
                    + task.getName());
        }
    }

    @Override
    public void onWindowDone(WindowTask task, Window window) {
        if (logger.isDebugEnabled()) {
            logger.debug("onWindowDone ["
                    + task.getPortal().getInvocation().getRequestPath().getUri() + "]: "
                    + task.getName());
        }
    }

    @Override
    public void onWindowError(WindowTask task, Window window) {
        if (logger.isDebugEnabled()) {
            logger.debug("onWindowError ["
                    + task.getPortal().getInvocation().getRequestPath().getUri() + "]: "
                    + task.getName(), window.getThrowable());
        }
    }

    @Override
    public void onWindowStarted(WindowTask task) {
        if (logger.isDebugEnabled()) {
            logger.debug("onWindowStarted ["
                    + task.getPortal().getInvocation().getRequestPath().getUri() + "]: "
                    + task.getName());
        }
    }

    @Override
    public void onWindowTimeout(WindowTask task, Window window) {
        if (logger.isDebugEnabled()) {
            logger.debug("onWindowTimeout ["
                    + task.getPortal().getInvocation().getRequestPath().getUri() + "]: "
                    + task.getName() + "; timeout="
                    + (System.currentTimeMillis() - window.getStartTime()));
        }
    }
}
