package net.paoding.rose.testcases;

import java.io.IOException;

import javax.servlet.ServletException;


public class CurrentTest extends AbstractControllerTest {

    public void test() throws ServletException, IOException {
        assertEquals("ok", invoke("/current"));
    }

}
