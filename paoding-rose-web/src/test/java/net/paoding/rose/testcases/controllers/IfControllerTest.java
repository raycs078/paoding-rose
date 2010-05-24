package net.paoding.rose.testcases.controllers;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class IfControllerTest extends AbstractControllerTest {

	public void testIfab1() throws ServletException, IOException {
    	request.addParameter("a", "a");
    	request.addParameter("b", "b");
    	assertEquals("ab1", invoke("/if"));
    }
	
	public void testIfab2() throws ServletException, IOException {
    	request.addParameter("a", "111");
    	request.addParameter("b", "222");
    	assertEquals("ab2", invoke("/if"));
    }
	
	public void testIfab3() throws ServletException, IOException {
    	request.addParameter("a", "fdsaffas");
    	request.addParameter("b", "fasdfas");
    	assertEquals("ab3", invoke("/if"));
    }
	
	public void testIfNotAb1() throws ServletException, IOException {
    	request.addParameter("a", "a");
    	request.addParameter("b", "bfsdfsd");
    	System.out.println(invoke("/if"));
    	assertEquals("ab3", invoke("/if"));
    }
	
	public void testIfabc1() throws ServletException, IOException {
    	request.addParameter("a", "a");
    	request.addParameter("b", "b");
    	request.addParameter("c", "c");
    	assertEquals("abc1", invoke("/if"));
    }
	
	public void testIfNotAbc1() throws ServletException, IOException {
    	request.addParameter("a", "a");
    	request.addParameter("b", "b");
    	request.addParameter("c", "fasdfsad");
    	assertEquals("ab1", invoke("/if"));
    }
	
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
