package de.yard.threed.javacommon;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;


import java.util.HashMap;

/**
 * 27.7.21 Primaer fuer Tests, aber auch gut fuer manche tools und andere desktop Anwendungen
 */
public class SimpleHeadlessPlatformFactory implements PlatformFactory {
    @Override
    public PlatformInternals createPlatform(HashMap<String, String> properties) {
        return SimpleHeadlessPlatform.init(properties);
    }
}
