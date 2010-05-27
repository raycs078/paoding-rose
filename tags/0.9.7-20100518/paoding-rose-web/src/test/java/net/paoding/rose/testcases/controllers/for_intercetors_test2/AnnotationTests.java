package net.paoding.rose.testcases.controllers.for_intercetors_test2;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.mock.controllers.for_interceptors_test2.AdvanceController;
import net.paoding.rose.mock.controllers.for_interceptors_test2.AdvanceDenyController;
import net.paoding.rose.mock.controllers.for_interceptors_test2.AdvanceInterceptor;
import net.paoding.rose.mock.controllers.for_interceptors_test2.SimpleController;
import net.paoding.rose.mock.controllers.for_interceptors_test2.SimpleDenyController;
import net.paoding.rose.mock.controllers.for_interceptors_test2.SimpleInterceptor;
import net.paoding.rose.testcases.AbstractControllerTest;

/**
 * 测试 Controller 上标注的 Annotation 是否按预期影响拦截器的执行。
 * 
 * @author han.liao
 * 
 */
public class AnnotationTests extends AbstractControllerTest {

    /**
     * inters2/simple: intercepted(none)
     * <p>
     * @throws ServletException
     * @throws IOException
     */
    public void testSimpleIndex() throws ServletException, IOException {
        assertEquals(SimpleController.RETURN + ".index", invoke("/inters2/simple"));
        assertNull(request.getAttribute(SimpleInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(AdvanceInterceptor.AFTER_COMPLETION));
    }

    /**
     * inters2/simple/method: intercepted(simple, advance)
     * <p>
     * @throws ServletException
     * @throws IOException
     */
    public void testSimpleMethod() throws ServletException, IOException {
        assertEquals(SimpleInterceptor.RETURN, invoke("/inters2/simple/method"));
        assertEquals(true, request.getAttribute(SimpleInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(AdvanceInterceptor.AFTER_COMPLETION));
    }

    /**
     * inters2/simpleDeny: intercepted(none)
     * <p>
     * @throws ServletException
     * @throws IOException
     */
    public void testSimpleDenyIndex() throws ServletException, IOException {
        assertEquals(SimpleDenyController.RETURN + ".index", invoke("/inters2/simpleDeny"));
        assertNull(request.getAttribute(SimpleInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(AdvanceInterceptor.AFTER_COMPLETION));
    }

    /**
     * inters2/simpleDeny/method: intercepted(simple, advance)
     * <p>
     * @throws ServletException
     * @throws IOException
     */
    public void testSimpleDenyMethod() throws ServletException, IOException {
        assertEquals(SimpleInterceptor.RETURN, invoke("/inters2/simpleDeny/method"));
        assertEquals(true, request.getAttribute(SimpleInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(AdvanceInterceptor.AFTER_COMPLETION));
    }

    /**
     * inters2/advance: intercepted(none)
     * <p>
     * @throws ServletException
     * @throws IOException
     */
    public void testAdvanceIndex() throws ServletException, IOException {
        assertEquals(AdvanceController.RETURN + ".index", invoke("/inters2/advance"));
        assertNull(request.getAttribute(SimpleInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(AdvanceInterceptor.AFTER_COMPLETION));
    }

    /**
     * inters2/advance/method: intercepted(advance)
     * <p>
     * @throws ServletException
     * @throws IOException
     */
    public void testAdvanceMethod() throws ServletException, IOException {
        assertEquals(AdvanceInterceptor.RETURN, invoke("/inters2/advance/method"));
        assertNull(request.getAttribute(SimpleInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(AdvanceInterceptor.AFTER_COMPLETION));
    }

    /**
     * inters2/advanceDeny: intercepted(none)
     * <p>
     * @throws ServletException
     * @throws IOException
     */
    public void testAdvanceDenyIndex() throws ServletException, IOException {
        assertEquals(AdvanceDenyController.RETURN + ".index", invoke("/inters2/advanceDeny"));
        assertNull(request.getAttribute(SimpleInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(AdvanceInterceptor.AFTER_COMPLETION));
    }

    /**
     * inters2/advanceDeny/method: intercepted(advance)
     * <p>
     * @throws ServletException
     * @throws IOException
     */
    public void testAdvanceDenyMethod() throws ServletException, IOException {
        assertEquals(AdvanceInterceptor.RETURN, invoke("/inters2/advanceDeny/method"));
        assertNull(request.getAttribute(SimpleInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(AdvanceInterceptor.AFTER_COMPLETION));
    }
}
