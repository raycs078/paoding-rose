package net.paoding.rose.testcases.controllers.nodef;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class NoDefControllerTest extends AbstractControllerTest {

    public void testNotFoundController() throws ServletException, IOException {
        assertEquals(null, invoke("/nodef"));
    }

    public void testNotFoundController2() throws ServletException, IOException {
        assertEquals(null, invoke("/nodef/abc"));
    }
}
