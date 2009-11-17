package net.paoding.rose.testcases.web.impl.thread;

import java.lang.reflect.Method;

import net.paoding.rose.testcases.AbstractControllerTest;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.mapping.MappingImpl;
import net.paoding.rose.web.impl.mapping.MatchMode;
import net.paoding.rose.web.impl.thread.ActionEngine;
import net.paoding.rose.web.impl.thread.ControllerEngine;
import net.paoding.rose.web.impl.thread.MatchResult;
import net.paoding.rose.web.paramresolver.MethodParameterResolver;
import net.paoding.rose.web.paramresolver.ParameterNameDiscovererImpl;
import net.paoding.rose.web.paramresolver.ResolverFactoryImpl;

import org.springframework.mock.web.MockFilterChain;

public class MethodParameterResolverMatchResultTest extends AbstractControllerTest {

    

    public void testInt() throws Exception {
        Object[] parameters = resolveMethodParameters("innt");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(Integer.class, parameters[0].getClass());
        assertEquals(123456, parameters[0]);
    }

    public void testInteger() throws Exception {
        Object[] parameters = resolveMethodParameters("integer");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(Integer.class, parameters[0].getClass());
        assertEquals(123456, parameters[0]);
    }

    public void testBool() throws Exception {
        Object[] parameters = resolveMethodParameters("bool");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(Boolean.class, parameters[0].getClass());
        assertEquals(true, parameters[0]);
    }

    public void testBoool() throws Exception {
        Object[] parameters = resolveMethodParameters("boool");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(Boolean.class, parameters[0].getClass());
        assertEquals(true, parameters[0]);
    }

    public void testLoong() throws Exception {
        Object[] parameters = resolveMethodParameters("loong");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(Long.class, parameters[0].getClass());
        assertEquals(123456L, parameters[0]);
    }

    public void testLooong() throws Exception {
        Object[] parameters = resolveMethodParameters("looong");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(Long.class, parameters[0].getClass());
        assertEquals(123456L, parameters[0]);
    }

    public void testString() throws Exception {
        Object[] parameters = resolveMethodParameters("string");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(String.class, parameters[0].getClass());
        assertEquals("123456", parameters[0]);
    }

    public void testNullPrimitiveBool() throws Exception {
        Object[] parameters = resolveMethodParameters("nullPrimitiveBool");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(Boolean.class, parameters[0].getClass());
        assertEquals(false, parameters[0]);
    }

    public void testNullPrimitiveBoolWrapper() throws Exception {
        Object[] parameters = resolveMethodParameters("nullPrimitiveBoolWrapper");
        assertNotNull(parameters);
        assertNull(parameters[0]);
    }
    
    protected Object[] resolveMethodParameters(String methodName) throws Exception {
        String uri =  "/methodparameter/123456/true/" + methodName;
        invoke(uri, "GET", "");
        Invocation inv = (Invocation) request.getAttribute("$$paoding-rose.invocation");
        return inv.getMethodParameters();
    }

//    protected Object[] resolveMethodParameters2(String methodName) throws Exception {
//        Method method = findMethod(methodName);
//        assertNotNull("not found method named: " + methodName, method);
//
//        MappingImpl<ControllerEngine> controllerMapping = new MappingImpl<ControllerEngine>(
//                "/{controller.id:[0-9]+}/{controller.bool}", MatchMode.PATH_STARTS_WITH,
//                new ReqMethod[] { ReqMethod.ALL }, (ControllerEngine) null);
//        MatchResult<ControllerEngine> controllerMatchResult = controllerMapping.match(
//                "/123456/true/" + methodName, request.getMethod());
//
//        //        inv.setControllerMatchResult(controllerMatchResult);
//        //        assertNotNull(inv.getControllerMatchResult());
//
//        MappingImpl<ActionEngine> actionMapping = new MappingImpl<ActionEngine>("/" + methodName,
//                MatchMode.PATH_STARTS_WITH, new ReqMethod[] { ReqMethod.ALL }, (ActionEngine) null);
//        MatchResult<ActionEngine> actionMatchResult = actionMapping.match("/" + methodName, request
//                .getMethod());
//        //        inv.setActionMatchResult(actionMatchResult);
//        //        assertNotNull(inv.getActionMatchResult());
//        inv.setMethod(method);
//
//        ParameterNameDiscovererImpl parameterNameDiscoverer = new ParameterNameDiscovererImpl();
//        ResolverFactoryImpl resolverFactory = new ResolverFactoryImpl();
//        MethodParameterResolver resolver = new MethodParameterResolver(MockController.class,
//                method, parameterNameDiscoverer, resolverFactory);
//        return resolver.resolve(inv, paramenterBindingResult);
//    }

}
