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
package net.paoding.rose.web.var;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;

/**
 * 仅仅提供Rose框架内部使用，外部程序请勿调用.
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public final class PrivateVar {

    private static Log logger = LogFactory.getLog(PrivateVar.class);

    // 当前环境的ServletContext对象，由RoseFilter初始化时设置通过servletContext(ServlerContext)设置进来
    private static ServletContext servletContext;

    //---------------------------------------------------------------

    /**
     * 设置当前环境的 {@link ServletContext}环境对象.该设置方法在只能被调用一次.
     * <p>
     * 设置成功后，Rose框架内部的程序将调用 {@link #servletContext()}方法返回之.
     * 
     * @param servletContext
     * @throws AssertionError 如果已经成功设置过，外部程序又要重复设置时
     * @throws NullPointerException 如果所给的servletContext 为null时
     * @see #servletContext()
     */
    public static void servletContext(ServletContext servletContext) {
        if (servletContext == null) {
            throw new NullPointerException("ServletContext");
        }
        PrivateVar.servletContext = servletContext;
    }

    /**
     * 返回当前环境的 {@link ServletContext}对象，在RoseFilter初始化成功后有效
     * 
     * @return
     * @throws IllegalStateException 如果还未初始化成功时
     * @see #servletContext(ServletContext)
     */
    public static ServletContext servletContext() {
        if (PrivateVar.servletContext == null) {
            throw new IllegalStateException();
        }
        return PrivateVar.servletContext;
    }

    /**
     * 获取当前Web应用的根Spring上下文环境.
     * <p>
     * 所谓根Spring上下文环境的对象都可以应用到各个module中.
     * 
     * @return
     */
    public static WebApplicationContext getRootWebApplicationContext() {
        return (WebApplicationContext) servletContext
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    }

    public static void setRootWebApplicationContext(WebApplicationContext rootContext) {
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                rootContext);
        logger.debug("Published rose.root WebApplicationContext [" + rootContext
                + "] as ServletContext attribute with name ["
                + WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
    }

    /**
     * 获取某个module的上下文环境,这个上下文环境包括了module本身特有的上下文，还包括各个module共享的根环境.
     * 
     * @param modulePath module地址，如果为null等价于
     *        {@link #getRootWebApplicationContext()}
     * @return null,如果给定module的不存在
     */
    public static WebApplicationContext getWebApplicationContext(String modulePath) {
        if (modulePath == null) {
            return getRootWebApplicationContext();
        }
        return (WebApplicationContext) servletContext.getAttribute(WebApplicationContext.class
                .getName()
                + ".module-" + modulePath);
    }

    /**
     * 将构造函数私有化，禁止实例化
     */
    private PrivateVar() {
        throw new AssertionError();
    }

    private static Properties roseProperties;

    public static String getProperty(String name) {
        return getProperty(name, null);
    }

    /**
     * 
     * @param name
     * @return
     */
    public static String getProperty(String name, String def) {
        if (roseProperties == null) {
            String rosePropertiesPath = "rose.properties";
            if (servletContext != null) {
                rosePropertiesPath = "/WEB-INF/rose.properties";
            }
            Properties roseProperties = new Properties();
            File file = new File(servletContext.getRealPath(rosePropertiesPath));
            if (file.exists()) {
                InputStream in = null;
                try {
                    in = new FileInputStream(file);
                    roseProperties.load(in);
                } catch (IOException e) {
                    throw new IllegalArgumentException(rosePropertiesPath, e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                        }
                    }
                }
                PrivateVar.roseProperties = roseProperties;
            } else {
                PrivateVar.roseProperties = new Properties();
            }
        }
        return roseProperties.getProperty(name, def);
    }
}
