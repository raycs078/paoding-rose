package net.paoding.rose.testcases.controllers;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class UserControllerTest extends AbstractControllerTest {

    public void testAlias() throws ServletException, IOException {
        assertEquals("123", invoke("/user/alias123"));
    }

    public void testAlias2() throws ServletException, IOException {
        assertEquals("xyz", invoke("/user/aliasxyz"));
    }

    public void testGetREST() throws ServletException, IOException {
        assertEquals("index", invoke("/user"));
    }

    public void testPostREST() throws ServletException, IOException {
        assertEquals("POST", invoke("/user", "POST", ""));
    }

    public void testIndex() throws ServletException, IOException {
        assertEquals("index", invoke("/user/index"));
    }

    public void testShow() throws ServletException, IOException {
        assertEquals(1234567, invoke("/user/1234567"));
    }

    public void testAccount() throws ServletException, IOException {
        assertEquals("account_1234567", invoke("/user/1234567/account"));
    }

    public void testQueryString() throws ServletException, IOException {
        request.addParameter("id", "4");
        assertEquals(4, invoke("/user/queryString"));
    }

    public void testInf() throws ServletException, IOException {
        request.addParameter("id", "5");
        assertEquals(5, invoke("/user/inf"));
    }

}
