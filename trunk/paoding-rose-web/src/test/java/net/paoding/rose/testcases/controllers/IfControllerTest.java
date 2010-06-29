package net.paoding.rose.testcases.controllers;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class IfControllerTest extends AbstractControllerTest {

	public void tetIfNotAb1() throws ServletException, IOException {
		request.addParameter("a", "a");
		request.addParameter("b", "b");
		assertEquals("a", invoke("/if")); 
	}
	
	public void testIfab1() throws ServletException, IOException {
    	request.setQueryString("a=a&b=b");
    	assertEquals("ab1", invoke("/if"));
    }
	
	public void testIfab1_2() throws ServletException, IOException {
    	request.setQueryString("a=haha&b=haha&a=a&b=b&b=");
    	assertEquals("ab1", invoke("/if"));
    }
	
	public void testIfab2() throws ServletException, IOException {
    	request.setQueryString("a=111&b=222");
    	assertEquals("ab2", invoke("/if"));
    }
	
	public void testIfab3() throws ServletException, IOException {
    	request.setQueryString("a=fdsaffas&b=fdsaffas");
    	assertEquals("ab3", invoke("/if"));
    }
	
	public void testIfab3_2() throws ServletException, IOException {
    	request.setQueryString("a=&b=");
    	assertEquals("ab3", invoke("/if"));
    }
	
	public void testIfab3_3() throws ServletException, IOException {
    	request.setQueryString("a&b");
    	assertEquals("ab3", invoke("/if"));
    }
	
	public void testIfNotAb1() throws ServletException, IOException {
    	request.setQueryString("a=a&b=bfsdfsd");
    	assertEquals("ab3", invoke("/if"));
    }
	
	public void testIfabc1() throws ServletException, IOException {
    	request.setQueryString("a=a&b=b&c=c");
    	assertEquals("abc1", invoke("/if"));
    }
	
	public void testIfNotAbc1() throws ServletException, IOException {
    	request.setQueryString("a=a&b=b&c=fasdfsad");
    	assertEquals("ab1", invoke("/if"));
    }
	
    public void testNotIf() throws ServletException, IOException {
        assertEquals("a", invoke("/if"));
    }

    public void testIfb() throws ServletException, IOException {
        request.setQueryString("b=anyvalueforb");
        assertEquals("b", invoke("/if"));
    }

    public void testIfc() throws ServletException, IOException {
        request.setQueryString("c=anyvalueforc");
        assertEquals("c", invoke("/if"));
    }
    
    public void testIfc_2() throws ServletException, IOException {
        request.setQueryString("c=");
        assertEquals("c", invoke("/if"));
    }
    
    public void testIfc_3() throws ServletException, IOException {
        request.setQueryString("c");
        assertEquals("c", invoke("/if"));
    }

    public void testIfc3() throws ServletException, IOException {
        request.setQueryString("c=3");
        assertEquals("c3", invoke("/if"));
    }

    public void testIfc2() throws ServletException, IOException {
        request.setQueryString("c=2");
        assertEquals("c2", invoke("/if"));
    }

    public void testSubDir() throws ServletException, IOException {
        assertEquals("d", invoke("/if/d"));
    }
}
