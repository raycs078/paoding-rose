package net.paoding.rose.web.controllers.roseInfo;

import org.springframework.core.SpringVersion;

import net.paoding.rose.RoseVersion;

class Frame {

    public static String wrap(String msg) {
        String roseVersion = RoseVersion.getVersion();
        String springVersion = SpringVersion.getVersion();
        return "@<html><head><title>Paoding Rose " + roseVersion + "@Spring-" + springVersion
                + "</title></head><body>" + msg + "<div>" + roseVersion + "@Spring-"
                + springVersion + "</div></body></html>";
    }
}
