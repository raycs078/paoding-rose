package net.paoding.rose.web.controllers.roseInfo;

import net.paoding.rose.RoseVersion;

class Frame {

    public static String wrap(String msg) {
        return "@<html><head><title>Paoding Rose " + RoseVersion.getVersion()
                + "</title></head><body>" + msg + "<div>" + RoseVersion.getVersion()
                + "</div></body></html>";
    }
}
