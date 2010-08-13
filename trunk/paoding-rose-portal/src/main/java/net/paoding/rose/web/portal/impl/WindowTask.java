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

import java.util.Random;
import java.util.concurrent.ExecutorService;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link WindowTask} 把一个窗口任务进行封装，使可以提交到 {@link ExecutorService} 执行。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
final class WindowTask implements Runnable {

	private static final String VERSION_PARAM_NAME = "__ver";
	
    private static final Log logger = LogFactory.getLog(WindowTask.class);

    private final WindowImpl window;
    
    private Random random = new Random();
    
    public WindowTask(WindowImpl window) {
        if (window == null) {
            throw new NullPointerException("window");
        }
        this.window = window;
    }

    public WindowImpl getWindow() {
        return window;
    }

    @Override
    public void run() {
        try {
            // started
            window.getPortal().onWindowStarted(window);

            // doRequest
            final WindowRequest request = window.getRequest();
            String windowPath = window.getPath();
            if (windowPath.length() == 0 || windowPath.charAt(0) != '/') {
                String requestUri = request.getRequestURI();
                if (!requestUri.endsWith("/")) {
                    requestUri = requestUri + "/";
                }
                windowPath = requestUri + windowPath;
            }

            //考虑window在forward的过程中可能遇到304的问题，所以在path中加入版本号
            if (windowPath.indexOf('?') >= 0) {
            	windowPath += "&" + generateVersionParam();
            } else {
            	windowPath += "?" + generateVersionParam();
            }
            
            if (logger.isDebugEnabled()) {
            	logger.debug("forward to window path: " + windowPath);
            }
            
            final RequestDispatcher rd = request.getRequestDispatcher(windowPath);
            request.setAttribute("$$paoding-rose-portal.window", window);
            if (window.getResponse().isCommitted()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("onWindowTimeout: response has committed. [" + window.getName()
                            + "]@" + window.getPortal());
                }
                window.getPortal().onWindowTimeout(window);
                return;
            }
            rd.forward(request, window.getResponse());

            // done!
            window.getPortal().onWindowDone(window);
        } catch (Throwable e) {
            logger.error("", e);
            window.setThrowable(e);
            window.getPortal().onWindowError(window);
        } finally {

        	final HttpServletRequest wrapper = window.getPortal().getRequest();
            final PortalRequest portalRequest = PortalRequest.unwrapPortalRequest(wrapper);
            // remove request from ThreadLocal in PortalRequest 
            // 销毁在PortalRequest的ThreadLocal成员变量中保存的与 当前线程相关的request对象，以防内存泄漏。
            portalRequest.setRequest(null);
        	
        }
    }

	/**
	 * 生成version参数
	 * @return
	 */
	private String generateVersionParam() {
		return VERSION_PARAM_NAME + "=" + Integer.toHexString(random.nextInt());
	}

    @Override
    public String toString() {
        return "window [name=" + window.getName() + ", path=" + window.getPath() + "]";
    }
    
}
