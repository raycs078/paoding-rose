package net.paoding.rose.testcases.web.impl.module;

import java.util.List;

import junit.framework.TestCase;
import net.paoding.rose.web.impl.mapping.ConstantMapping;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.impl.mapping.MappingFactory;
import net.paoding.rose.web.impl.mapping.RegexMapping;

public class MappingTest extends TestCase {

    public void testEmpty() {
        List<Mapping> mappings = MappingFactory.parse("");
        assertEquals(0, mappings.size());
    }

    public void testUser() {
        List<Mapping> mappings = MappingFactory.parse("/user");
        assertEquals("/user", mappings.get(0).getDefinition());
        assertEquals(1, mappings.size());
    }

    public void testRegex() {
        List<Mapping> mappings = MappingFactory.parse("/{user}");
        assertEquals(ConstantMapping.class, mappings.get(0).getClass());
        assertEquals("/", mappings.get(0).getDefinition());

        assertEquals(RegexMapping.class, mappings.get(1).getClass());
        assertEquals("{user}", mappings.get(1).getDefinition());
        assertEquals(2, mappings.size());
    }

    public void testRegex2() {
        List<Mapping> mappings = MappingFactory.parse("/${user}");
        assertEquals(ConstantMapping.class, mappings.get(0).getClass());
        assertEquals("/", mappings.get(0).getDefinition());

        assertEquals(RegexMapping.class, mappings.get(1).getClass());
        assertEquals("{user}", mappings.get(1).getDefinition());
        assertEquals(2, mappings.size());
    }

    public void testRegex3() {
        List<Mapping> mappings = MappingFactory.parse("/abc/${user:[0-9]+}");
        assertEquals(ConstantMapping.class, mappings.get(0).getClass());
        assertEquals("/abc", mappings.get(0).getDefinition());

        assertEquals(ConstantMapping.class, mappings.get(1).getClass());
        assertEquals("/", mappings.get(1).getDefinition());

        assertEquals(RegexMapping.class, mappings.get(2).getClass());
        assertEquals("{user}", mappings.get(2).getDefinition());
        assertEquals(3, mappings.size());
    }

    public void testRegex4() {
        List<Mapping> mappings = MappingFactory.parse("/abc/xyz${user}");
        assertEquals(ConstantMapping.class, mappings.get(0).getClass());
        assertEquals("/abc", mappings.get(0).getDefinition());

        assertEquals(ConstantMapping.class, mappings.get(1).getClass());
        assertEquals("/xyz", mappings.get(1).getDefinition());

        assertEquals(RegexMapping.class, mappings.get(2).getClass());
        assertEquals("{user}", mappings.get(2).getDefinition());
        assertEquals(3, mappings.size());
    }

    public void testRegex5() {
        List<Mapping> mappings = MappingFactory.parse("/abc/xyz{user}ijk");
        assertEquals(ConstantMapping.class, mappings.get(0).getClass());
        assertEquals("/abc", mappings.get(0).getDefinition());

        assertEquals(ConstantMapping.class, mappings.get(1).getClass());
        assertEquals("/xyz", mappings.get(1).getDefinition());

        assertEquals(RegexMapping.class, mappings.get(2).getClass());
        assertEquals("{user}", mappings.get(2).getDefinition());

        assertEquals(ConstantMapping.class, mappings.get(3).getClass());
        assertEquals("ijk", mappings.get(3).getDefinition());
        assertEquals(4, mappings.size());
    }

    public void testRegex6() {
        List<Mapping> mappings = MappingFactory.parse("/$controller.id/$controller.bool");
        assertEquals(ConstantMapping.class, mappings.get(0).getClass());
        assertEquals("/", mappings.get(0).getDefinition());

        assertEquals(RegexMapping.class, mappings.get(1).getClass());
        assertEquals("{controller.id}", mappings.get(1).getDefinition());

        assertEquals(ConstantMapping.class, mappings.get(2).getClass());
        assertEquals("/", mappings.get(2).getDefinition());

        assertEquals(RegexMapping.class, mappings.get(3).getClass());
        assertEquals("{controller.bool}", mappings.get(3).getDefinition());

        assertEquals(4, mappings.size());
    }
}
