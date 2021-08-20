package de.yard.threed.engine.test;

import de.yard.threed.core.Color;
import de.yard.threed.core.testutil.TestUtil;

/**
 * Als Platformtest wegen Integerarithmetik.
 * <p/>
 * Created by thomass on 24.05.16.
 */
public class ColorTest {
    public void testColor() {
        Color col = new Color(0f, 1f, 0f, 1f);
        int argb = col.getARGB();
        TestUtil.assertInt("", -16711936, argb);
        col = new Color(0, 0xFF, 0, 0xFF);
        argb = col.getARGB();
        TestUtil.assertInt("", -16711936, argb);
        TestUtil.assertInt("", 255, col.getAlphaasInt());
        col = new Color(-16711936);
        TestUtil.assertInt("", 255, col.getAlphaasInt());
        TestUtil.assertInt("", 0, col.getRasInt());
        TestUtil.assertInt("", 255, col.getGasInt());
        TestUtil.assertInt("", 0, col.getBasInt());
    }
}
