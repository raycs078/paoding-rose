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
package net.paoding.rose.scanning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class LoadScope {

    private Map<String, String[]> load = new HashMap<String, String[]>();

    public LoadScope(String loadScope, String defType) {
        init(loadScope, defType);
    }

    private void init(String loadScope, String defType) {
        if (StringUtils.isBlank(loadScope) || "*".equals(loadScope)) {
            return;
        }
        loadScope = loadScope.trim();
        String[] componetConfs = StringUtils.split(loadScope, ";");
        for (String componetConf : componetConfs) {
            if (StringUtils.isBlank(loadScope)) {
                continue;
            }
            // 代表"controllers=com.renren.xoa, com.renren.yourapp"串
            componetConf = componetConf.trim();
            int componetTypeIndex;
            String componetType = defType; // 代表"controllers", "applicationContext", "dao", "messages", "*"等串
            String componetConfValue = componetConf;
            if ((componetTypeIndex = componetConf.indexOf('=')) != -1) {
                componetType = componetConf.substring(0, componetTypeIndex).trim();
                componetConfValue = componetConf.substring(componetTypeIndex + 1).trim();
            }
            if (componetType.startsWith("!")) {
                componetType = componetType.substring(1);
            } else {
                componetConfValue = componetConfValue + ", net.paoding.rose";
            }
            String[] packages = StringUtils.split(componetConfValue, ", \t\n\r\0");//都好和\t之间有一个空格
            this.load.put(componetType, packages);
        }
    }

    public String[] getScope(String componentType) {
        String[] scope = this.load.get(componentType);
        if (scope == null) {
            scope = this.load.get("*");
        }
        return scope;
    }

    public List<ResourceRef> filter(String componentType, List<ResourceRef> input)
            throws IOException {
        assert componentType != null;
        if (this.load.size() == 0 || this.load.get(componentType) == null) {
            return input;
        }
        String[] packages = load.get(componentType);
        if (packages == null) {
            packages = this.load.get("*");
        }
        ArrayList<ResourceRef> output = new ArrayList<ResourceRef>(input);
        for (Iterator<ResourceRef> iter = output.iterator(); iter.hasNext();) {
            ResourceRef r = iter.next();
            for (String pkg : packages) {
                pkg = pkg.replace('.', '/');
                Resource pkgResource = r.getInnerResource(pkg);
                if (!pkgResource.exists()) {
                    iter.remove();
                }
            }
        }
        return output;
    }
}
