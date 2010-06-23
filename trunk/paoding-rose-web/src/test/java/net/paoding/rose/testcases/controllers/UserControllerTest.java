package net.paoding.rose.testcases.controllers;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class UserControllerTest extends AbstractControllerTest {

    public void testAlias() throws ServletException, IOException {
        assertEquals("123", invoke("/user/alias123"));
    }

    public void testAlias1() throws ServletException, IOException {
        assertEquals("123", invoke("/user/alias123/"));
    }

    public void testAlias2() throws ServletException, IOException {
        assertEquals("xyz", invoke("/user/aliasxyz"));
    }

    public void testAlias3() throws ServletException, IOException {
        assertEquals("xyz", invoke("/user/aliasxyz/"));
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

    /*- 此测试用例本意是测试  @Get("{id}/")和 @Get("{id}")的不同的，但现在认为这2个为完全等价
       现在改用启动testShow3()
      public void testShow2() throws ServletException, IOException {
        assertEquals(1234567 * 2, invoke("/user/1234567/"));
    }*/

    public void testShow3() throws ServletException, IOException {
        assertEquals(1234567, invoke("/user/1234567/"));
    }

    public void testShow404() throws ServletException, IOException {
        assertNull(invoke("/user/1234567/404"));
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
