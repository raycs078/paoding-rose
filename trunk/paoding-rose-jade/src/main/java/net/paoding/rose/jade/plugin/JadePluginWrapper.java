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
package net.paoding.rose.jade.plugin;

import javax.sql.DataSource;

import net.paoding.rose.jade.provider.Modifier;

/**
 * JadePluginWrapper <br>
 * 
 * @author tai.wang@opi-corp.com May 26, 2010 - 4:22:18 PM
 */
public class JadePluginWrapper implements IJadePlugin {

    IJadePlugin[] plugins = null;

    public JadePluginWrapper(IJadePlugin[] plugins) {
        this.plugins = plugins;
    }

    @Override
    public void end() {
        if (null == plugins) {
            return;
        }
        for (IJadePlugin plugin : plugins) {
            plugin.end();
        }
    }

    @Override
    public void start(DataSource dataSource, String sqlString, Modifier modifier,
            Object[] arrayParameters) {
        if (null == plugins) {
            return;
        }
        for (IJadePlugin plugin : plugins) {
            plugin.start(dataSource, sqlString, modifier, arrayParameters);
        }
    }

}
