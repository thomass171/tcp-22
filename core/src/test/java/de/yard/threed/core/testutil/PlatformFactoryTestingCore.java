package de.yard.threed.core.testutil;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;

import java.util.HashMap;

/**
 * 8.4.21: only for tests in module 'core'?
 */
public class PlatformFactoryTestingCore implements PlatformFactory {
    @Override
    public PlatformInternals createPlatform(HashMap<String, String> properties) {
        /*16.6.21Engine*/PlatformInternals pl = /*(SimpleHeadlessPlatform)*/ TestPlatform.init(new TestLogFactory(), properties);
        //RM cannot be part of constructor due to LogFactory
        //16.6.21 ((SimpleHeadlessPlatform) pl).resourcemanager = new TestResourceManager();
        return pl;
    }
}
