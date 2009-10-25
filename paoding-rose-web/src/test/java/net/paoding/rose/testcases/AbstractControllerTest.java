package net.paoding.rose.testcases;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import junit.framework.TestCase;

import net.paoding.rose.RoseFilter;
import net.paoding.rose.testcases.controllers.RoseTestEnv;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public abstract class AbstractControllerTest extends TestCase {

    protected RoseFilter filter;

    protected ServletContext servletContext;

    protected MockHttpServletRequest request;

    protected MockHttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        filter = RoseTestEnv.instance().getRoseFilter();
        servletContext = filter.getFilterConfig().getServletContext();
        request = new MockHttpServletRequest(servletContext);
        response = new MockHttpServletResponse();
        request.setMethod("GET");
    }

    protected Object invoke(String uri) throws ServletException, IOException {
        return invoke(uri, "", "");
    }

    protected Object invoke(String uri, String method, String queryString) throws ServletException,
            IOException {
        request.setRequestURI(uri);
        if (StringUtils.isNotEmpty(method)) {
            request.setMethod(method);
        }
        if (StringUtils.isNotEmpty(queryString)) {
            request.setQueryString(queryString);
        }
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        return RoseTestEnv.instance().getInstructionExecutor().getInstruction(request);
    }
}
