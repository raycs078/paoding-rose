package net.paoding.rose.testcases.web.impl.thread;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequestWrapper;

import junit.framework.TestCase;
import net.paoding.rose.web.InvocationUtils;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.mapping.MappingImpl;
import net.paoding.rose.web.impl.mapping.MatchMode;
import net.paoding.rose.web.impl.thread.ActionEngine;
import net.paoding.rose.web.impl.thread.ControllerEngine;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.impl.thread.MatchResult;
import net.paoding.rose.web.impl.validation.ParameterBindingResult;
import net.paoding.rose.web.paramresolver.MethodParameterResolver;
import net.paoding.rose.web.paramresolver.ParameterNameDiscovererImpl;
import net.paoding.rose.web.paramresolver.ResolverFactoryImpl;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.multipart.MultipartFile;

public class MethodParameterResolverMatchResultTest extends TestCase {

    MockServletContext servletcontext;

    MockHttpServletRequest request;

    MockHttpServletResponse response;

    InvocationBean inv;

    ParameterBindingResult paramenterBindingResult;

    //
    MockMultipartHttpServletRequest multipartRequest;

    MultipartFile file1;

    MultipartFile file2;

    @Override
    protected void setUp() throws Exception {
        servletcontext = new MockServletContext();
        request = new MockHttpServletRequest(servletcontext);
        request.setMethod("GET");
        response = new MockHttpServletResponse();
        inv = new InvocationBean();
        inv.setRequest(request);
        inv.setResponse(response);
        InvocationUtils.bindRequestToCurrentThread(request);
        InvocationUtils.bindInvocationToRequest(inv, new HttpServletRequestWrapper(request));
        paramenterBindingResult = new ParameterBindingResult(inv);

        //
        multipartRequest = new MockMultipartHttpServletRequest();
        multipartRequest.setMethod("POST");
        file1 = new MockMultipartFile("file1", "originalFileName1", "application/oct-stream",
                new byte[] { 1, 2 });
        file2 = new MockMultipartFile("file2", "originalFileName2", "application/oct-stream",
                new byte[] { 3, 4 });
        multipartRequest.addFile(file1);
        multipartRequest.addFile(file2);
    }

    class MockController {

        public void innt(@Param("controller.id") int cid) {
        }

        public void integer(@Param("controller.id") Integer cid) {
        }

        public void bool(@Param("controller.bool") boolean cid) {
        }

        public void boool(@Param("controller.bool") Boolean cid) {
        }

        public void loong(@Param("controller.id") long cid) {
        }

        public void looong(@Param("controller.id") Long cid) {
        }

        public void string(@Param("controller.id") String cid) {
        }

        public void nullPrimitiveBool(@Param("controller.abcded") boolean cid) {
        }

        public void nullPrimitiveBoolWrapper(@Param("controller.abcded") Boolean cid) {
        }
    }

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
        Method method = findMethod(methodName);
        assertNotNull("not found method named: " + methodName, method);

        MappingImpl<ControllerEngine> controllerMapping = new MappingImpl<ControllerEngine>(
                "/{controller.id:[0-9]+}/{controller.bool}", MatchMode.PATH_STARTS_WITH,
                new ReqMethod[] { ReqMethod.ALL }, (ControllerEngine) null);
        MatchResult<ControllerEngine> controllerMatchResult = controllerMapping.match(
                "/123456/true/" + methodName, request.getMethod());

        inv.setControllerMatchResult(controllerMatchResult);
        assertNotNull(inv.getControllerMatchResult());

        MappingImpl<ActionEngine> actionMapping = new MappingImpl<ActionEngine>("/" + methodName,
                MatchMode.PATH_STARTS_WITH, new ReqMethod[] { ReqMethod.ALL }, (ActionEngine) null);
        MatchResult<ActionEngine> actionMatchResult = actionMapping.match("/" + methodName, request
                .getMethod());
        inv.setActionMatchResult(actionMatchResult);
        assertNotNull(inv.getActionMatchResult());

        ParameterNameDiscovererImpl parameterNameDiscoverer = new ParameterNameDiscovererImpl();
        ResolverFactoryImpl resolverFactory = new ResolverFactoryImpl();
        MethodParameterResolver resolver = new MethodParameterResolver(method,
                parameterNameDiscoverer, resolverFactory);
        return resolver.resolve(inv, paramenterBindingResult);
    }

    private Method findMethod(String name) {
        try {
            Method[] methods = MockController.class.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(name)) {
                    return method;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
