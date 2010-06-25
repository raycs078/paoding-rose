package net.paoding.rose.testcases.controllers.mapping;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.mock.controllers.mapping.ConstantController;
import net.paoding.rose.testcases.AbstractControllerTest;

/**
 * @see ConstantController
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ConstantControllerTest extends AbstractControllerTest {

    public void test1() throws ServletException, IOException {
        assertEquals("xx:mn/show", invoke("/mapping/constant/mn/show"));
    }

    /**
     * 曾经出现/mn2被导入到/mn/show的路径中，使得404
     * 
     * @throws ServletException
     * @throws IOException
     */
    public void test2() throws ServletException, IOException {
        assertEquals("mn2", invoke("/mapping/constant/mn2"));
    }

}
