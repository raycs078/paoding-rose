package net.paoding.rose.testcases.app;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.paoding.rose.scanning.context.RoseAppContext;
import net.paoding.rose.testcases.controllers.autowire.AutowireBean;
import net.paoding.rose.testcases.controllers.autowire.AutowireBean2;

public class RoseAppContextTest extends TestCase {

    public void testRoseAppContext() {

        RoseAppContext rose = new RoseAppContext();

        Assert.assertNotNull(rose.getBean("autowireBean"));
        Assert.assertNotNull(rose.getBean("autowireBean2"));

        Assert.assertEquals(AutowireBean.class, rose.getBean("autowireBean").getClass());
        Assert.assertEquals(AutowireBean2.class, rose.getBean("autowireBean2").getClass());

        Assert.assertEquals(rose.getBean("autowireBean"), rose.getBean(AutowireBean.class));
        Assert.assertEquals(rose.getBean("autowireBean2"), rose.getBean(AutowireBean2.class));
    }
}
