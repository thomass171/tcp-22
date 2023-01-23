package de.yard.threed.sceneserver.testutils;

import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.platform.homebrew.PlatformHomeBrew;

import java.util.HashMap;

public class PlatformSceneServerFactoryForTesting implements PlatformFactory {
    @Override
    public PlatformInternals createPlatform(HashMap<String, String> properties) {
        return PlatformHomeBrew.init(properties,new SimpleEventBusForTesting());
    }
}
