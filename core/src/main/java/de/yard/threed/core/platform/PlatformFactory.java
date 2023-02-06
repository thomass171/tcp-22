package de.yard.threed.core.platform;

import de.yard.threed.core.configuration.Configuration;

import java.util.HashMap;

public interface PlatformFactory {
     // 2.8.21: Now returning PlatformInternals instead of just Platform
     PlatformInternals createPlatform(Configuration configuration);
}
