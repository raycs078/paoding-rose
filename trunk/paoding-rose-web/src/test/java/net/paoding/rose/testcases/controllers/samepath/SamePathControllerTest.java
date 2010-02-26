package net.paoding.rose.testcases.controllers.samepath;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class SamePathControllerTest extends AbstractControllerTest {

    public void testSA() throws ServletException, IOException {
        assertEquals("a", invoke("/samepath/s/ab"));
    }

    public void testSB() throws ServletException, IOException {
        assertEquals("b", invoke("/samepath/s/abb"));
    }

    public void testSC() throws ServletException, IOException {
        assertEquals("c", invoke("/samepath/s/ac"));
    }

    public void testSD() throws ServletException, IOException {
        assertEquals("d", invoke("/samepath/s/ddd"));
    }

    //------------------------------------------------

    public void testXA() throws ServletException, IOException {
        assertEquals("a", invoke("/samepath/x/ab"));
    }

    public void testXB() throws ServletException, IOException {
        assertEquals("b", invoke("/samepath/x/abb"));
    }

    public void testXC() throws ServletException, IOException {
        assertEquals("c", invoke("/samepath/x/ac"));
    }

    public void testXD() throws ServletException, IOException {
        assertEquals("d", invoke("/samepath/x/ddd"));
    }

}
