package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;

/**
 * Gibt es hier nicht, liegt im Platformtest.
 * 
 * Created by thomass on 18.08.16.
 */
public class UtilTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    
}
