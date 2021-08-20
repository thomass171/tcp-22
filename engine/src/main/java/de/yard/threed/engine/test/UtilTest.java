package de.yard.threed.engine.test;

import de.yard.threed.core.Util;
import de.yard.threed.core.testutil.TestUtil;


/**
 * Created by thomass on 18.08.16.
 */
public class UtilTest {
    /**
     * Gegenprobe mit String.format ist platformabhaengig
     */
    public void testFormat() {
        TestUtil.assertEquals("", "45", Util.format("%d", 45));
        TestUtil.assertEquals("", ",45,", Util.format(",%d,", 45));
        //Gegenprobe
        //TestUtil.assertEquals("", " 45", String.format("%3d", 45));
        TestUtil.assertEquals("", " 45", Util.format("%3d", 45));
        //Gegenprobe
        //TestUtil.assertEquals("", "045", String.format("%03d", 45));
        TestUtil.assertEquals("", "045", Util.format("%03d", 45));
        //Gegenprobe
        //TestUtil.assertEquals("", "         45", String.format("%11d", 45));
        TestUtil.assertEquals("", "         45", Util.format("%11d", 45));
        //Gegenprobe
        //TestUtil.assertEquals("", "00000000045", String.format("%011d", 45));
        TestUtil.assertEquals("", "00000000045", Util.format("%011d", 45));

    }
}
