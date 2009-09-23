package net.paoding.rose.testcases.web.impl.module;

import junit.framework.TestCase;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.mapping.MappingImpl;
import net.paoding.rose.web.impl.mapping.MatchMode;

public class SimpleMappingTest extends TestCase {

	public void testStartsWith() {
		MappingImpl<?> mapping = new MappingImpl<Object>("/user",
				MatchMode.PATH_STARTS_WITH,
				new ReqMethod[] { ReqMethod.ALL }, new Object());
		// expected: not null
		assertNotNull(mapping.match("/user", "GET"));
		assertNotNull(mapping.match("/user", "POST"));
		assertNotNull(mapping.match("/user/", "GET"));
		assertNotNull(mapping.match("/user/", "POST"));
		assertNotNull(mapping.match("/user/1", "GET"));
		assertNotNull(mapping.match("/user/1", "POST"));

		// expected: null
		assertNull(mapping.match("/user_001", "GET"));
		assertNull(mapping.match("/topic", "GET"));
		assertNull(mapping.match("", "GET"));
	}

	public void testEquals() {
		MappingImpl<?> mapping = new MappingImpl<Object>("/user",
				MatchMode.PATH_EQUALS, new ReqMethod[] { ReqMethod.ALL },
				new Object());
		// expected: not null
		assertNotNull(mapping.match("/user", "GET"));
		assertNotNull(mapping.match("/user", "POST"));
		assertNotNull(mapping.match("/user/", "GET"));
		assertNotNull(mapping.match("/user/", "POST"));

		// expected: null
		assertNull(mapping.match("/user/1", "GET"));
		assertNull(mapping.match("/user/1", "POST"));
		assertNull(mapping.match("/user_001", "GET"));
		assertNull(mapping.match("/topic", "GET")); // /topic.len = /user.len + 1
		assertNull(mapping.match("", "GET"));
	}
}
