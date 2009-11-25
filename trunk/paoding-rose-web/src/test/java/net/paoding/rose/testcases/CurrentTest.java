package net.paoding.rose.testcases;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.web.InvocationUtils;

public class CurrentTest extends AbstractControllerTest {

    public void test() throws ServletException, IOException {
        request.addParameter("testThread", Thread.currentThread().getName());
        assertEquals("ok", invoke("/current"));
        assertNull(InvocationUtils.getCurrentThreadRequest());
        assertNull(InvocationUtils.getCurrentThreadInvocation());
    }

}
