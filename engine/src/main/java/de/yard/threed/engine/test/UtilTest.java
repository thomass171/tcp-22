package de.yard.threed.engine.test;

import de.yard.threed.core.Util;
import de.yard.threed.core.testutil.RuntimeTestUtil;


/**
 * Created by thomass on 18.08.16.
 */
public class UtilTest {
    /**
     * Gegenprobe mit String.format ist platformabhaengig
     */
    public void testFormat() {
        RuntimeTestUtil.assertEquals("", "45", Util.format("%d", 45));
        RuntimeTestUtil.assertEquals("", ",45,", Util.format(",%d,", 45));
        //Gegenprobe
        //TestUtil.assertEquals("", " 45", String.format("%3d", 45));
        RuntimeTestUtil.assertEquals("", " 45", Util.format("%3d", 45));
        //Gegenprobe
        //TestUtil.assertEquals("", "045", String.format("%03d", 45));
        RuntimeTestUtil.assertEquals("", "045", Util.format("%03d", 45));
        //Gegenprobe
        //TestUtil.assertEquals("", "         45", String.format("%11d", 45));
        RuntimeTestUtil.assertEquals("", "         45", Util.format("%11d", 45));
        //Gegenprobe
        //TestUtil.assertEquals("", "00000000045", String.format("%011d", 45));
        RuntimeTestUtil.assertEquals("", "00000000045", Util.format("%011d", 45));

        // format double
        RuntimeTestUtil.assertEquals("", "1.14", Util.format(1.14, 8,4));
        RuntimeTestUtil.assertEquals("", "1.1", Util.format(1.14, 8,1));
        RuntimeTestUtil.assertEquals("", "1.1", Util.format(1.18, 8,1));
        RuntimeTestUtil.assertEquals("", "1.1", Util.format(1.1, 8,4));
        RuntimeTestUtil.assertEquals("", "1", Util.format(1.1, 8,0));
        //fails in browser RuntimeTestUtil.assertEquals("", "1.0", Util.format(1.0, 8,4));
        RuntimeTestUtil.assertEquals("", "1", Util.format(1.0, 8,0));


    }
}
