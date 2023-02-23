package de.yard.threed.core;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ColorTest {

    static Platform platform = TestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

    @Test
    public void test1() {
        Color c =Color.RED;
        int alpha = c.getAlphaasInt();
        Assertions.assertEquals( 255, alpha);
    }
}
