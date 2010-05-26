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

    public static boolean startsWith(CharSequence input, String prefix) {
        if (input.length() < prefix.length()) {
            return false;
        }
        if (input.getClass() == String.class) {
            return ((String) input).startsWith(prefix);
        }
        int len = prefix.length();
        for (int i = 0; i < len; i++) {
            char pi = input.charAt(i);
            char ci = prefix.charAt(i);
            if (pi != ci) {
                return false;
            }
        }
        return true;
    }
}
