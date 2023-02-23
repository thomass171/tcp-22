package de.yard.threed.engine.test;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.testutil.RuntimeTestUtil;

/**
 * Vor allem für Unity.
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

        //Trennzeichen ist gleichzeitig letztes Zeichen
        s = StringUtils.split("a\nb\nc\n","\n");
        RuntimeTestUtil.assertEquals("split.size", 3, s.length);
        RuntimeTestUtil.assertEquals("split[0]", "a", s[0]);
        RuntimeTestUtil.assertEquals("split[1]", "b", s[1]);
        RuntimeTestUtil.assertEquals("split[2]", "c", s[2]);
    }
}
