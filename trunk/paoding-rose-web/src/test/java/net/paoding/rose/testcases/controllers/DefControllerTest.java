package net.paoding.rose.testcases.controllers;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.mock.controllers.DefController;
import net.paoding.rose.testcases.AbstractControllerTest;

/**
 * @see DefController
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class DefControllerTest extends AbstractControllerTest {

    public void testIndex() throws ServletException, IOException {
        assertEquals("index", invoke(""));
    }

    public void testIndex2() throws ServletException, IOException {
        assertEquals("index", invoke("/"));
    }

    /*测试{}.html的不需要{}/.html*/
    public void testTiny() throws ServletException, IOException {
        assertEquals("yizuoyiwang", invoke("/def/t/yizuoyiwang.html"));
    }

    public void testTiny2() throws ServletException, IOException {
        assertEquals("yizuoyiwang", invoke("/t/yizuoyiwang.html"));
    }

    public void testMethod() throws ServletException, IOException {
        assertEquals("method", invoke("/method"));
    }

    public void testParam() throws ServletException, IOException {
        assertEquals("param_1234567", invoke("/param_1234567"));
    }
}
