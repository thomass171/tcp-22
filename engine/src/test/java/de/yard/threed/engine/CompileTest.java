package de.yard.threed.engine;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import org.junit.jupiter.api.Test;

/**
 * Nur um compilieren anzustossen
 */
public class CompileTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testSegmentedPath() {

    }


}
