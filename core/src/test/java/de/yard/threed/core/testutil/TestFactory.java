package de.yard.threed.core.testutil;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;

import java.util.HashMap;

public class TestFactory {

    public static Platform initPlatformForTest(PlatformFactory platformFactory, InitMethod sceneIinitMethod) {

        HashMap<String, String> properties = new HashMap<String, String>();





        PlatformInternals pl = platformFactory.createPlatform(properties);

        return Platform.getInstance();
    }

}
