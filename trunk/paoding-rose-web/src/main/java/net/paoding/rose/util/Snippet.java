package net.paoding.rose.util;

import java.util.Arrays;
import java.util.List;

import net.paoding.rose.RoseFilter;
import net.paoding.rose.web.InterceptorDelegate;
import net.paoding.rose.web.ParamValidator;
import net.paoding.rose.web.impl.module.ControllerRef;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.paramresolver.ParamResolver;

/**
 * 
 * @see RoseFilter
 * 
 */
public class Snippet {

    // 后续可以提取出来放到什么地方，是不是采用模板语言来定义?
    public static String dumpModules(List<Module> modules) {
        final StringBuilder sb = new StringBuilder(4028);
        sb.append("\n--------Modules(Total ").append(modules.size()).append(")--------");
        sb.append("\n");
        for (int i = 0; i < modules.size(); i++) {
            final Module module = modules.get(i);
            sb.append("module ").append(i + 1).append(":");
            sb.append("\n\tmappingPath='").append(module.getMappingPath());
            sb.append("';\n\tpackageRelativePath='").append(module.getRelativePath());
            sb.append("';\n\turl='").append(module.getUrl());
            sb.append("';\n\tcontrollers=[");
            final List<ControllerRef> controllerMappings = module.getControllers();

            for (final ControllerRef controller : controllerMappings) {
                sb.append("'").append(Arrays.toString(controller.getMappingPaths())).append("'=")
                        .append(controller.getControllerClass().getSimpleName()).append(", ");
            }
            if (!controllerMappings.isEmpty()) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("];\n\tparamResolvers=[");
            for (ParamResolver resolver : module.getCustomerResolvers()) {
                sb.append(resolver.getClass().getSimpleName()).append(", ");
            }
            if (module.getCustomerResolvers().size() > 0) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("];\n\tvalidators=[");
            for (ParamValidator validator : module.getValidators()) {
                sb.append(validator.getClass().getSimpleName()).append(", ");
            }
            if (module.getValidators().size() > 0) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("];\n\tinterceptors=[");
            for (InterceptorDelegate interceptor : module.getInterceptors()) {
                sb.append(interceptor.getName()).append("(").append(interceptor.getPriority())
                        .append("), ");
            }
            if (module.getInterceptors().size() > 0) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("];\n\terrorHander=").append(
                    module.getErrorHandler() == null ? "<null>" : module.getErrorHandler());
            // final Mapping<Controller> def = module.getDefaultController();
            // sb.append(";\n\tdefaultController=").append(def == null ? "<null>" : def.getPath());
            sb.append("\n\n");
        }
        sb.append("--------end--------");
        return sb.toString();
    }
}
