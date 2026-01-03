package de.yard.threed.engine.test;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.testutil.RuntimeTestUtil;

/**
 * Especially for Unity.
 * <p/>
 * Created by thomass on 06.04.16.
 */
public class StringUtilsTest {
    public void test1() {

        RuntimeTestUtil.assertTrue("", StringUtils.endsWith("abc.ac", "ac"));
        RuntimeTestUtil.assertFalse("", StringUtils.endsWith("abcc", "ac"));
        RuntimeTestUtil.assertEquals("substring", "bc", StringUtils.substring("abcc", 1, 3));

        String[] s = StringUtils.splitByWhitespace("a  b");
        RuntimeTestUtil.assertEquals("split.size", 2, s.length);
        RuntimeTestUtil.assertEquals("split[0]", "a", s[0]);
        RuntimeTestUtil.assertEquals("split[1]", "b", s[1]);

        s = StringUtils.splitByWhitespace("a  b \t cc ");
        RuntimeTestUtil.assertEquals("split.size", 3, s.length);
        RuntimeTestUtil.assertEquals("split[0]", "a", s[0]);
        RuntimeTestUtil.assertEquals("split[1]", "b", s[1]);
        RuntimeTestUtil.assertEquals("split[2]", "cc", s[2]);

        //Separator is last character
        s = StringUtils.splitByWholeSeparator("a\nb\nc\n","\n");
        RuntimeTestUtil.assertEquals("split.size", 4, s.length);
        RuntimeTestUtil.assertEquals("split[0]", "a", s[0]);
        RuntimeTestUtil.assertEquals("split[1]", "b", s[1]);
        RuntimeTestUtil.assertEquals("split[2]", "c", s[2]);
        RuntimeTestUtil.assertEquals("split[3]", "", s[3]);
    }

    public void testSplit() {
        //StringUtils.split(null, *)         = null
        //StringUtils.split("", *)           = []
        //StringUtils.split("abc def", null) = ["abc", "def"]
        assertSplit("abc def", " ", "abc", "def");
        assertSplit("abc  def", " ", "abc", "", "def");
        assertSplit("ab:cd:ef", ":", "ab", "cd", "ef");

        assertSplit("a", ",", "a");
        //assertSplitByJava("a", ",");

        assertSplit("a,b", ",", "a", "b");
        assertSplit("a,,b", ",", "a", "", "b");
        assertSplit("a,", ",", "a", "");
        //assertSplitByJava("a,", ",");

        assertSplit(",b", ",", "", "b");
        assertSplit(",-3", ",", "", "-3");
        assertSplit("-0.1,-3,-5", ",", "-0.1", "-3", "-5");

        assertSplit("a->b", "->", "a", "b");
        assertSplit("a->-3", "->", "a", "-3");
    }

    void testSubstringAfter() {
        //StringUtils.substringAfter(null, *)      = null
        //assertEquals("", StringUtils.substringAfter("", *)  );//      = ""
        //assertEquals("", StringUtils.substringAfter(*, null)      = ""
        RuntimeTestUtil.assertEquals("","bc", StringUtils.substringAfter("abc", "a"));//  = "bc"
        RuntimeTestUtil.assertEquals("","cba", StringUtils.substringAfter("abcba", "b"));// = "cba"
        RuntimeTestUtil.assertEquals("","", StringUtils.substringAfter("abc", "c"));//  = ""
        RuntimeTestUtil.assertEquals("","", StringUtils.substringAfter("abc", "d"));//  = ""
        RuntimeTestUtil.assertEquals("","abc", StringUtils.substringAfter("abc", ""));//   = "abc"
    }

    void testSubstringBefore() {
        //StringUtils.substringBefore(null, *)      = null
        //StringUtils.substringBefore("", *)        = ""
        RuntimeTestUtil. assertEquals("","", StringUtils.substringBefore("abc", "a"));//  = ""
        RuntimeTestUtil.assertEquals("","a", StringUtils.substringBefore("abcba", "b"));// = "a"
        RuntimeTestUtil.assertEquals("", "ab", StringUtils.substringBefore("abc", "c"));//   = "ab"
        RuntimeTestUtil.assertEquals("","abc", StringUtils.substringBefore("abc", "d"));//   = "abc"
        RuntimeTestUtil.assertEquals("","", StringUtils.substringBefore("abc", ""));//    = ""
        //assertEquals("bc", StringUtils.substringBefore("abc", null));//   = "abc"
    }

    private void assertSplit(String s, String delim, String... expectedParts) {
        String[] parts = StringUtils.splitByWholeSeparator(s, delim);
        RuntimeTestUtil.assertEquals("",""+expectedParts.length, parts.length+"");
        for (int i = 0; i < expectedParts.length; i++) {
            RuntimeTestUtil.assertEquals("",expectedParts[i], parts[i]);
        }
    }

}
