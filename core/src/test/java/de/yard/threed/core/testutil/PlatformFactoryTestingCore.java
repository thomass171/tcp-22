package de.yard.threed.core.testutil;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;

import java.util.HashMap;

/**
 * 8.4.21: only for tests in module 'core'?
 * SimpleHeadlessPlatform is not available in core because it depends on core (would cause a cyclic dependency)
 */
public class PlatformFactoryTestingCore implements PlatformFactory {
    @Override
    public PlatformInternals createPlatform(Configuration configuration) {
        /*16.6.21Engine*/PlatformInternals pl = TestPlatform.init(new TestLogFactory(), configuration);
        //RM cannot be part of constructor due to LogFactory
        //16.6.21 ((SimpleHeadlessPlatform) pl).resourcemanager = new TestResourceManager();
        return pl;
    }
}
