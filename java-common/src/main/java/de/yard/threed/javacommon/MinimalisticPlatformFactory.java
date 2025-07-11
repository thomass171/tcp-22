package de.yard.threed.javacommon;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;

/**
 * See {@link MinimalisticPlatform}.
 */
public class MinimalisticPlatformFactory implements PlatformFactory {

    @Override
    public PlatformInternals createPlatform(Configuration configuration) {
        PlatformInternals platformInternals = MinimalisticPlatform.init(configuration);
        return platformInternals;
    }
}
