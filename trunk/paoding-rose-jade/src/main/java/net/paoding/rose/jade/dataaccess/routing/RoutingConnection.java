/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.dataaccess.routing;

import java.sql.Connection;

/**
 * {@link RoutingConnection} 由 {@link RoutingDataSource}
 * 创建，封装对散库系统中的数据库连接抽象。
 * 
 * @author qieqie
 * 
 */
public interface RoutingConnection extends Connection {

    /**
     * 参数的名字
     * 
     * @see Connection#setClientInfo(String, String)
     */
    public static final String PATH = RoutingConnection.class.getName() + "#path";

    /**
     * catalog参数的名字
     * 
     * @see Connection#setClientInfo(String, String)
     */
    public static final String CATALOG = RoutingConnection.class.getName() + "#catalog";

    /**
     * node参数的名字
     * 
     * @see Connection#setClientInfo(String, String)
     */
    public static final String NODE = RoutingConnection.class.getName() + "#node";

    /**
     * 
     * @return
     */
    public String getUsername();

    /**
     * 
     * @return
     */
    public String getPassword();

}
