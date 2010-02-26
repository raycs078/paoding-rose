package net.paoding.rose.testcases.web.impl.module;

import junit.framework.TestCase;
import net.paoding.rose.web.impl.mapping.MappingImpl;
import net.paoding.rose.web.impl.mapping.MatchMode;

public class MappingSortingTest extends TestCase {

    public void testSimpleEquals() {
        MappingImpl mapping1 = new MappingImpl("/user", MatchMode.STARTS_WITH);
        MappingImpl mapping2 = new MappingImpl("/user", MatchMode.STARTS_WITH);

        assertEquals(0, mapping1.compareTo(mapping1));
        assertEquals(0, mapping2.compareTo(mapping2));
        assertEquals(0, mapping1.compareTo(mapping2));
        assertEquals(0, mapping2.compareTo(mapping1));
    }

    public void testSimpleEquals2() {
        MappingImpl mapping1 = new MappingImpl("/user/", MatchMode.STARTS_WITH);
        MappingImpl mapping2 = new MappingImpl("/user", MatchMode.STARTS_WITH);

        assertEquals(0, mapping1.compareTo(mapping1));
        assertEquals(0, mapping2.compareTo(mapping2));
    }

    public void testSimpleEmpty() {
        MappingImpl mapping1 = new MappingImpl("", MatchMode.STARTS_WITH);
        MappingImpl mapping2 = new MappingImpl("/user", MatchMode.STARTS_WITH);

        assertEquals(0, mapping1.compareTo(mapping1));
        assertEquals(0, mapping2.compareTo(mapping2));
        assertEquals(1, mapping1.compareTo(mapping2));
        assertEquals(-1, mapping2.compareTo(mapping1));
    }

    public void testSimpleStartWith() {
        MappingImpl mapping1 = new MappingImpl("/u", MatchMode.STARTS_WITH);
        MappingImpl mapping2 = new MappingImpl("/user", MatchMode.STARTS_WITH);

        assertEquals(0, mapping1.compareTo(mapping1));
        assertEquals(0, mapping2.compareTo(mapping2));
        assertEquals(1, mapping1.compareTo(mapping2));
        assertEquals(-1, mapping2.compareTo(mapping1));
    }

    public void testSimpleNotEquals2() {
        MappingImpl mapping1 = new MappingImpl("/abc", MatchMode.STARTS_WITH);
        MappingImpl mapping2 = new MappingImpl("/user", MatchMode.STARTS_WITH);

        assertEquals(0, mapping1.compareTo(mapping1));
        assertEquals(0, mapping2.compareTo(mapping2));
        int value = mapping1.compareTo(mapping2);
        assertEquals(-value, mapping2.compareTo(mapping1));
    }

    public void testSimplePattern1() {
        MappingImpl mapping1 = new MappingImpl("/{id}", MatchMode.STARTS_WITH);
        MappingImpl mapping2 = new MappingImpl("/user", MatchMode.STARTS_WITH);

        assertEquals(0, mapping1.compareTo(mapping1));
        assertEquals(0, mapping2.compareTo(mapping2));
        assertEquals(1, mapping1.compareTo(mapping2));
        assertEquals(-1, mapping2.compareTo(mapping1));
    }

    public void testSimplePattern2() {
        MappingImpl mapping1 = new MappingImpl("/u{id}", MatchMode.STARTS_WITH);
        MappingImpl mapping2 = new MappingImpl("/user", MatchMode.STARTS_WITH);

        assertEquals(0, mapping1.compareTo(mapping1));
        assertEquals(0, mapping2.compareTo(mapping2));
        assertEquals(1, mapping1.compareTo(mapping2));
        assertEquals(-1, mapping2.compareTo(mapping1));
    }

    public void testPatternSimple3() {
        MappingImpl mapping1 = new MappingImpl("/user_{id}", MatchMode.STARTS_WITH);
        MappingImpl mapping2 = new MappingImpl("/user", MatchMode.STARTS_WITH);

        assertEquals(0, mapping1.compareTo(mapping1));
        assertEquals(0, mapping2.compareTo(mapping2));
        assertEquals(-1, mapping1.compareTo(mapping2));
        assertEquals(1, mapping2.compareTo(mapping1));
    }

    public void testPatternSimple4() {
        MappingImpl mapping1 = new MappingImpl("", MatchMode.STARTS_WITH);
        MappingImpl mapping2 = new MappingImpl("/user_{id}", MatchMode.STARTS_WITH);

        assertEquals(0, mapping1.compareTo(mapping1));
        assertEquals(0, mapping2.compareTo(mapping2));
        assertEquals(1, mapping1.compareTo(mapping2));
        assertEquals(-1, mapping2.compareTo(mapping1));
    }

    public void testPatternSimple5() {
        MappingImpl mapping1 = new MappingImpl("/user_", MatchMode.STARTS_WITH);
        MappingImpl mapping2 = new MappingImpl("/user_{id:*}", MatchMode.STARTS_WITH);

        assertEquals(0, mapping1.compareTo(mapping1));
        assertEquals(0, mapping2.compareTo(mapping2));
        assertEquals(1, mapping2.compareTo(mapping1));
        assertEquals(-1, mapping1.compareTo(mapping2));
    }

    public void testPattern1() {
        MappingImpl mapping1 = new MappingImpl("/user_{id}", MatchMode.STARTS_WITH);
        MappingImpl mapping2 = new MappingImpl("/user_{id}/", MatchMode.STARTS_WITH);

        assertEquals(0, mapping1.compareTo(mapping1));
        assertEquals(0, mapping2.compareTo(mapping2));
        assertEquals(0, mapping1.compareTo(mapping2));
        assertEquals(0, mapping2.compareTo(mapping1));
    }

    public void testPattern2() {
        MappingImpl mapping1 = new MappingImpl("/{id}", MatchMode.STARTS_WITH);
        MappingImpl mapping2 = new MappingImpl("/user_{id}", MatchMode.STARTS_WITH);

        assertEquals(0, mapping1.compareTo(mapping1));
        assertEquals(0, mapping2.compareTo(mapping2));
        assertEquals(1, mapping1.compareTo(mapping2));
        assertEquals(-1, mapping2.compareTo(mapping1));
    }

}
