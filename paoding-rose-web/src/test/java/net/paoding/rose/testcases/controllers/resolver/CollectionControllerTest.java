package net.paoding.rose.testcases.controllers.resolver;

import java.io.IOException;

import net.paoding.rose.testcases.AbstractControllerTest;

public class CollectionControllerTest extends AbstractControllerTest {

    public void testList() throws Exception, IOException {
        request.addParameter("int", "1");
        request.addParameter("int", "2");
        request.addParameter("int", "3");

        request.addParameter("integer", "4, 5, 6");
        request.addParameter("name", "wang");
        request.addParameter("name", "zhi");

        request.addParameter("bool", "true");
        request.addParameter("bool", "false");

        @SuppressWarnings("unchecked")
        Class[] classes = (Class[]) invoke("/resolver/collection/list");
        int i = 0;
        assertTrue(Integer.class.isAssignableFrom(classes[i++]));
        assertTrue(Integer.class.isAssignableFrom(classes[i++]));
        assertTrue(String.class.isAssignableFrom(classes[i++]));
        assertTrue(Boolean.class.isAssignableFrom(classes[i++]));
    }

    public void testSet() throws Exception, IOException {
        request.addParameter("int", "1");
        request.addParameter("int", "2");
        request.addParameter("int", "3");

        request.addParameter("integer", "4, 5, 6");
        request.addParameter("name", "wang");
        request.addParameter("name", "zhi");

        request.addParameter("bool", "true");
        request.addParameter("bool", "false");

        @SuppressWarnings("unchecked")
        Class[] classes = (Class[]) invoke("/resolver/collection/set");
        int i = 0;
        assertTrue(Integer.class.isAssignableFrom(classes[i++]));
        assertTrue(Integer.class.isAssignableFrom(classes[i++]));
        assertTrue(String.class.isAssignableFrom(classes[i++]));
        assertTrue(Boolean.class.isAssignableFrom(classes[i++]));
    }

    public void testMap() throws Exception, IOException {
        request.addParameter("ss:ss1", "1");
        request.addParameter("ss:ss2", "2");
        request.addParameter("ss:ss3", "3");

        request.addParameter("is:1", "11");
        request.addParameter("is:2", "12");
        request.addParameter("is:3", "13");

        request.addParameter("sf:ss1", "1.02");
        request.addParameter("sf:ss2", "2.12");
        request.addParameter("sf:ss3", "13");

        request.addParameter("ib:1", "1");
        request.addParameter("ib:2", "true");
        request.addParameter("ib:3", "false");

        @SuppressWarnings("unchecked")
        Class[] classes = (Class[]) invoke("/resolver/collection/map");
        int i = 0;
        assertTrue(String.class.isAssignableFrom(classes[i++]));
        assertTrue(String.class.isAssignableFrom(classes[i++]));
        //
        assertTrue(Integer.class.isAssignableFrom(classes[i++]));
        //        System.out.println("========" + classes[i]);
        assertTrue(String.class.isAssignableFrom(classes[i++]));
        //
        assertTrue(String.class.isAssignableFrom(classes[i++]));
        //        System.out.println("========" + classes[i]);
        assertTrue(Float.class.isAssignableFrom(classes[i++]));
        //
        assertTrue(Integer.class.isAssignableFrom(classes[i++]));
        assertTrue(Boolean.class.isAssignableFrom(classes[i++]));
    }
}
