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

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.paramresolver.ParamResolverBean;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PortalResolver implements ParamResolverBean {

    private PortalFactory portalFactory;

    private long defaultTimeout;

    public void setPortalFactory(PortalFactory portalFactory) {
        this.portalFactory = portalFactory;
    }

    public PortalFactory getPortalFactory() {
        return portalFactory;
    }

    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public long getDefaultTimeout() {
        return defaultTimeout;
    }

    @Override
    public boolean supports(Class<?> parameterType) {
        if (portalFactory == null) {
            return false;
        }
        return parameterType == Portal.class;
    }

    @Override
    public Portal resolve(Class<?> parameterType, int replicatedCount, int indexOfReplicated,
            Invocation inv, String parameterName, Param paramAnnotation) throws Exception {
        Portal portal = portalFactory.createPortal(inv);
        // 换request对象
        inv.setRequest(new PortalRequest(portal));
        //
        long timeout = this.defaultTimeout;
        PortalSetting portalSetting = inv.getMethod().getAnnotation(PortalSetting.class);
        if (portalSetting != null && portalSetting.timeout() >= 0) {
            long annotationTimeout = portalSetting.timeUnit().toMillis(portalSetting.timeout());
            // < 0的情况，是PortalSetting的默认设置，即如果PortalSetting没有设置有效的timeout，则使用defaultTimeout策略
            // == 0的情况表示并且要求表示不需要设置超时时间，并且也不使用defaultTimeout策略
            if (annotationTimeout >= 0) {
                timeout = annotationTimeout;
            }
        }
        if (timeout > 0) {
            portal.setTimeout(timeout);
        }
        inv.setAttribute("$$paoding-rose-portal.portal", portal);
        return portal;
    }

}
