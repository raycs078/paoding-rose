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
package net.paoding.rose.ar.hibernate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

/**
 * $ java net.paoding.ar.practice.SchemaExporter mysql5
 * com.xiaonei.blog.domin
 * 
 * @author zhiliang.wang [qieqie.wang@paoding.net]
 * 
 */
public class SchemaExporter {

    private static Map<String, String> dialects = new HashMap<String, String>();
    static {
        dialects.put("mysql", "org.hibernate.dialect.MySQLDialect");
        dialects.put("mysql5", "org.hibernate.dialect.MySQL5Dialect");
        dialects.put("oracle", "org.hibernate.dialect.Oracle9Dialect");
    }

    /**
     * <pre>
     * example:
     * $ java net.paoding.ar.practice.SchemaExporter mysql5 com.xiaonei.blog.domain
     * 
     * <pre>
     * 
     * &#064;param args
     *            [0]=dialect[mysql|mysql5|oralce] [1]=packages&lt;br&gt;
     * 
     * &#064;throws IOException
     * @throws Exception
     */
    public static void main(String[] args) throws IOException, Exception {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);
        String dialect = dialects.get(args[0]);
        config.setProperty("hibernate.dialect", dialect == null ? args[0] : dialect);
        for (Class<?> clazz : new RoseDomainClasses().findDomainClasses()) {
            addAnnotatedClass(config, clazz);
        }
        SchemaExport se = new SchemaExport(config);
        se.setDelimiter(";");
        se.setFormat(true);
        se.create(true, false);
    }

    protected static void addAnnotatedClass(AnnotationConfiguration config, Class<?> clazz) {
        if (null != clazz.getAnnotation(Entity.class)) {
            config.addAnnotatedClass(clazz);
        }
    }
}
