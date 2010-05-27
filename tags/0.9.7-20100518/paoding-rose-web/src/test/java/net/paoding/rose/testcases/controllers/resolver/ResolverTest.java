package net.paoding.rose.testcases.controllers.resolver;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.mock.controllers.resolver.SomeResolver;
import net.paoding.rose.mock.resolvers.BeanResolver;
import net.paoding.rose.mock.resolvers.InterfaceResolver;
import net.paoding.rose.testcases.AbstractControllerTest;

import org.springframework.context.ApplicationContext;

public class ResolverTest extends AbstractControllerTest {

    public void testIndex() throws ServletException, IOException {
        assertEquals(SomeResolver.DEFAULT_PHONE_ID, invoke("/resolver/main"));
    }

    public void testIntfx() throws ServletException, IOException {
        assertEquals(InterfaceResolver.GET_VALUE, invoke("/resolver/main/intf"));
    }

    public void testCtx() throws ServletException, IOException {
        Object ret = invoke("/resolver/main/ctx");
        assertTrue(ret instanceof ApplicationContext);
        ApplicationContext ctx = (ApplicationContext) ret;
        assertNotNull(ctx.getBean("applicationContextResolver"));
    }

    public void testSubCtx() throws ServletException, IOException {
        Object ret = invoke("/resolver/sub/main/ctx");
        assertTrue(ret instanceof ApplicationContext);
        ApplicationContext ctx = (ApplicationContext) ret;
        assertNotNull(ctx.getBean("applicationContextResolver"));
    }

    public void testChild() throws ServletException, IOException {
        assertEquals(InterfaceResolver.GET_VALUE, invoke("/resolver/child/intf"));
    }

    public void testBeanResolver() throws ServletException, IOException {
        assertEquals(BeanResolver.GET_VALUE, invoke("/resolver/main/bean"));
    }

    public void testBeanExResolver() throws ServletException, IOException {
        assertNull(invoke("/resolver/main/beanex"));
    }

    public void testSuperControllerResolver() throws ServletException, IOException {
        assertEquals("ok", invoke("/resolver/sub/resolver"));
    }
}
