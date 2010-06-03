package net.paoding.rose.testcases.controllers.errorhandler;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.mock.controllers.errorhandler.MainController;
import net.paoding.rose.testcases.AbstractControllerTest;

/**
 * @see MainController
 * @author 王志亮 [qieqie.wang@gmail.com]
 *
 */
public class ErrorHanlderTest extends AbstractControllerTest {

    public void testIndex() throws ServletException, IOException {
        Object obj = invoke("/errorhandler/main");
        assertTrue(obj.getClass() == IllegalArgumentException.class);
        assertEquals("main", ((IllegalArgumentException) obj).getMessage());
    }

    public void testSubIndex() throws ServletException, IOException {
        Object obj = invoke("/errorhandler/sub/main");
        assertTrue(obj.getClass() == IllegalArgumentException.class);
        assertEquals("main-sub", ((IllegalArgumentException) obj).getMessage());
    }

}
