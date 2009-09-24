/**
 * Copyright 2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.ar.hibernate;

import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.cfg.AnnotationConfiguration;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

/**
 * 
 * @author zhiliang.wang [qieqie.wang@paoding.net]
 * @since 0.1
 */
public class AutoScanningSessionFactoryBean extends AnnotationSessionFactoryBean {

    public AutoScanningSessionFactoryBean() {
        setNamingStrategy(ImprovedNamingStrategy.INSTANCE);
    }

    // domain实体类应该放到package为yyy.domain以及直接或间接package下(并标注@javax.
    // persistence.Entity)...
    protected void postProcessAnnotationConfiguration(AnnotationConfiguration config)
            throws HibernateException {
        try {
            for (Class<?> clazz : new RoseDomainClasses().findDomainClasses()) {
                if (isEntityClass(clazz)) {
                    config.addAnnotatedClass(clazz);
                }
            }
            if (StringUtils.isBlank(config.getProperty("hibernate.dialect"))) {
                config.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
            }
        } catch (HibernateException e) {
            throw e;
        } catch (Exception e) {
            throw new HibernateException(e);
        }
    }

    protected boolean isEntityClass(Class<?> clazz) {
        return clazz.getAnnotation(Entity.class) != null;
    }

}
