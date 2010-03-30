/*
 * Copyright 2009-2010 the original author or authors.
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
package net.paoding.rose.jade.jadeinterface.provider.threadlocal;

import java.util.Map;

import net.paoding.rose.jade.jadeinterface.annotation.SQLType;
import net.paoding.rose.jade.jadeinterface.provider.Modifier;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 * 
 */
public class JadeThreadLocal {

    private static final ThreadLocal<JadeThreadLocal> locals = new ThreadLocal<JadeThreadLocal>();

    public static JadeThreadLocal get() {
        return locals.get();
    }

    static JadeThreadLocal set(SQLType sqlType, String sql, Modifier modifier,
            Map<String, ?> parameters) {
        JadeThreadLocal local = new JadeThreadLocal(sqlType, sql, modifier, parameters);
        locals.set(local);
        return local;
    }

    static void remove() {
        locals.remove();
    }

    private SQLType sqlType;

    private String sql;

    private Modifier modifier;

    private Map<String, ?> parameters;

    private JadeThreadLocal(SQLType sqlType, String sql, Modifier modifier,
            Map<String, ?> parameters) {
        this.sqlType = sqlType;
        this.sql = sql;
        this.modifier = modifier;
        this.parameters = parameters;
    }

    public SQLType getSqlType() {
        return sqlType;
    }

    public boolean isReadType() {
        return this.sqlType == SQLType.READ;
    }

    public boolean isWriteType() {
        return this.sqlType == SQLType.WRITE;
    }

    public String getSql() {
        return sql;
    }

    public Modifier getModifier() {
        return modifier;
    }

    public Map<String, ?> getParameters() {
        return parameters;
    }

}
