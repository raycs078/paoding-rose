package net.paoding.rose.testcases.controllers.for_intercetors_test;

import java.io.IOException;

import javax.servlet.ServletException;

import net.paoding.rose.mock.controllers.for_interceptors_test.AdvancedInterceptor;
import net.paoding.rose.mock.controllers.for_interceptors_test.BlockInterceptor;
import net.paoding.rose.mock.controllers.for_interceptors_test.HackInterceptor;
import net.paoding.rose.testcases.AbstractControllerTest;

/**
 * 测试拦截器的中断、return、afterCompletion是否按照预期正确执行
 * 
 * @author zhiliang.wang
 *
 */
public class InterceptorTest extends AbstractControllerTest {

    /**
     * intercepted(advanced, block, hack)
     * <p>
     * advanced.before return true;<br>
     * block.before return BlockInterceptor.RETURN<br>
     * block.after return the input instruction<br>
     * advanced.after return yyy=AdvancedInterceptor.RETURN<br>
     * 
     * @throws ServletException
     * @throws IOException
     */
    public void testAdvancedIndex() throws ServletException, IOException {
        assertEquals(AdvancedInterceptor.RETURN, invoke("/inters/advanced"));
        assertEquals(true, request.getAttribute(AdvancedInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(BlockInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(HackInterceptor.AFTER_COMPLETION));
    }

    public void testAdvancedIndex2() throws ServletException, IOException {
        assertEquals(AdvancedInterceptor.RETURN, invoke("/inters/advanced/index"));
        assertEquals(true, request.getAttribute(AdvancedInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(BlockInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(HackInterceptor.AFTER_COMPLETION));
    }

    /**
     * intercepted(block, hack)
     * <p>
     * block.before return BlockInterceptor.RETURN<br>
     * block.after return the input instruction<br>
     * 
     * @throws ServletException
     * @throws IOException
     */
    public void testBlockIndex() throws ServletException, IOException {
        assertEquals(BlockInterceptor.RETURN, invoke("/inters/block"));
        assertNull(request.getAttribute(AdvancedInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(BlockInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(HackInterceptor.AFTER_COMPLETION));
    }

    public void testBlockIndex2() throws ServletException, IOException {
        assertEquals(BlockInterceptor.RETURN, invoke("/inters/block/index"));
        assertNull(request.getAttribute(AdvancedInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(BlockInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(HackInterceptor.AFTER_COMPLETION));
    }

    /**
     * intercepted(hack)
     * <p>
     * block.before return true<br>
     * block.after return BlockInterceptor.RETURN<br>
     * 
     * @throws ServletException
     * @throws IOException
     */
    public void testHackIndex() throws ServletException, IOException {
        assertEquals(HackInterceptor.RETURN, invoke("/inters/hack"));
        assertNull(request.getAttribute(AdvancedInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(BlockInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(HackInterceptor.AFTER_COMPLETION));
    }

    public void testHackIndex2() throws ServletException, IOException {
        assertEquals(HackInterceptor.RETURN, invoke("/inters/hack/index"));
    }
    
    
    //---------------------------------------------

    /**
     * intercepted(advanced, block, hack)
     * <p>
     * advanced.before return true;<br>
     * block.before return BlockInterceptor.RETURN<br>
     * block.after return the input instruction<br>
     * advanced.after return yyy=AdvancedInterceptor.RETURN<br>
     * 
     * @throws ServletException
     * @throws IOException
     */
    public void testSubAdvancedIndex() throws ServletException, IOException {
        assertEquals(AdvancedInterceptor.RETURN, invoke("/inters/sub/advanced"));
        assertEquals(true, request.getAttribute(AdvancedInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(BlockInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(HackInterceptor.AFTER_COMPLETION));
    }

    public void testSubAdvancedIndex2() throws ServletException, IOException {
        assertEquals(AdvancedInterceptor.RETURN, invoke("/inters/sub/advanced/index"));
        assertEquals(true, request.getAttribute(AdvancedInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(BlockInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(HackInterceptor.AFTER_COMPLETION));
    }

    /**
     * intercepted(block, hack)
     * <p>
     * block.before return BlockInterceptor.RETURN<br>
     * block.after return the input instruction<br>
     * 
     * @throws ServletException
     * @throws IOException
     */
    public void testSubBlockIndex() throws ServletException, IOException {
        assertEquals(BlockInterceptor.RETURN, invoke("/inters/sub/block"));
        assertNull(request.getAttribute(AdvancedInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(BlockInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(HackInterceptor.AFTER_COMPLETION));
    }

    public void testSubBlockIndex2() throws ServletException, IOException {
        assertEquals(BlockInterceptor.RETURN, invoke("/inters/sub/block/index"));
        assertNull(request.getAttribute(AdvancedInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(BlockInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(HackInterceptor.AFTER_COMPLETION));
    }

    /**
     * intercepted(hack)
     * <p>
     * block.before return true<br>
     * block.after return BlockInterceptor.RETURN<br>
     * 
     * @throws ServletException
     * @throws IOException
     */
    public void testSubHackIndex() throws ServletException, IOException {
        assertEquals(HackInterceptor.RETURN, invoke("/inters/sub/hack"));
        assertNull(request.getAttribute(AdvancedInterceptor.AFTER_COMPLETION));
        assertNull(request.getAttribute(BlockInterceptor.AFTER_COMPLETION));
        assertEquals(true, request.getAttribute(HackInterceptor.AFTER_COMPLETION));
    }

    public void testSubHackIndex2() throws ServletException, IOException {
        assertEquals(HackInterceptor.RETURN, invoke("/inters/sub/hack/index"));
    }

}
