package net.paoding.rose.testcases.controllers.validators;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class DefControllerTest extends AbstractControllerTest {

    public void testIndex() throws ServletException, IOException {
        request.addParameter("p", "not-int");
        assertEquals("error", invoke("/validators/hello"));
    }

    public void testIndex2() throws ServletException, IOException {
        request.addParameter("p", "34");
        assertEquals(34, invoke("/validators/hello"));
    }

}
