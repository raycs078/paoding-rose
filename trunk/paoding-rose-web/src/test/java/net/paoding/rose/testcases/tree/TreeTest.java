package net.paoding.rose.testcases.tree;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class TreeTest extends AbstractControllerTest {

    public void testGet() throws ServletException, IOException {
        request.setMethod("GET");
        assertEquals(7, invoke("/tree/count"));
    }
    
    public void testGet2() throws ServletException, IOException {
        request.setMethod("GET");
        System.out.println(invoke("/rose-info/tree"));
    }

}
