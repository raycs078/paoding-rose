package net.paoding.rose.scanner;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.paoding.rose.scanning.ResourceRef;
import net.paoding.rose.scanning.RoseScanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RoseFolders {

    protected static Log logger = LogFactory.getLog(RoseModuleInfos.class);

    @SuppressWarnings("unchecked")
    public static List<ResourceRef> getRoseFolders(String[] namespaces) throws IOException {
        RoseScanner roseScanner = RoseScanner.getInstance();
        List<ResourceRef> resources = new ArrayList<ResourceRef>();
        try {
            // 为兼容旧的scanning // 2010.03.24
            // TODO: 2010.04.24之后应该删除此try-catch代码，直接使用roseScanner.getClassesFolderResources(namespaces)
            Method getClassesFolderResources = RoseScanner.class.getMethod(
                    "getClassesFolderResources", String[].class);
            Method getJarResources = RoseScanner.class.getMethod("getJarResources", String[].class);
            resources.addAll((List<ResourceRef>) getClassesFolderResources.invoke(roseScanner,
                    (Object) namespaces));
            resources.addAll((List<ResourceRef>) getJarResources.invoke(roseScanner,
                    (Object) namespaces));
        } catch (NoSuchMethodException e) {
            if (namespaces != null && namespaces.length > 0) {
                throw new IllegalStateException(
                        "PLEASE UPDATE paoding-rose-scanning.jar for support rose namespaces filter, or remove roseFilter's namespaces init-param");
            }
            logger.warn(//
                    "PLEASE UPDATE paoding-rose-scanning.jar for support rose namespaces filter");
            resources.addAll(roseScanner.getClassesFolderResources());
            resources.addAll(roseScanner.getJarResources());
        } catch (Exception e) {
            logger.error("", e);
        }
        return resources;
    }
}
