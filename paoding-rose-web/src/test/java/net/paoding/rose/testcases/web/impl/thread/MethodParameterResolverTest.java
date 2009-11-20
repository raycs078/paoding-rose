package net.paoding.rose.testcases.web.impl.thread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationUtils;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.ParamConf;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.impl.validation.ParameterBindingResult;
import net.paoding.rose.web.paramresolver.MethodParameterResolver;
import net.paoding.rose.web.paramresolver.ParameterNameDiscovererImpl;
import net.paoding.rose.web.paramresolver.ResolverFactoryImpl;
import net.paoding.rose.web.var.Flash;
import net.paoding.rose.web.var.Model;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

public class MethodParameterResolverTest extends TestCase {

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
        inv = new InvocationBean(request, response, new RequestPath(request));
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

        public void inv(Invocation inv) {
        }

        public void request(ServletRequest request) {
        }

        public void request2(HttpServletRequest request) {
        }

        public void response(ServletResponse response) {
        }

        public void response2(HttpServletResponse response) {
        }

        public void session(HttpSession session) {
        }

        public void session2(@Param(value = "", required = false) HttpSession session) {
        }

        public void model(Model request) {
        }

        public void flash(Flash request) {
        }

        public void hello(@Param("name") String name, @Param("int") int i,
                @Param("bool") boolean bool) {
        }

        public void multipart0(@Param("file1") MultipartFile file1,
                @Param("file2") MultipartFile file2, MultipartRequest request) {
        }

        public void multipart(@Param("file1") MultipartFile file1,
                @Param("file2") MultipartFile file2, MultipartRequest request) {
        }

        public void multipart2(MultipartRequest request, @Param("file2") MultipartFile file1,
                @Param("file1") MultipartFile file2) {
        }

        public void array(@Param("int") int[] ints, @Param("integer") Integer[] integers,
                @Param("name") String[] names, @Param("bool") boolean[] bools) {
        }

        public void list(@Param(value = "int") List<Integer> ints, //
                @Param(value = "integer") ArrayList<Integer> integers, //
                @Param("name") Collection<String> names, //
                @Param(value = "bool") LinkedList<Boolean> bools) {
        }

        public void set(@Param(value = "int") Set<Integer> ints, //
                @Param(value = "integer") HashSet<Integer> integers, //
                @Param("name") Set<String> names, //
                @Param(value = "bool") TreeSet<Boolean> bools) {
        }

        public void map(@Param("ss") Map<String, String> string2string,
                @Param(value = "is") Map<Integer, String> int2string,
                @Param(value = "null") Map<String, Float> adf,
                @Param(value = "sf") Map<String, Float> string2float,
                @Param(value = "ib") Map<Integer, Boolean> int2bool) {
        }

        public void date(@Param("d") Date d, @Param("sd") java.sql.Date sd, @Param("t") Time t,
                @Param("ts") Timestamp ts) {
        }

        public void datedef(@Param(value = "d", def = "") Date d,
                @Param(value = "sd", def = "123456") java.sql.Date sd, @Param(value = "t") Time t,
                @Param("ts") Timestamp ts) {
        }

        public void datePattern1(@Param(value = "d", conf = {
                @ParamConf(name = "pattern", value = "yyyy.MMddHHmmss"),
                @ParamConf(name = "pattern", value = "yyMMddHHmmss") }) Date d,
                @Param(value = "t", conf = { @ParamConf(name = "pattern", value = "HHmmss"),
                        @ParamConf(name = "pattern", value = "ss.HHmm") }) Time t) {
        }

        public void datePattern2(
                @Param(value = "d", conf = { @ParamConf(name = "pattern", value = "long") }) Date d,
                @Param(value = "t", conf = { @ParamConf(name = "pattern", value = "long") }) Time t) {
        }

        public void userBean(User user) {
        }

        public void userBean2(@Param("ua") User a, @Param("ub") User b) {
        }

        public void bindingResult(User user, BindingResult userBr) {
        }

        //

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

        public void nullPrimitiveInt(@Param("controller.afadfa2dfdafd") int cid) {
        }

        public void nullPrimitiveBool(@Param("controller.abcda4dfadfed") boolean cid) {
        }

        public void nullPrimitiveBoolWrapper(@Param("controller.abcdeadf2d") Boolean cid) {
        }

        public void inf(Interface in, @Param("a") int a, @Param("controller.bool") boolean b) {

        }

    }

    interface Interface {

    }

    static class User {

        private String name;

        private Long id;

        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

    }

    public void testInvocation() throws Exception {
        Object[] parameters = resolveMethodParameters("inv");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertSame(inv, parameters[0]);
    }

    public void testRequest() throws Exception {
        Object[] parameters = resolveMethodParameters("request");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertSame(request, parameters[0]);
    }

    public void testRequest2() throws Exception {
        Object[] parameters = resolveMethodParameters("request2");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertSame(request, parameters[0]);
    }

    public void testResponse() throws Exception {
        Object[] parameters = resolveMethodParameters("response");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertSame(response, parameters[0]);
    }

    public void testResponse2() throws Exception {
        Object[] parameters = resolveMethodParameters("response2");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertSame(response, parameters[0]);
    }

    public void testSession() throws Exception {
        Object[] parameters = resolveMethodParameters("session");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertSame(request.getSession(), parameters[0]);
    }

    public void testSession2() throws Exception {
        Object[] parameters = resolveMethodParameters("session2");
        assertNotNull(parameters);
        assertNull(parameters[0]);
    }

    public void testModel() throws Exception {
        Object[] parameters = resolveMethodParameters("model");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertSame(inv.getModel(), parameters[0]);
    }

    public void testFlash() throws Exception {
        Object[] parameters = resolveMethodParameters("flash");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertSame(inv.getFlash(), parameters[0]);
    }

    public void testHello() throws Exception {
        request.addParameter("name", "rose");
        request.addParameter("int", "345");
        request.addParameter("bool", "true");

        assertEquals("rose", request.getParameter("name"));

        Object[] parameters = resolveMethodParameters("hello");
        assertNotNull(parameters);
        assertEquals("rose", parameters[0]);
        assertEquals(345, parameters[1]);
        assertEquals(true, parameters[2]);
    }

    public void testMultiPartFile0() throws Exception {
        Object[] parameters = resolveMethodParameters("multipart0");
        assertNotNull(parameters);
        assertNull(parameters[0]);
        assertNull(parameters[1]);
        assertNull(parameters[2]);
    }

    public void testMultiPartFile() throws Exception {
        inv.setRequest(multipartRequest);
        InvocationUtils.bindRequestToCurrentThread(multipartRequest);
        InvocationUtils.bindInvocationToRequest(inv,
                new HttpServletRequestWrapper(multipartRequest));
        Object[] parameters = resolveMethodParameters("multipart");
        assertNotNull(parameters);
        assertSame(file1, parameters[0]);
        assertSame(file2, parameters[1]);
        assertSame(multipartRequest, parameters[2]);
    }

    public void testMultiPartFile2() throws Exception {
        inv.setRequest(multipartRequest);
        InvocationUtils.bindRequestToCurrentThread(multipartRequest);
        InvocationUtils.bindInvocationToRequest(inv,
                new HttpServletRequestWrapper(multipartRequest));
        Object[] parameters = resolveMethodParameters("multipart2");
        assertNotNull(parameters);
        assertSame(multipartRequest, parameters[0]);
        assertSame(file2, parameters[1]);
        assertSame(file1, parameters[2]);
    }

    public void testArray() throws Exception {
        request.addParameter("int", "1");
        request.addParameter("int", "2");
        request.addParameter("int", "3");

        request.addParameter("integer", "4, 5, 6");

        request.addParameter("bool", "true");
        request.addParameter("bool", "false");

        Object[] parameters = resolveMethodParameters("array");
        assertNotNull(parameters);

        assertTrue("unexpected type " + parameters[0].getClass().getName(),
                parameters[0] instanceof int[]);
        assertTrue(Arrays.equals(new int[] { 1, 2, 3 }, (int[]) parameters[0]));

        assertTrue("unexpected type " + parameters[1].getClass().getName(),
                parameters[1] instanceof Integer[]);
        assertTrue(Arrays.equals(new Integer[] { 4, 5, 6 }, (Integer[]) parameters[1]));

        assertNotNull(parameters[2]);
        assertTrue("unexpected type " + parameters[2].getClass().getName(),
                parameters[2] instanceof String[]);
        assertTrue(((String[]) parameters[2]).length == 0);

        assertTrue("unexpected type " + parameters[3].getClass().getName(),
                parameters[3] instanceof boolean[]);
        assertTrue(Arrays.equals(new boolean[] { true, false }, (boolean[]) parameters[3]));
    }

    public void testList() throws Exception {
        request.addParameter("int", "1");
        request.addParameter("int", "2");
        request.addParameter("int", "3");

        request.addParameter("integer", "4, 5, 6");

        request.addParameter("bool", "true");
        request.addParameter("bool", "false");

        Object[] parameters = resolveMethodParameters("list");
        assertNotNull(parameters);

        assertTrue("unexpected type " + parameters[0].getClass().getName(),
                parameters[0] instanceof List);
        assertTrue(Arrays.equals(new Integer[] { 1, 2, 3 }, ((List<?>) parameters[0])
                .toArray(new Integer[0])));

        assertTrue("unexpected type " + parameters[1].getClass().getName(),
                parameters[1] instanceof List);
        assertTrue(Arrays.equals(new Integer[] { 4, 5, 6 }, ((List<?>) parameters[1])
                .toArray((new Integer[0]))));

        assertNotNull(parameters[2]);
        assertTrue("unexpected type " + parameters[2].getClass().getName(),
                parameters[2] instanceof List);
        assertTrue(((List<?>) parameters[2]).size() == 0);

        assertTrue("unexpected type " + parameters[3].getClass().getName(),
                parameters[3] instanceof List);
        assertTrue(Arrays.equals(new Boolean[] { true, false }, ((List<?>) parameters[3])
                .toArray((new Boolean[0]))));
    }

    public void testSet() throws Exception {
        request.addParameter("int", "1");
        request.addParameter("int", "2");
        request.addParameter("int", "3");

        request.addParameter("integer", "4, 5, 6");

        request.addParameter("bool", "true");
        request.addParameter("bool", "false");

        Object[] parameters = resolveMethodParameters("set");
        assertNotNull(parameters);

        assertTrue("unexpected type " + parameters[0].getClass().getName(),
                parameters[0] instanceof Set);
        assertTrue(Arrays.equals(new Integer[] { 1, 2, 3 }, ((Set<?>) parameters[0])
                .toArray(new Integer[0])));

        assertTrue("unexpected type " + parameters[1].getClass().getName(),
                parameters[1] instanceof Set);
        assertTrue(Arrays.equals(new Integer[] { 4, 5, 6 }, ((Set<?>) parameters[1])
                .toArray((new Integer[0]))));

        assertNotNull(parameters[2]);
        assertTrue("unexpected type " + parameters[2].getClass().getName(),
                parameters[2] instanceof Set);
        assertTrue(((Set<?>) parameters[2]).size() == 0);

        assertTrue("unexpected type " + parameters[3].getClass().getName(),
                parameters[3] instanceof Set);
        Boolean[] bools = ((Set<?>) parameters[3]).toArray((new Boolean[0]));
        assertEquals(2, bools.length);
        assertTrue(ArrayUtils.contains(bools, true));
        assertTrue(ArrayUtils.contains(bools, false));
    }

    public void testMap() throws Exception {
        request.addParameter("ss:ss1", "1");
        request.addParameter("ss:ss2", "2");
        request.addParameter("ss:ss3", "3");

        request.addParameter("is:1", "11");
        request.addParameter("is:2", "12");
        request.addParameter("is:3", "13");

        request.addParameter("sf:ss1", "1.02");
        request.addParameter("sf:ss2", "2.12");
        request.addParameter("sf:ss3", "13");

        request.addParameter("ib:1", "1");
        request.addParameter("ib:2", "true");
        request.addParameter("ib:3", "false");

        Object[] parameters = resolveMethodParameters("map");
        assertNotNull(parameters);

        assertTrue("unexpected type " + parameters[0].getClass().getName(),
                parameters[0] instanceof Map);
        Map<?, ?> ss = (Map<?, ?>) parameters[0];
        assertEquals("1", ss.get("ss1"));
        assertEquals("2", ss.get("ss2"));
        assertEquals("3", ss.get("ss3"));

        assertTrue("unexpected type " + parameters[1].getClass().getName(),
                parameters[1] instanceof Map);
        Map<?, ?> is = (Map<?, ?>) parameters[1];
        assertEquals("11", is.get(1));
        assertEquals("12", is.get(2));
        assertEquals("13", is.get(3));

        assertNotNull(parameters[2]);
        assertTrue("unexpected type " + parameters[2].getClass().getName(),
                parameters[2] instanceof Map);
        assertTrue(((Map<?, ?>) parameters[2]).size() == 0);

        assertTrue("unexpected type " + parameters[3].getClass().getName(),
                parameters[3] instanceof Map);
        Map<?, ?> sf = (Map<?, ?>) parameters[3];
        assertEquals(1.02f, sf.get("ss1"));
        assertEquals(2.12f, sf.get("ss2"));
        assertEquals(13f, sf.get("ss3"));

        assertTrue("unexpected type " + parameters[4].getClass().getName(),
                parameters[4] instanceof Map);
        Map<?, ?> ii = (Map<?, ?>) parameters[4];
        assertEquals(true, ii.get(1));
        assertEquals(true, ii.get(2));
        assertEquals(false, ii.get(3));
    }

    public void testDate() throws Exception {
        request.addParameter("d", "2001-04-23 10:00:42");
        request.addParameter("sd", "2001-04-23");
        request.addParameter("t", "10:00:42");
        request.addParameter("ts", "2001-04-23 10:00:42");
        Object[] parameters = resolveMethodParameters("date");
        assertNotNull(parameters);

        assertEquals(Date.class, parameters[0].getClass());
        assertEquals(java.sql.Date.class, parameters[1].getClass());
        assertEquals(java.sql.Time.class, parameters[2].getClass());
        assertEquals(java.sql.Timestamp.class, parameters[3].getClass());

        assertEquals("2001-04-23 10:00:42", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format((Date) parameters[0]));
        assertEquals("2001-04-23", new SimpleDateFormat("yyyy-MM-dd")
                .format((java.sql.Date) parameters[1]));
        assertEquals("10:00:42", new SimpleDateFormat("HH:mm:ss")
                .format((java.sql.Time) parameters[2]));
        assertEquals("2001-04-23 10:00:42", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format((java.sql.Timestamp) parameters[3]));
    }

    public void testDatedef() throws Exception {
        long low = System.currentTimeMillis();
        Object[] parameters = resolveMethodParameters("datedef");
        long high = System.currentTimeMillis();
        assertNotNull(parameters);

        assertEquals(Date.class, parameters[0].getClass());
        assertEquals(java.sql.Date.class, parameters[1].getClass());
        assertNull(parameters[2]);
        assertNull(parameters[3]);

        assertTrue(((Date) parameters[0]).getTime() >= low);
        assertTrue(((Date) parameters[0]).getTime() <= high);
        assertEquals(123456, ((java.sql.Date) parameters[1]).getTime());
    }

    public void testDatePattern11() throws Exception {
        request.addParameter("d", "20010423100042");
        request.addParameter("t", "100042");

        Object[] parameters = resolveMethodParameters("datePattern1");
        assertNotNull(parameters);

        assertEquals(Date.class, parameters[0].getClass());
        assertEquals(java.sql.Time.class, parameters[1].getClass());

        assertEquals("2001-04-23 10:00:42", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format((Date) parameters[0]));
        assertEquals("10:00:42", new SimpleDateFormat("HH:mm:ss")
                .format((java.sql.Time) parameters[1]));
    }

    public void testDatePattern12() throws Exception {
        request.addParameter("d", "010423100042");
        request.addParameter("t", "42.1000");

        Object[] parameters = resolveMethodParameters("datePattern1");
        assertNotNull(parameters);

        assertEquals(Date.class, parameters[0].getClass());
        assertEquals(java.sql.Time.class, parameters[1].getClass());

        assertEquals("2001-04-23 10:00:42", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format((Date) parameters[0]));
        assertEquals("10:00:42", new SimpleDateFormat("HH:mm:ss")
                .format((java.sql.Time) parameters[1]));
    }

    public void testDatePattern21() throws Exception {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2001-04-23 10:00:42");
        request.addParameter("d", "" + date.getTime());
        request.addParameter("t", "" + date.getTime());

        Object[] parameters = resolveMethodParameters("datePattern2");
        assertNotNull(parameters);

        assertEquals(Date.class, parameters[0].getClass());
        assertEquals(java.sql.Time.class, parameters[1].getClass());

        assertEquals("2001-04-23 10:00:42", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format((Date) parameters[0]));
        assertEquals("10:00:42", new SimpleDateFormat("HH:mm:ss")
                .format((java.sql.Time) parameters[1]));
    }

    public void testUserBean() throws Exception {
        request.addParameter("id", "12");
        request.addParameter("name", "rose");
        request.addParameter("age", "20");

        Object[] parameters = resolveMethodParameters("userBean");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);

        assertTrue("unexpected type " + parameters[0].getClass().getName(),
                parameters[0] instanceof User);
        User user = (User) parameters[0];
        assertEquals(Long.valueOf(12), user.getId());
        assertEquals("rose", user.getName());
        assertEquals(20, user.getAge());

    }

    public void testUserBean2() throws Exception {
        request.addParameter("ua.id", "12");
        request.addParameter("ua.name", "rose");
        request.addParameter("ua.age", "20");

        request.addParameter("ub.id", "13");

        Object[] parameters = resolveMethodParameters("userBean2");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertNotNull(parameters[1]);

        assertTrue("unexpected type " + parameters[0].getClass().getName(),
                parameters[0] instanceof User);
        User ua = (User) parameters[0];
        assertEquals(Long.valueOf(12), ua.getId());
        assertEquals("rose", ua.getName());
        assertEquals(20, ua.getAge());

        assertTrue("unexpected type " + parameters[1].getClass().getName(),
                parameters[1] instanceof User);
        User ub = (User) parameters[1];
        assertEquals(Long.valueOf(13), ub.getId());
        assertNull(ub.getName());
        assertEquals(0, ub.getAge());

    }

    public void testBindingResult() throws Exception {
        request.addParameter("id", "42");
        request.addParameter("name", "rose");
        request.addParameter("age", "20");

        Object[] parameters = resolveMethodParameters("bindingResult");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertNotNull(parameters[1]);

        assertTrue("unexpected type " + parameters[0].getClass().getName(),
                parameters[0] instanceof User);
        User ua = (User) parameters[0];
        assertEquals(Long.valueOf(42), ua.getId());
        assertEquals("rose", ua.getName());
        assertEquals(20, ua.getAge());

        assertTrue("unexpected type " + parameters[1].getClass().getName(),
                parameters[1] instanceof BindingResult);
    }

    public void testInt() throws Exception {
        Object[] parameters = resolveMethodParameters("innt");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(Integer.class, parameters[0].getClass());
        assertEquals(0, parameters[0]);
    }

    public void testInteger() throws Exception {
        Object[] parameters = resolveMethodParameters("integer");
        assertNotNull(parameters);
        assertNull(parameters[0]);
    }

    public void testBool() throws Exception {
        Object[] parameters = resolveMethodParameters("bool");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(Boolean.class, parameters[0].getClass());
        assertEquals(false, parameters[0]);
    }

    public void testBoool() throws Exception {
        Object[] parameters = resolveMethodParameters("boool");
        assertNotNull(parameters);
        assertNull(parameters[0]);
    }

    public void testLoong() throws Exception {
        Object[] parameters = resolveMethodParameters("loong");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(Long.class, parameters[0].getClass());
        assertEquals(0L, parameters[0]);
    }

    public void testLooong() throws Exception {
        Object[] parameters = resolveMethodParameters("looong");
        assertNotNull(parameters);
        assertNull(parameters[0]);
    }

    public void testString() throws Exception {
        Object[] parameters = resolveMethodParameters("string");
        assertNotNull(parameters);
        assertNull(parameters[0]);
    }

    public void testNullPrimitiveInt() throws Exception, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        Object[] parameters = resolveMethodParameters("nullPrimitiveInt");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(Integer.class, parameters[0].getClass());
        assertEquals(0, parameters[0]);
        Method method = findMethod("nullPrimitiveInt");
        method.invoke(new MockController(), parameters);
        assertTrue(true);
    }

    public void testNullPrimitiveBool() throws Exception, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        Object[] parameters = resolveMethodParameters("nullPrimitiveBool");
        assertNotNull(parameters);
        assertNotNull(parameters[0]);
        assertEquals(Boolean.class, parameters[0].getClass());
        assertEquals(false, parameters[0]);
        Method method = findMethod("nullPrimitiveBool");
        method.invoke(new MockController(), parameters);
        assertTrue(true);
    }

    public void testNullPrimitiveBoolWrapper() throws Exception, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        Object[] parameters = resolveMethodParameters("nullPrimitiveBoolWrapper");
        assertNotNull(parameters);
        assertNull(parameters[0]);
        Method method = findMethod("nullPrimitiveBoolWrapper");
        method.invoke(new MockController(), parameters);
        assertTrue(true);
    }

    public void testInf() throws Exception, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        request.addParameter("a", "2001");
        request.addParameter("controller.bool", "true");

        Object[] parameters = resolveMethodParameters("inf");
        assertNotNull(parameters);
        assertNull(parameters[0]);
        assertNotNull(parameters[1]);
        assertEquals(2001, parameters[1]);
        assertEquals(true, parameters[2]);
        Method method = findMethod("inf");
        method.invoke(new MockController(), parameters);
        assertTrue(true);
    }

    public void testInf2() throws Exception, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        request.addParameter("a", "20a01"); // type miss match 不要阻止下一个转化
        request.addParameter("controller.bool", "true");

        Object[] parameters = resolveMethodParameters("inf");
        assertNotNull(parameters);
        assertNull(parameters[0]);
        assertNotNull(parameters[1]);
        assertEquals(0, parameters[1]);
        assertEquals(true, parameters[2]);
        Method method = findMethod("inf");
        method.invoke(new MockController(), parameters);
        assertTrue(true);
    }

    protected Object[] resolveMethodParameters(String methodName) throws Exception {
        Method method = findMethod(methodName);
        assertNotNull("not found method named: " + methodName, method);

        //        ControllerEngine controllerEngine = new ControllerEngine(module, "/mock", new ControllerRef(
        //                new String[] { "/mock" }, "mock", new MockController(), MockController.class,
        //                ReqMethod.ALL.parse()));

        //        inv.setController(controllerEngine.getController());
        //
        //        inv.setMethod(method);

        ParameterNameDiscovererImpl parameterNameDiscoverer = new ParameterNameDiscovererImpl();
        ResolverFactoryImpl resolverFactory = new ResolverFactoryImpl();
        MethodParameterResolver resolver = new MethodParameterResolver(MockController.class,
                method, parameterNameDiscoverer, resolverFactory);
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
