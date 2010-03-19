package net.paoding.rose.testcases.controllers;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class IfControllerTest extends AbstractControllerTest {

    public void testNotIf() throws ServletException, IOException {
        assertEquals("a", invoke("/if"));
    }

    public void testIfb() throws ServletException, IOException {
        request.addParameter("b", "anyvalueforb");
        assertEquals("b", invoke("/if"));
    }

    public void testIfc() throws ServletException, IOException {
        request.addParameter("c", "anyvalueforc");
        assertEquals("c", invoke("/if"));
    }

    public void testIfc3() throws ServletException, IOException {
        request.addParameter("c", "3");
        assertEquals("c3", invoke("/if"));
    }

    public void testIfc2() throws ServletException, IOException {
        request.addParameter("c", "2");
        assertEquals("c2", invoke("/if"));
    }

    public void testSubDir() throws ServletException, IOException {
        assertEquals("d", invoke("/if/d"));
    }
}
