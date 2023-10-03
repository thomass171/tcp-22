package de.yard.threed.platform.jme;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;

public class JmePlatformFactory implements PlatformFactory {
    @Override
    public PlatformInternals createPlatform(Configuration configuration) {
        PlatformInternals platformInternals = PlatformJme.init(configuration);
        return platformInternals;
    }
}
