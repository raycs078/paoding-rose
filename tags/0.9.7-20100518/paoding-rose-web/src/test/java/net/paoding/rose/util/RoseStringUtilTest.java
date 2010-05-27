package net.paoding.rose.util;

import org.junit.Test;

import junit.framework.Assert;

public class RoseStringUtilTest {

    @Test
    public void test() {
        Assert.assertEquals("", RoseStringUtil.relativePathToModulePath(""));
        Assert.assertEquals("/abc", RoseStringUtil.relativePathToModulePath("abc"));
        Assert.assertEquals("/abc", RoseStringUtil.relativePathToModulePath("abc/"));
    }
}
