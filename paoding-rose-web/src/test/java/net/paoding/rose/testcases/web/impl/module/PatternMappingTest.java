package net.paoding.rose.testcases.web.impl.module;

import java.util.Arrays;

import junit.framework.TestCase;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.mapping.MatchMode;
import net.paoding.rose.web.impl.mapping.MappingImpl;

public class PatternMappingTest extends TestCase {

    public void testStartsWith11() {
        this._testStartsWith1("/{name}");
    }

    public void testStartsWith12() {
        this._testStartsWith1("/${name}");
    }

    public void testStartsWith13() {
        this._testStartsWith1("/$name");
    }

    private void _testStartsWith1(String pattern) {
        Object target = new Object();
        MappingImpl<?> mapping = new MappingImpl<Object>(pattern, MatchMode.PATH_STARTS_WITH,
                new ReqMethod[] { ReqMethod.ALL }, target);
        // expected: not null
        assertNotNull(mapping.match("/user", "GET"));
        assertSame(target, mapping.match("/user", "GET").getMapping().getTarget());
        assertNotNull(mapping.match("/user", "POST"));
        assertNotNull(mapping.match("/user/", "GET"));
        assertNotNull(mapping.match("/user/", "POST"));
        assertNotNull(mapping.match("/user/1", "GET"));
        assertNotNull(mapping.match("/user/1", "POST"));
        assertNotNull(mapping.match("/topic/1/", "GET"));
        assertNotNull(mapping.match("/topic/1/", "POST"));
        assertNotNull(mapping.match("/topic/123/134", "GET"));
        assertNotNull(mapping.match("/topic", "GET"));

        // expected: null
        assertNull(mapping.match("", "GET"));
        assertNull(mapping.match("", "POST"));
    }

    public void testStartsWith21() {
        this._testStartsWith2("/hi{name}/{id}");
    }

    public void testStartsWith22() {
        this._testStartsWith2("/hi${name}/${id}");
    }

    public void testStartsWith23() {
        this._testStartsWith2("/hi${name}/{id}");
    }

    public void testStartsWith24() {
        this._testStartsWith2("/hi{name}/${id}");
    }

    public void testStartsWith25() {
        this._testStartsWith2("/hi${name}/$id");
    }

    public void testStartsWith26() {
        this._testStartsWith2("/hi$name/$id");
    }

    private void _testStartsWith2(String pattern) {
        Object target = new Object();
        MappingImpl<?> mapping = new MappingImpl<Object>(pattern, MatchMode.PATH_STARTS_WITH,
                new ReqMethod[] { ReqMethod.ALL }, target);
        // expected: not null
        assertNotNull(mapping.match("/hiuser/123", "POST"));
        assertSame(target, mapping.match("/hiuser/123", "GET").getMapping().getTarget());
        assertNotNull(mapping.match("/hiuser/abc", "GET"));
        assertNotNull(mapping.match("/hiuser/123/", "GET"));
        assertNotNull(mapping.match("/hiuser/abc/", "POST"));
        assertNotNull(mapping.match("/hiuser/123/456", "POST"));
        assertNotNull(mapping.match("/hiuser/abc/def", "GET"));
        assertNotNull(mapping.match("/hiuser/123/456/", "GET"));
        assertNotNull(mapping.match("/hiuser/abc/def/", "POST"));

        // expected: null
        assertNull(mapping.match("/hiuser", "GET"));
        assertNull(mapping.match("/hiuser", "POST"));
        assertNull(mapping.match("/hiuser/", "GET"));;
        assertNull(mapping.match("/user/1", "GET"));
        assertNull(mapping.match("/user/1", "POST"));
        assertNull(mapping.match("", "GET"));
        assertNull(mapping.match("", "POST"));
    }

    public void testStartsWith31() {
        this._testStartsWith3("/hi{name}/{id:[0-9]+}");
    }

    public void testStartsWith32() {
        this._testStartsWith3("/hi${name}/${id:[0-9]+}");
    }

    public void testStartsWith33() {
        this._testStartsWith3("/hi{name}/${id:[0-9]+}");
    }

    public void testStartsWith34() {
        this._testStartsWith3("/hi{name}/${id:[0-9]+}");
    }

    public void testStartsWith35() {
        this._testStartsWith3("/hi$name/$id:[0-9]+");
    }

    public void testStartsWith36() {
        this._testStartsWith3("/hi${name}/$id:[0-9]+");
    }

    private void _testStartsWith3(String pattern) {
        Object target = new Object();
        MappingImpl<?> mapping = new MappingImpl<Object>(pattern, MatchMode.PATH_STARTS_WITH,
                new ReqMethod[] { ReqMethod.ALL }, target);
        assertNotNull(mapping.match("/hiuser/123", "POST"));
        assertSame(target, mapping.match("/hiuser/123", "GET").getMapping().getTarget());
        assertNull(mapping.match("/hiuser/abc", "GET"));
        assertNotNull(mapping.match("/hiuser/123/", "GET"));
        assertNull(mapping.match("/hiuser/abc/", "POST"));
        assertNotNull(mapping.match("/hiuser/123/456", "POST"));
        assertNull(mapping.match("/hiuser/abc/def", "GET"));
        assertNotNull(mapping.match("/hiuser/123/456/", "GET"));
        assertNull(mapping.match("/hiuser/abc/def/", "POST"));

        assertNull(mapping.match("/hiuser", "GET"));
        assertNull(mapping.match("/hiuser", "POST"));
        assertNull(mapping.match("/hiuser/", "GET"));
        assertNull(mapping.match("/user/1", "GET"));
        assertNull(mapping.match("/user/1", "POST"));
        assertNull(mapping.match("", "GET"));
        assertNull(mapping.match("", "POST"));
    }

    public void testStartsWith41() {
        this._testStartsWith4("/hi{name:[abc]{3}}/{id:[0-9]+}");
    }

    public void testStartsWith42() {
        this._testStartsWith4("/hi${name:[abc]{3}}/${id:[0-9]+}");
    }

    public void testStartsWith43() {
        this._testStartsWith4("/hi${name:[abc]{3}}/{id:[0-9]+}");
    }

    public void testStartsWith44() {
        this._testStartsWith4("/hi{name:[abc]{3}}/${id:[0-9]+}");
    }

    public void testStartsWith45() {
        this._testStartsWith4("/hi${name:[abc]{3}}/$id:[0-9]+");
    }

    public void testStartsWith46() {
        this._testStartsWith4("/hi$name:[abc]{3}/$id:[0-9]+");
    }

    private void _testStartsWith4(String pattern) {
        Object target = new Object();
        MappingImpl<?> mapping = new MappingImpl<Object>(pattern, MatchMode.PATH_STARTS_WITH,
                new ReqMethod[] { ReqMethod.ALL }, target);
        assertNotNull(mapping.match("/hiabc/123", "GET"));
        assertSame(target, mapping.match("/hiabc/123", "GET").getMapping().getTarget());
        assertNotNull(mapping.match("/hiaaa/456", "POST"));
        assertNotNull(mapping.match("/hibbb/789", "GET"));
        assertNotNull(mapping.match("/hiccc/0123", "POST"));
        assertNotNull(mapping.match("/hibac/456769", "GET"));
        assertNotNull(mapping.match("/hiaaa/456/", "POST"));
        assertNotNull(mapping.match("/hibbb/789/asdfa", "GET"));
        assertNotNull(mapping.match("/hiccc/0123/a", "POST"));
        assertNotNull(mapping.match("/hibac/456769/bc", "GET"));

        assertNull(mapping.match("/hiabc", "GET"));
        assertNull(mapping.match("/hiaaa/", "POST"));
        assertNull(mapping.match("/hibbb/abc", "GET"));
        assertNull(mapping.match("/hiccc/abc", "POST"));
        assertNull(mapping.match("/hibac/abc", "GET"));
        assertNull(mapping.match("/hi/456", "GET"));
        assertNull(mapping.match("/hia/789", "GET"));

        assertNull(mapping.match("", "GET"));
        assertNull(mapping.match("", "POST"));
    }

    public void testEquals11() {
        this._testEquals1("/{name}");
    }

    public void testEquals12() {
        this._testEquals1("/${name}");
    }

    private void _testEquals1(String pattern) {
        Object target = new Object();
        MappingImpl<?> mapping = new MappingImpl<Object>(pattern, MatchMode.PATH_EQUALS,
                new ReqMethod[] { ReqMethod.ALL }, target);
        // expected: not null
        assertNotNull(mapping.match("/user", "GET"));
        assertSame(target, mapping.match("/user", "GET").getMapping().getTarget());
        assertNotNull(mapping.match("/user", "POST"));
        assertNotNull(mapping.match("/user/", "GET"));
        assertNotNull(mapping.match("/user/", "POST"));
        assertNull(mapping.match("/user/1", "GET"));
        assertNull(mapping.match("/user/1", "POST"));
        assertNull(mapping.match("/topic/1/", "GET"));
        assertNull(mapping.match("/topic/1/", "POST"));
        assertNull(mapping.match("/topic/123/134", "GET"));
        assertNotNull(mapping.match("/topic", "GET"));

        // expected: null
        assertNull(mapping.match("", "GET"));
        assertNull(mapping.match("", "POST"));
    }

    public void testEquals21() {
        this._testEquals2("/hi{name}/{id}");
    }

    public void testEquals22() {
        this._testEquals2("/hi${name}/${id}");
    }

    public void testEquals23() {
        this._testEquals2("/hi${name}/{id}");
    }

    public void testEquals24() {
        this._testEquals2("/hi{name}/${id}");
    }

    public void testEquals25() {
        this._testEquals2("/hi${name}/$id");
    }

    public void testEquals26() {
        this._testEquals2("/hi$name/${id}");
    }

    private void _testEquals2(String pattern) {
        Object target = new Object();
        MappingImpl<?> mapping = new MappingImpl<Object>(pattern, MatchMode.PATH_EQUALS,
                new ReqMethod[] { ReqMethod.ALL }, target);
        // expected: not null
        assertNotNull(mapping.match("/hiuser/123", "POST"));
        assertSame(target, mapping.match("/hiuser/123", "GET").getMapping().getTarget());
        assertNotNull(mapping.match("/hiuser/abc", "GET"));
        assertNotNull(mapping.match("/hiuser/123/", "GET"));
        assertNotNull(mapping.match("/hiuser/abc/", "POST"));
        assertNull(mapping.match("/hiuser/123/456", "POST"));
        assertNull(mapping.match("/hiuser/abc/def", "GET"));
        assertNull(mapping.match("/hiuser/123/456/", "GET"));
        assertNull(mapping.match("/hiuser/abc/def/", "POST"));

        // expected: null
        assertNull(mapping.match("/hiuser", "GET"));
        assertNull(mapping.match("/hiuser", "POST"));
        assertNull(mapping.match("/hiuser/", "GET"));;
        assertNull(mapping.match("/user/1", "GET"));
        assertNull(mapping.match("/user/1", "POST"));
        assertNull(mapping.match("", "GET"));
        assertNull(mapping.match("", "POST"));
    }

    public void testEquals31() {
        this._testEquals3("/hi{name}/{id:[0-9]+}");
    }

    public void testEquals32() {
        this._testEquals3("/hi${name}/${id:[0-9]+}");
    }

    public void testEquals33() {
        this._testEquals3("/hi{name}/${id:[0-9]+}");
    }

    public void testEquals34() {
        this._testEquals3("/hi${name}/{id:[0-9]+}");
    }

    public void testEquals35() {
        this._testEquals3("/hi{name}/$id:[0-9]+");
    }

    public void testEquals36() {
        this._testEquals3("/hi$name/$id:[0-9]+");
    }

    private void _testEquals3(String pattern) {
        Object target = new Object();
        MappingImpl<?> mapping = new MappingImpl<Object>(pattern, MatchMode.PATH_EQUALS,
                new ReqMethod[] { ReqMethod.ALL }, target);
        assertNotNull(mapping.match("/hiuser/123", "POST"));
        assertSame(target, mapping.match("/hiuser/123", "GET").getMapping().getTarget());
        assertNull(mapping.match("/hiuser/abc", "GET"));
        assertNotNull(mapping.match("/hiuser/123/", "GET"));
        assertNull(mapping.match("/hiuser/abc/", "POST"));
        assertNull(mapping.match("/hiuser/123/456", "POST"));
        assertNull(mapping.match("/hiuser/abc/def", "GET"));
        assertNull(mapping.match("/hiuser/123/456/", "GET"));
        assertNull(mapping.match("/hiuser/abc/def/", "POST"));

        assertNull(mapping.match("/hiuser", "GET"));
        assertNull(mapping.match("/hiuser", "POST"));
        assertNull(mapping.match("/hiuser/", "GET"));;
        assertNull(mapping.match("/user/1", "GET"));
        assertNull(mapping.match("/user/1", "POST"));
        assertNull(mapping.match("", "GET"));
        assertNull(mapping.match("", "POST"));
    }

    public void testEquals41() {
        this._testEquals4("/hi{name:[abc]{3}}/{id:[0-9]+}");
    }

    public void testEquals42() {
        this._testEquals4("/hi${name:[abc]{3}}/${id:[0-9]+}");
    }

    public void testEquals43() {
        this._testEquals4("/hi${name:[abc]{3}}/{id:[0-9]+}");
    }

    public void testEquals44() {
        this._testEquals4("/hi{name:[abc]{3}}/${id:[0-9]+}");
    }

    public void testEquals45() {
        this._testEquals4("/hi${name:[abc]{3}}/$id:[0-9]+");
    }

    public void testEquals46() {
        this._testEquals4("/hi$name:[abc]{3}/$id:[0-9]+");
    }

    private void _testEquals4(String pattern) {
        Object target = new Object();
        MappingImpl<?> mapping = new MappingImpl<Object>(pattern, MatchMode.PATH_EQUALS,
                new ReqMethod[] { ReqMethod.ALL }, target);
        assertNotNull(mapping.match("/hiabc/123", "GET"));
        assertSame(target, mapping.match("/hiabc/123", "GET").getMapping().getTarget());
        assertNotNull(mapping.match("/hiaaa/456", "POST"));
        assertNotNull(mapping.match("/hibbb/789", "GET"));
        assertNotNull(mapping.match("/hiccc/0123", "POST"));
        assertNotNull(mapping.match("/hibac/456769", "GET"));
        assertNotNull(mapping.match("/hiaaa/456/", "POST"));
        assertNull(mapping.match("/hibbb/789/asdfa", "GET"));
        assertNull(mapping.match("/hiccc/0123/a", "POST"));
        assertNull(mapping.match("/hibac/456769/bc", "GET"));

        assertNull(mapping.match("/hiabc", "GET"));
        assertNull(mapping.match("/hiaaa/", "POST"));
        assertNull(mapping.match("/hibbb/abc", "GET"));
        assertNull(mapping.match("/hiccc/abc", "POST"));
        assertNull(mapping.match("/hibac/abc", "GET"));
        assertNull(mapping.match("/hi/456", "GET"));
        assertNull(mapping.match("/hia/789", "GET"));

        assertNull(mapping.match("", "GET"));
        assertNull(mapping.match("", "POST"));
    }

    public void testParamAndConstaint11() {
        this._testParamAndConstaint1("/hi{name:[abc]{3}}/{id:[0-9]+}");
    }

    public void testParamAndConstaint12() {
        this._testParamAndConstaint1("/hi${name:[abc]{3}}/${id:[0-9]+}");
    }

    public void testParamAndConstaint13() {
        this._testParamAndConstaint1("/hi${name:[abc]{3}}/{id:[0-9]+}");
    }

    public void testParamAndConstaint14() {
        this._testParamAndConstaint1("/hi{name:[abc]{3}}/${id:[0-9]+}");
    }

    public void testParamAndConstaint15() {
        this._testParamAndConstaint1("/hi$name:[abc]{3}/$id:[0-9]+");
    }

    public void testParamAndConstaint16() {
        this._testParamAndConstaint1("/hi{name:[abc]{3}}/$id:[0-9]+");
    }

    private void _testParamAndConstaint1(String pattern) {
        Object target = new Object();
        MappingImpl<?> mapping = new MappingImpl<Object>(pattern, MatchMode.PATH_EQUALS,
                new ReqMethod[] { ReqMethod.ALL }, target);
        assertTrue(Arrays.equals(new String[] { "name", "id" }, mapping.getParamNames()));
        assertTrue(Arrays.equals(new String[] { "/hi", "/", "" }, mapping.getConstants()));
    }

    public void testParamAndConstaint21() {
        this._testParamAndConstaint2("/{name}");
    }

    public void testParamAndConstaint22() {
        this._testParamAndConstaint2("/${name}");
    }

    public void testParamAndConstaint23() {
        this._testParamAndConstaint2("/$name");
    }

    private void _testParamAndConstaint2(String pattern) {
        Object target = new Object();
        MappingImpl<?> mapping = new MappingImpl<Object>(pattern, MatchMode.PATH_EQUALS,
                new ReqMethod[] { ReqMethod.ALL }, target);
        assertTrue(Arrays.equals(new String[] { "name" }, mapping.getParamNames()));
        assertTrue(Arrays.equals(new String[] { "/", "" }, mapping.getConstants()));
    }
}
