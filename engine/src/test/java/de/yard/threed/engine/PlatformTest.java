package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;

import de.yard.threed.engine.test.MainTest;
import org.junit.jupiter.api.Test;


/**
 * 30.6.21 Not really a platform test; tests only Java/Homebrew. But well, at least the tests are tested.
 */
public class PlatformTest {
    static Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"data", "engine"}, new PlatformFactoryHeadless());

    @Test
    public void testPlatform() {
        MainTest.runTest();
    }


}
