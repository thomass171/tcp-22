package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;

import de.yard.threed.engine.test.MainTest;
import org.junit.Test;


/**
 * 30.6.21 eigentlich witzlos, test doch nur Java/Homebrew. Naja, testes zumindest die Tests.
 */
public class PlatformTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"data", "engine"}, new PlatformFactoryHeadless());

    @Test
    public void testPlatform() {
        MainTest.runTest(null);
    }


}
