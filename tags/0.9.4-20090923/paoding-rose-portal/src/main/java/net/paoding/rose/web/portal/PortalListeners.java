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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PortalListeners implements PortalListener {

    private List<PortalListener> listeners = new ArrayList<PortalListener>();

    public void setListeners(List<PortalListener> listeners) {
        List<PortalListener> copied = new ArrayList<PortalListener>(listeners);
        for (PortalListener portalListener : copied) {
            if (portalListener == null) {
                throw new NullPointerException("PortalListener");
            }
        }
        this.listeners = copied;
    }

    public void addListener(PortalListener l) {
        if (l == null) {
            throw new NullPointerException("PortalListener");
        }
        this.listeners.add(l);
    }

    @Override
    public void onPortalCreated(Portal portal) {
        for (PortalListener l : listeners) {
            l.onPortalCreated(portal);
        }
    }

    @Override
    public void onPortalReady(Portal portal) {
        for (PortalListener l : listeners) {
            l.onPortalReady(portal);
        }
    }

    @Override
    public void onWindowAdded(WindowTask task) {
        for (PortalListener l : listeners) {
            l.onWindowAdded(task);
        }
    }

    @Override
    public void onWindowCanceled(WindowTask task) {
        for (PortalListener l : listeners) {
            l.onWindowCanceled(task);
        }
    }

    @Override
    public void onWindowDone(WindowTask task, Window window) {
        for (PortalListener l : listeners) {
            l.onWindowDone(task, window);
        }
    }

    @Override
    public void onWindowError(WindowTask task, Window window) {
        for (PortalListener l : listeners) {
            l.onWindowError(task, window);
        }
    }

    @Override
    public void onWindowStarted(WindowTask task) {
        for (PortalListener l : listeners) {
            l.onWindowStarted(task);
        }
    }

    @Override
    public void onWindowTimeout(WindowTask task, Window window) {
        for (PortalListener l : listeners) {
            l.onWindowTimeout(task, window);
        }
    }

}
