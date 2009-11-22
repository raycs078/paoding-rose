package net.paoding.rose.testcases.tree;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class TreeTest extends AbstractControllerTest {

    public void testGet() throws ServletException, IOException {
        request.setMethod("GET");
        assertEquals(5, invoke("/tree/count"));
    }

}
