package net.paoding.rose.testcases.controllers.intercetorsAnnotatedBySuper;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.mock.controllers.intercetorsAnnotatedBySuper.HeadInterceptor;
import net.paoding.rose.mock.controllers.intercetorsAnnotatedBySuper.TailInterceptor;
import net.paoding.rose.mock.controllers.intercetorsAnnotatedBySuper.TailPostInterceptor;
import net.paoding.rose.testcases.AbstractControllerTest;

public class SuperInterceptorTest extends AbstractControllerTest {

	// 只allow tail，所以预期返回的是TailInterceptor的after
	public void testAllowTail() throws ServletException, IOException {
		assertEquals(TailInterceptor.RETURN, invoke("/inters-super/allow"));
	}

	// 只deny tail，即允许了head和tailPost，head优先于tailPost，
	// 所以预期返回的是HeadInterceptor的after即tailPost的return+head的return
	public void testDenyTail() throws ServletException, IOException {
		assertEquals(TailPostInterceptor.RETURN + "." + HeadInterceptor.RETURN,
				invoke("/inters-super/deny"));
	}
}
