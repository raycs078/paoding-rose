package net.paoding.rose.util;

import org.apache.commons.lang.StringUtils;

public class RoseStringUtil {

    public static String relativePathToModulePath(String relativePath) {
        if (relativePath == null) {
            throw new NullPointerException();
        }
        if (relativePath.length() == 0) {
            return "";
        }
        return StringUtils.removeEnd("/" + relativePath, "/");
    }

    public static String mappingPath(String mappingPath) {
        if (mappingPath.length() != 0) {
            mappingPath = StringUtils.removeEnd(mappingPath, "/");
            while (mappingPath.indexOf("//") != -1) {
                mappingPath = mappingPath.replace("//", "/");
            }
        }
        return mappingPath;
    }
}
