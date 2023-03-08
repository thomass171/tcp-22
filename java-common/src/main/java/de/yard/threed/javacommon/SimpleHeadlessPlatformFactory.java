package de.yard.threed.javacommon;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.NativeEventBus;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;


import java.util.HashMap;

/**
 * 27.7.21 Primaer fuer Tests, aber auch gut fuer manche tools und andere desktop Anwendungen
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
