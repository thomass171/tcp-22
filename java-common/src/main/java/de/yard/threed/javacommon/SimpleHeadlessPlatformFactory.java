package de.yard.threed.javacommon;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.NativeEventBus;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;


import java.util.HashMap;

/**
 * 27.7.21 Primarily for tests, but also for some tools or desktop apps.
 */
public class SimpleHeadlessPlatformFactory implements PlatformFactory {

    NativeEventBus optionalEventbus = null;

    public SimpleHeadlessPlatformFactory() {
    }

    public SimpleHeadlessPlatformFactory(NativeEventBus eventBus) {
        this.optionalEventbus = eventBus;
    }

    @Override
    public PlatformInternals createPlatform(Configuration configuration) {
        return SimpleHeadlessPlatform.init(configuration, optionalEventbus);
    }
}
