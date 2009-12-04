package net.paoding.rose.testcases;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import net.paoding.rose.RoseFilter;
import net.paoding.rose.testcases.controllers.RoseTestEnv;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.WebUtils;

public abstract class AbstractControllerTest extends TestCase {

    public static RoseFilter filter;

    protected ServletContext servletContext;

    protected MockHttpServletRequest request;

    protected MockHttpServletResponse response;

    protected MockFilterChain chain = new MockFilterChain();


    public void innerSetUp() throws Exception {
        
    }
    
    @Before
    public void setUp() throws Exception {
        System.out.println("====================setUp TEST=========== " + getClass().getName());
        filter = RoseTestEnv.instance().getRoseFilter();
        servletContext = filter.getFilterConfig().getServletContext();
        request = new MockHttpServletRequest(servletContext) {

            @Override
            public RequestDispatcher getRequestDispatcher(final String path) {
                return new RequestDispatcher() {

                    @Override
                    public void forward(ServletRequest request, ServletResponse response)
                            throws ServletException, IOException {
                        WebUtils.exposeForwardRequestAttributes((HttpServletRequest) request);
                        String uri = path;
                        if (path.indexOf('?') >= 0) {
                            uri = path.substring(0, path.indexOf('?'));
                            ((MockHttpServletRequest) request).setRequestURI(uri);
                            ((MockHttpServletRequest) request).setQueryString(path.substring(path
                                    .indexOf('?') + 1));
                        } else {
                            ((MockHttpServletRequest) request).setRequestURI(uri);
                            ((MockHttpServletRequest) request).setQueryString("");
                        }

                        AbstractControllerTest.filter.doFilter(request, response, chain);
                    }

                    @Override
                    public void include(ServletRequest request, ServletResponse response)
                            throws ServletException, IOException {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        response = new MockHttpServletResponse();
        request.setMethod("GET");
        innerSetUp();
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
        filter.doFilter(request, response, chain);
        return RoseTestEnv.instance().getInstructionExecutor().getInstruction(request);
    }
}
