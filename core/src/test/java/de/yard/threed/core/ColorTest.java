package de.yard.threed.core;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.jupiter.api.Test;

public class ColorTest {

    static Platform platform = TestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

    @Test
    public void test1() {
        Color c =Color.RED;
        int alpha = c.getAlphaasInt();
        TestUtil.assertEquals("", 255, alpha);
    }
}
