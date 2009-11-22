package net.paoding.rose.testcases.web.impl.module;

import java.util.Arrays;

import junit.framework.TestCase;
import net.paoding.rose.web.impl.mapping.MappingImpl;
import net.paoding.rose.web.impl.mapping.MatchMode;

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
        MappingImpl mapping = new MappingImpl(pattern, MatchMode.STARTS_WITH);
        // expected: not null
        assertNotNull(mapping.match("/user"));
        assertNotNull(mapping.match("/user"));
        assertNotNull(mapping.match("/user/"));
        assertNotNull(mapping.match("/user/"));
        assertNotNull(mapping.match("/user/1"));
        assertNotNull(mapping.match("/user/1"));
        assertNotNull(mapping.match("/topic/1/"));
        assertNotNull(mapping.match("/topic/1/"));
        assertNotNull(mapping.match("/topic/123/134"));
        assertNotNull(mapping.match("/topic"));

        // expected: null
        assertNull(mapping.match(""));
        assertNull(mapping.match(""));
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
        MappingImpl mapping = new MappingImpl(pattern, MatchMode.STARTS_WITH);
        // expected: not null
        assertNotNull(mapping.match("/hiuser/123"));
        assertNotNull(mapping.match("/hiuser/abc"));
        assertNotNull(mapping.match("/hiuser/123/"));
        assertNotNull(mapping.match("/hiuser/abc/"));
        assertNotNull(mapping.match("/hiuser/123/456"));
        assertNotNull(mapping.match("/hiuser/abc/def"));
        assertNotNull(mapping.match("/hiuser/123/456/"));
        assertNotNull(mapping.match("/hiuser/abc/def/"));

        // expected: null
        assertNull(mapping.match("/hiuser"));
        assertNull(mapping.match("/hiuser"));
        assertNull(mapping.match("/hiuser/"));;
        assertNull(mapping.match("/user/1"));
        assertNull(mapping.match("/user/1"));
        assertNull(mapping.match(""));
        assertNull(mapping.match(""));
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
        MappingImpl mapping = new MappingImpl(pattern, MatchMode.STARTS_WITH);
        assertNotNull(mapping.match("/hiuser/123"));
        assertNull(mapping.match("/hiuser/abc"));
        assertNotNull(mapping.match("/hiuser/123/"));
        assertNull(mapping.match("/hiuser/abc/"));
        assertNotNull(mapping.match("/hiuser/123/456"));
        assertNull(mapping.match("/hiuser/abc/def"));
        assertNotNull(mapping.match("/hiuser/123/456/"));
        assertNull(mapping.match("/hiuser/abc/def/"));

        assertNull(mapping.match("/hiuser"));
        assertNull(mapping.match("/hiuser"));
        assertNull(mapping.match("/hiuser/"));
        assertNull(mapping.match("/user/1"));
        assertNull(mapping.match("/user/1"));
        assertNull(mapping.match(""));
        assertNull(mapping.match(""));
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
        MappingImpl mapping = new MappingImpl(pattern, MatchMode.STARTS_WITH);
        assertNotNull(mapping.match("/hiabc/123"));
        assertNotNull(mapping.match("/hiaaa/456"));
        assertNotNull(mapping.match("/hibbb/789"));
        assertNotNull(mapping.match("/hiccc/0123"));
        assertNotNull(mapping.match("/hibac/456769"));
        assertNotNull(mapping.match("/hiaaa/456/"));
        assertNotNull(mapping.match("/hibbb/789/asdfa"));
        assertNotNull(mapping.match("/hiccc/0123/a"));
        assertNotNull(mapping.match("/hibac/456769/bc"));

        assertNull(mapping.match("/hiabc"));
        assertNull(mapping.match("/hiaaa/"));
        assertNull(mapping.match("/hibbb/abc"));
        assertNull(mapping.match("/hiccc/abc"));
        assertNull(mapping.match("/hibac/abc"));
        assertNull(mapping.match("/hi/456"));
        assertNull(mapping.match("/hia/789"));

        assertNull(mapping.match(""));
        assertNull(mapping.match(""));
    }

    public void testEquals11() {
        this._testEquals1("/{name}");
    }

    public void testEquals12() {
        this._testEquals1("/${name}");
    }

    private void _testEquals1(String pattern) {
        MappingImpl mapping = new MappingImpl(pattern, MatchMode.EQUALS);
        // expected: not null
        assertNotNull(mapping.match("/user"));
        assertNotNull(mapping.match("/user"));
        assertNull(mapping.match("/user/1"));
        assertNull(mapping.match("/topic/1/"));
        assertNull(mapping.match("/topic/123/134"));
        assertNotNull(mapping.match("/topic"));

        // expected: null
        assertNull(mapping.match(""));
        assertNull(mapping.match(""));
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
        MappingImpl mapping = new MappingImpl(pattern, MatchMode.EQUALS);
        // expected: not null
        assertNotNull(mapping.match("/hiuser/123"));
        assertNotNull(mapping.match("/hiuser/abc"));
        assertNotNull(mapping.match("/hiuser/123/"));
        assertNotNull(mapping.match("/hiuser/abc/"));
        assertNull(mapping.match("/hiuser/123/456"));
        assertNull(mapping.match("/hiuser/abc/def"));
        assertNull(mapping.match("/hiuser/123/456/"));
        assertNull(mapping.match("/hiuser/abc/def/"));

        // expected: null
        assertNull(mapping.match("/hiuser"));
        assertNull(mapping.match("/hiuser"));
        assertNull(mapping.match("/hiuser/"));;
        assertNull(mapping.match("/user/1"));
        assertNull(mapping.match("/user/1"));
        assertNull(mapping.match(""));
        assertNull(mapping.match(""));
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
        MappingImpl mapping = new MappingImpl(pattern, MatchMode.EQUALS);
        assertNotNull(mapping.match("/hiuser/123"));
        assertNull(mapping.match("/hiuser/abc"));
        assertNotNull(mapping.match("/hiuser/123/"));
        assertNull(mapping.match("/hiuser/abc/"));
        assertNull(mapping.match("/hiuser/123/456"));
        assertNull(mapping.match("/hiuser/abc/def"));
        assertNull(mapping.match("/hiuser/123/456/"));
        assertNull(mapping.match("/hiuser/abc/def/"));

        assertNull(mapping.match("/hiuser"));
        assertNull(mapping.match("/hiuser"));
        assertNull(mapping.match("/hiuser/"));;
        assertNull(mapping.match("/user/1"));
        assertNull(mapping.match("/user/1"));
        assertNull(mapping.match(""));
        assertNull(mapping.match(""));
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
        MappingImpl mapping = new MappingImpl(pattern, MatchMode.EQUALS);
        assertNotNull(mapping.match("/hiabc/123"));
        assertNotNull(mapping.match("/hiaaa/456"));
        assertNotNull(mapping.match("/hibbb/789"));
        assertNotNull(mapping.match("/hiccc/0123"));
        assertNotNull(mapping.match("/hibac/456769"));
        assertNotNull(mapping.match("/hiaaa/456/"));
        assertNull(mapping.match("/hibbb/789/asdfa"));
        assertNull(mapping.match("/hiccc/0123/a"));
        assertNull(mapping.match("/hibac/456769/bc"));

        assertNull(mapping.match("/hiabc"));
        assertNull(mapping.match("/hiaaa/"));
        assertNull(mapping.match("/hibbb/abc"));
        assertNull(mapping.match("/hiccc/abc"));
        assertNull(mapping.match("/hibac/abc"));
        assertNull(mapping.match("/hi/456"));
        assertNull(mapping.match("/hia/789"));

        assertNull(mapping.match(""));
        assertNull(mapping.match(""));
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
        MappingImpl mapping = new MappingImpl(pattern, MatchMode.EQUALS);
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
        MappingImpl mapping = new MappingImpl(pattern, MatchMode.EQUALS);
        assertTrue(Arrays.equals(new String[] { "name" }, mapping.getParamNames()));
        assertTrue(Arrays.equals(new String[] { "/", "" }, mapping.getConstants()));
    }
}
