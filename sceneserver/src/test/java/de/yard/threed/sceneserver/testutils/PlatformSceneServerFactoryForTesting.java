package de.yard.threed.sceneserver.testutils;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.platform.homebrew.PlatformHomeBrew;

import java.util.HashMap;

/*replaced with HomeBrewPlatformFactory public class PlatformSceneServerFactoryForTesting implements PlatformFactory {
    @Override
    public PlatformInternals createPlatform(Configuration configuration) {
        return PlatformHomeBrew.init(configuration, new SimpleEventBusForTesting());
    }
}*/
