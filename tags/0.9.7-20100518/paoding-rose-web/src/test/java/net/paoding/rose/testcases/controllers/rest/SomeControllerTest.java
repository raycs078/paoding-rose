package net.paoding.rose.testcases.controllers.rest;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class SomeControllerTest extends AbstractControllerTest {

    public void testGet() throws ServletException, IOException {
        request.setMethod("GET");
        assertEquals("get", invoke("/rest/some"));
    }

    public void testPost() throws ServletException, IOException {
        request.setMethod("POST");
        assertEquals("post", invoke("/rest/some"));
    }

}
