package net.paoding.rose.testcases.web.impl.module;

import junit.framework.TestCase;
import net.paoding.rose.web.impl.mapping.MappingImpl;
import net.paoding.rose.web.impl.mapping.MatchMode;

public class SimpleMappingTest extends TestCase {

    public void testStartsWith() {
        MappingImpl mapping = new MappingImpl("/user", MatchMode.STARTS_WITH);
        // expected: not null
        assertNotNull(mapping.match("/user"));
        assertNotNull(mapping.match("/user"));
        assertNotNull(mapping.match("/user/"));
        assertNotNull(mapping.match("/user/"));
        assertNotNull(mapping.match("/user/1"));
        assertNotNull(mapping.match("/user/1"));

        // expected: null
        assertNull(mapping.match("/user_001"));
        assertNull(mapping.match("/topic"));
        assertNull(mapping.match(""));
    }

    public void testEquals() {
        MappingImpl mapping = new MappingImpl("/user", MatchMode.EQUALS);
        // expected: not null
        assertNotNull(mapping.match("/user"));
        assertNotNull(mapping.match("/user"));
        assertNotNull(mapping.match("/user/"));
        assertNotNull(mapping.match("/user/"));

        // expected: null
        assertNull(mapping.match("/user/1"));
        assertNull(mapping.match("/user/1"));
        assertNull(mapping.match("/user_001"));
        assertNull(mapping.match("/topic")); // /topic.len = /user.len + 1
        assertNull(mapping.match(""));
    }
}
