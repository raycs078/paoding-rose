package net.paoding.rose.testcases.controllers;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class OrderControllerTest extends AbstractControllerTest {

	public void testList() throws ServletException, IOException {
		assertEquals("list/1234567", invoke("/1234567/order/list"));
	}

	public void testShow() throws ServletException, IOException {
		assertEquals("show/1234567/7654321", invoke("/1234567/order/7654321"));
	}

	public void testEmptyPath() throws ServletException, IOException {
		assertEquals("def", invoke("/1234567/order"));
	}

	public void testEmptyPath2() throws ServletException, IOException {
		assertEquals("def", invoke("/1234567/order/"));
	}

}
