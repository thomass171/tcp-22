package de.yard.threed.core.testutil;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;

import java.util.HashMap;

/**
 * ConfigurationByEnv is not available in core,but needed by some tests for "ADDITIONALBUNDLE"
 */
public class CoreTestFactory {

    public static Platform initPlatformForTest(PlatformFactory platformFactory, InitMethod sceneIinitMethod) {

        HashMap<String, String> properties = new HashMap<String, String>();
        PlatformInternals pl = platformFactory.createPlatform(new ConfigurationByProperties(properties));

        return Platform.getInstance();
    }

    /**
     * additionalConfiguration probably is a ConfigurationByEnv.
     *
     */
    public static Platform initPlatformForTest(PlatformFactory platformFactory, InitMethod sceneIinitMethod, Configuration additionalConfiguration) {

        // Some projects tests need env variables like "ADDITIONALBUNDLE"
        HashMap<String, String> properties = new HashMap<String, String>();
        PlatformInternals pl = platformFactory.createPlatform(new ConfigurationByProperties(properties)
                .addConfiguration(additionalConfiguration, false));

        return Platform.getInstance();
    }
}
