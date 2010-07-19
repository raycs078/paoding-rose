/*
 * Copyright 2007-2009 The Apache Software Foundation
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
package net.paoding.rose.ar.init;

import net.paoding.rose.ar.Ar;

import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @author zhiliang.wang [qieqie.wang@paoding.net]
 * 
 */
public class SessionFactorySetterCallback implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext,
                SessionFactory.class);
        if (names.length == 0) {
            throw new NullPointerException("coun't found sessionFactory in applicationContext");
        }
        Ar.setSessionFactory((SessionFactory) applicationContext.getBean(names[0]));
    }

}
