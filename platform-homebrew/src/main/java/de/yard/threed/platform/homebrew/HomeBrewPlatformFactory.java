package de.yard.threed.platform.homebrew;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.NativeEventBus;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.javacommon.JAEventBus;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;

/**
 * 23.9.23 This factory creates a headless (no opengl/renderer) platform. Also for scene server but only limited for tests (might lead to cyclic dependencies).
 */
public class HomeBrewPlatformFactory implements PlatformFactory {

    NativeEventBus optionalEventbus = new JAEventBus();

    public HomeBrewPlatformFactory() {
    }

    public HomeBrewPlatformFactory(NativeEventBus eventBus) {
        this.optionalEventbus = eventBus;
    }

    @Override
    public PlatformInternals createPlatform(Configuration configuration) {
        return PlatformHomeBrew.init(configuration,optionalEventbus );
    }
}
