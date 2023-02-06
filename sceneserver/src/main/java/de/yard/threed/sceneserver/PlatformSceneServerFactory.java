package de.yard.threed.sceneserver;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.platform.homebrew.PlatformHomeBrew;

import java.util.HashMap;

/**
 * HomeBrewPlatform with custom renderer? Thats the reason for not using SimpleHeadless. But thats not the only reason.
 * HomeBrewPlatform is a full platform, which SimpleHeadless isn't.
 * 25.1.23:No longer needed because HomeBrewScenerunner creates platform?
 *
 */
public class PlatformSceneServerFactory implements PlatformFactory {
    @Override
    public PlatformInternals createPlatform(Configuration configuration) {
       return PlatformHomeBrew.init(configuration);

    }
}
