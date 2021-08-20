package de.yard.threed.core.platform;

import java.util.HashMap;

public interface PlatformFactory {
     // 2.8.21: Now returning PlatformInternals instead of just Platform
     PlatformInternals createPlatform(HashMap<String, String> properties);
}
